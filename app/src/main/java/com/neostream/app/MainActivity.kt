package com.neostream.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.neostream.app.databinding.ActivityMainBinding
import com.neostream.app.ui.imports.AddSourceActivity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
  private lateinit var vb: ActivityMainBinding

  private val addSourceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
      Toast.makeText(this, "Playlist importée ✅", Toast.LENGTH_LONG).show()
      // Optionally, bring focus back to play/sources inputs for TV remotes
      vb.inputUrl.requestFocus()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityMainBinding.inflate(layoutInflater)
    setContentView(vb.root)

    // Bouton "Coller depuis presse-papiers"
    vb.btnPaste.setOnClickListener {
      val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = cm.primaryClip
      val text = clip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
      if (!text.isNullOrBlank()) {
        vb.inputUrl.setText(text)
        vb.inputUrl.setSelection(text.length)
      }
    }

    // Open AddSource directly (M3U/Xtream toggle)
    vb.btnSources.setOnClickListener {
      addSourceLauncher.launch(Intent(this, AddSourceActivity::class.java))
    }

    vb.btnPlay.setOnClickListener {
      val url = vb.inputUrl.text?.toString()?.trim().orEmpty()
      val referer = vb.inputReferer.text?.toString()?.trim().orEmpty()
      val cookie  = vb.inputCookie.text?.toString()?.trim().orEmpty()

      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        Toast.makeText(this, "Entrez une URL valide (http/https)", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      Log.d(TAG, "Play clicked: url=$url, referer=${referer.isNotEmpty()}, cookie=${cookie.isNotEmpty()}")

      val i = Intent().setClassName(this, "com.neostream.app.ui.PlayerActivity").apply {
        putExtra("url", url)
        putExtra("referer", referer)
        putExtra("cookie", cookie)
      }
      startActivity(i)
    }

    // Support: déclenchement via ADB (extras)
    handleDeepLink(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent) // Update the activity's intent
    handleDeepLink(intent)
  }

  private fun handleDeepLink(i: Intent) {
    val url = i.getStringExtra("url") ?: return
    val referer = i.getStringExtra("referer").orEmpty()
    val cookie  = i.getStringExtra("cookie").orEmpty()
    Log.d(TAG, "DeepLink extras: url=$url")
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.PlayerActivity").apply {
      putExtra("url", url)
      putExtra("referer", referer)
      putExtra("cookie", cookie)
    })
  }
}
