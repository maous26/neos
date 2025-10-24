package com.neostream.app.ui.imports

import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.neostream.app.databinding.ActivityAddSourceBinding
import com.neostream.app.data.prefs.SecurePrefs
import com.neostream.app.data.ingest.M3uImporter
import kotlinx.coroutines.launch
import java.net.URI

class AddSourceActivity : AppCompatActivity() {
  private lateinit var vb: ActivityAddSourceBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityAddSourceBinding.inflate(layoutInflater)
    setContentView(vb.root)

    // Ensure initial visibility reflects default selection
    toggleBoxes()

    // Toggle UI
    vb.rgType.setOnCheckedChangeListener { _, _ -> toggleBoxes() }
    vb.btnPaste.setOnClickListener {
      val cm = getSystemService(ClipboardManager::class.java)
      val txt = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
      if (!txt.isNullOrBlank()) vb.inputM3uUrl.setText(txt)
    }

    vb.btnImport.setOnClickListener { onImport() }

    // Option: pré-remplir depuis extra debug
    intent.getStringExtra("presetUrl")?.let { vb.inputM3uUrl.setText(it) }
  }

  private fun toggleBoxes() {
    val xtream = vb.rbXtream.isChecked
    vb.boxXtream.visibility = if (xtream) View.VISIBLE else View.GONE
    vb.boxM3u.visibility = if (xtream) View.GONE else View.VISIBLE
  }

  private fun onImport() {
    val url = if (vb.rbXtream.isChecked) buildXtreamUrl() else vb.inputM3uUrl.text?.toString()?.trim().orEmpty()
    if (!url.startsWith("http")) {
      Toast.makeText(this, "URL invalide", Toast.LENGTH_SHORT).show(); return
    }
    lifecycleScope.launch {
      try {
        // Sauvegarde chiffrée
        SecurePrefs(this@AddSourceActivity).setPlaylistUrl(url)
        // Import
        M3uImporter(this@AddSourceActivity).importFromUrl(url)
        Toast.makeText(this@AddSourceActivity, "Playlist importée ✅", Toast.LENGTH_LONG).show()
        finish()
      } catch (t: Throwable) {
        Toast.makeText(this@AddSourceActivity, "Échec import: ${t.message}", Toast.LENGTH_LONG).show()
      }
    }
  }

  /** Construit l’URL Xtream → M3U Plus */
  private fun buildXtreamUrl(): String {
    var server = vb.inputServer.text?.toString()?.trim().orEmpty()
    val user = vb.inputUser.text?.toString()?.trim().orEmpty()
    val pass = vb.inputPass.text?.toString()?.trim().orEmpty()
    val https = vb.checkHttps.isChecked

    if (server.isEmpty() || user.isEmpty() || pass.isEmpty()) return ""

    // Normalisation : schéma + port
    if (!server.startsWith("http")) server = (if (https) "https://" else "http://") + server
    // Ajoute :80 si seulement un host sans port & schéma http
    try {
      val u = URI(server)
      val host = u.host ?: server
      val scheme = u.scheme ?: (if (https) "https" else "http")
      val port = if (u.port != -1) u.port else if (scheme == "http") 80 else 443
      server = "$scheme://$host:$port"
    } catch (_: Throwable) {}

    return "$server/get.php?username=$user&password=$pass&type=m3u_plus&output=m3u8"
  }
}
