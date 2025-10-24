package com.neostream.app.ui.imports

import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.neostream.app.databinding.ActivityAddSourceBinding
import com.neostream.app.data.prefs.SecurePrefs
import com.neostream.app.data.ingest.M3uImporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI

private const val TAG = "AddSourceActivity"

class AddSourceActivity : AppCompatActivity() {
  private lateinit var vb: ActivityAddSourceBinding
  private var isImporting = false

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
    // Prevent double-click
    if (isImporting) {
      Log.d(TAG, "Import already in progress, ignoring click")
      return
    }

    val isXtream = vb.rbXtream.isChecked
    val url = if (isXtream) buildXtreamUrl() else vb.inputM3uUrl.text?.toString()?.trim().orEmpty()
    
    Log.d(TAG, "onImport called - Mode: ${if (isXtream) "Xtream" else "M3U"}")
    Log.d(TAG, "Built URL: $url")

    // Validation
    if (url.isEmpty()) {
      val msg = if (isXtream) "Remplissez tous les champs (serveur, username, password)" else "Entrez une URL M3U"
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
      Log.w(TAG, "Empty URL - validation failed")
      return
    }

    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      Toast.makeText(this, "URL invalide - doit commencer par http:// ou https://", Toast.LENGTH_LONG).show()
      Log.w(TAG, "Invalid URL format: $url")
      return
    }

    // Start import
    isImporting = true
    vb.btnImport.isEnabled = false
    vb.btnImport.text = "Import en cours..."
    
    Log.d(TAG, "Starting import from URL: $url")

    lifecycleScope.launch {
      try {
        // Sauvegarde chiffrée
        SecurePrefs(this@AddSourceActivity).setPlaylistUrl(url)
        Log.d(TAG, "URL saved to secure prefs")
        
        // Import
        Toast.makeText(this@AddSourceActivity, "Téléchargement de la playlist...", Toast.LENGTH_SHORT).show()
        M3uImporter(this@AddSourceActivity).importFromUrl(url)
        
        Log.d(TAG, "Import completed successfully")
        Toast.makeText(this@AddSourceActivity, "✅ Playlist importée avec succès!", Toast.LENGTH_LONG).show()
        
        // Wait a bit so user can see success message
        delay(1500)
        finish()
      } catch (t: Throwable) {
        Log.e(TAG, "Import failed", t)
        val errorMsg = when {
          t.message?.contains("Unable to resolve host") == true -> 
            "Erreur réseau: impossible de contacter le serveur"
          t.message?.contains("timeout") == true -> 
            "Timeout: le serveur ne répond pas"
          t.message?.contains("401") == true || t.message?.contains("403") == true -> 
            "Erreur d'authentification: vérifiez vos identifiants"
          t.message?.contains("404") == true -> 
            "URL introuvable (404)"
          else -> 
            "Échec import: ${t.message}"
        }
        Toast.makeText(this@AddSourceActivity, errorMsg, Toast.LENGTH_LONG).show()
      } finally {
        isImporting = false
        vb.btnImport.isEnabled = true
        vb.btnImport.text = "Importer"
      }
    }
  }

  /** Construit l'URL Xtream → M3U Plus */
  private fun buildXtreamUrl(): String {
    var server = vb.inputServer.text?.toString()?.trim().orEmpty()
    val user = vb.inputUser.text?.toString()?.trim().orEmpty()
    val pass = vb.inputPass.text?.toString()?.trim().orEmpty()
    val https = vb.checkHttps.isChecked

    Log.d(TAG, "Building Xtream URL - Server: $server, User: $user, HTTPS: $https")

    if (server.isEmpty() || user.isEmpty() || pass.isEmpty()) {
      Log.w(TAG, "Empty fields - Server empty: ${server.isEmpty()}, User empty: ${user.isEmpty()}, Pass empty: ${pass.isEmpty()}")
      return ""
    }

    // Normalisation : schéma + port
    if (!server.startsWith("http")) {
      server = (if (https) "https://" else "http://") + server
      Log.d(TAG, "Added scheme to server: $server")
    }
    
    // Ajoute :80 si seulement un host sans port & schéma http
    try {
      val u = URI(server)
      val host = u.host ?: server
      val scheme = u.scheme ?: (if (https) "https" else "http")
      val port = if (u.port != -1) u.port else if (scheme == "http") 80 else 443
      server = "$scheme://$host:$port"
      Log.d(TAG, "Normalized server: $server")
    } catch (e: Throwable) {
      Log.w(TAG, "Failed to parse server URI, using as-is", e)
    }

    val finalUrl = "$server/get.php?username=$user&password=$pass&type=m3u_plus&output=m3u8"
    Log.d(TAG, "Final Xtream URL built: $finalUrl")
    return finalUrl
  }
}