package com.neostream.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.neostream.app.databinding.ActivityPlayerBinding
import com.neostream.app.net.UrlResolver
import com.neostream.app.net.UrlPrefetcher
import com.neostream.app.exo.ExoFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PlayerActivity : AppCompatActivity(), Player.Listener {

  private lateinit var vb: ActivityPlayerBinding
  private var player: ExoPlayer? = null
  private lateinit var urls: MutableList<String>
  private var currentIndex = 0
  private var referer: String = ""
  private var cookie: String = ""
  private val currentUrl: String get() = urls.getOrElse(currentIndex) { "" }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityPlayerBinding.inflate(layoutInflater)
    setContentView(vb.root)

    // Modern immersive fullscreen
    WindowCompat.setDecorFitsSystemWindows(window, false)
    hideSystemBars()

    val first = intent.getStringExtra("url")
    if (first.isNullOrBlank()) {
      Toast.makeText(this, "URL manquante", Toast.LENGTH_SHORT).show()
      finish(); return
    }

    urls = (intent.getStringArrayExtra("urlList")?.toList() ?: emptyList()).toMutableList()
    if (urls.isEmpty()) urls.add(first)

    referer = intent.getStringExtra("referer").orEmpty()
    cookie = intent.getStringExtra("cookie").orEmpty()

    startPlayback(urls[currentIndex])
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) hideSystemBars()
  }

  private fun hideSystemBars() {
    val controller = WindowInsetsControllerCompat(window, vb.playerView)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller.hide(WindowInsetsCompat.Type.systemBars())
  }

  private fun startPlayback(url: String) {
    val ua = "NEOSTREAM/1.0 (AndroidTV)"
    val headers = mutableMapOf("User-Agent" to ua)
    if (referer.isNotBlank()) headers["Referer"] = referer
    if (cookie.isNotBlank())  headers["Cookie"]  = cookie

    lifecycleScope.launch {
      try {
        val resolved = withContext(Dispatchers.IO) { UrlResolver.resolve(url, headers) }

        val finalHeaders = headers + listOfNotNull(
          resolved.cookie?.let { "Cookie" to it }
        ).toMap()
        val finalUrl = resolved.url
        val headersMap = finalHeaders

        val dataSourceFactory: DataSource.Factory = ExoFactory.okHttp(finalHeaders)

        player = ExoFactory.player(this@PlayerActivity).also {
          vb.playerView.player = it
          it.addListener(this@PlayerActivity)
        }

        val lower = finalUrl.lowercase()
        val mediaItem = MediaItem.Builder()
          .setUri(finalUrl)
          .setMimeType(
            when {
              lower.endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
              lower.endsWith(".mpd")  -> MimeTypes.APPLICATION_MPD
              else -> null
            }
          )
          .build()

        val source = when {
          lower.endsWith(".m3u8") -> HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(mediaItem)
          lower.endsWith(".mpd") -> DashMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
          else -> ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        }

        player!!.setMediaSource(source)

        // Pré-zapping best-effort (en parallèle, non bloquant)
        GlobalScope.launch(Dispatchers.IO) {
          try { UrlPrefetcher.prefetch(finalUrl, headersMap) } catch (_: Throwable) {}
        }

        player!!.playWhenReady = true
        player!!.prepare()
      } catch (_: Exception) {
        handlePlaybackError()
      }
    }
  }

  private fun tryStart(nextUrl: String): Boolean {
    // Release old player if any and (re)start
    vb.playerView.player = null
    player?.release(); player = null
    startPlayback(nextUrl)
    return true
  }

  private fun handlePlaybackError() {
    // Default chained fallback across provided list
    if (currentIndex + 1 < urls.size) {
      currentIndex++
      Toast.makeText(this, "Changement de source…", Toast.LENGTH_SHORT).show()
      tryStart(urls[currentIndex])
    } else {
      Toast.makeText(this, "Lecture impossible", Toast.LENGTH_LONG).show()
      finish()
    }
  }

  override fun onPlayerError(error: PlaybackException) {
    val cur = currentUrl
    if (cur.endsWith(".m3u8", true)) {
      val ts = cur.removeSuffix(".m3u8") + ".ts"
      if (!tryStart(ts)) {
        Toast.makeText(this, "Lecture impossible (${error.errorCodeName})", Toast.LENGTH_LONG).show()
      }
    } else {
      Toast.makeText(this, "Erreur lecteur: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
      handlePlaybackError()
    }
  }

  override fun onStop() { super.onStop(); player?.pause() }

  override fun onDestroy() {
    super.onDestroy()
    vb.playerView.player = null
    player?.release(); player = null
  }
}
