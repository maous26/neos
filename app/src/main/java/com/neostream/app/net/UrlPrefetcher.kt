package com.neostream.app.net

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object UrlPrefetcher {
  private val client = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(3, TimeUnit.SECONDS)
    .build()

  fun prefetch(url: String, headers: Map<String,String> = emptyMap()) {
    try {
      // 1) manifest
      client.newCall(Request.Builder().url(url).apply {
        headers.forEach { (k,v) -> if (v.isNotBlank()) header(k,v) }
      }.build()).execute().use { /* ignore body */ }

      if (url.endsWith(".m3u8", true)) {
        // 2) première ligne segment (best effort)
        client.newCall(Request.Builder().url(url).apply {
          headers.forEach { (k,v) -> if (v.isNotBlank()) header(k,v) }
        }.build()).execute().use { resp ->
          val text = resp.body?.string() ?: return
          val seg = text.lineSequence().firstOrNull { it.startsWith("http") || it.endsWith(".ts") } ?: return
          val segUrl = if (seg.startsWith("http")) seg else url.substringBeforeLast("/") + "/" + seg
          // range partielle pour amorcer le cache réseau
          client.newCall(Request.Builder().url(segUrl).apply {
            header("Range","bytes=0-65535"); headers.forEach { (k,v) -> if (v.isNotBlank()) header(k,v) }
          }.build()).execute().close()
        }
      }
    } catch (_: Throwable) { /* best effort, no crash */ }
  }
}
