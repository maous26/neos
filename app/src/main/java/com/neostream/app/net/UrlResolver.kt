package com.neostream.app.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class ResolvedUrl(
  val url: String,
  val cookie: String? = null,
  val contentType: String? = null
)

object UrlResolver {
  private val client = OkHttpClient.Builder()
    .followRedirects(false) // on suit à la main pour capturer Location/cookies
    .build()

  fun resolve(initialUrl: String, headers: Map<String, String> = emptyMap()): ResolvedUrl {
    var currentUrl = initialUrl
    val cookies = mutableListOf<String>()
    var lastContentType: String? = null

    repeat(6) {
      val reqBuilder = Request.Builder().url(currentUrl)
      headers.forEach { (k,v) -> if (v.isNotBlank()) reqBuilder.addHeader(k, v) }
      val resp: Response = client.newCall(reqBuilder.build()).execute()
      lastContentType = resp.header("Content-Type")
      val loc = resp.header("Location")
      resp.header("Set-Cookie")?.let { cookies += it }
      if (loc != null && (resp.code in 300..399)) {
        resp.close()
        currentUrl = if (loc.startsWith("http")) loc else run {
          val base = currentUrl.substringBefore("/", "")
          if (base.isNotEmpty()) "$base$loc" else loc
        }
      } else {
        resp.close()
        return ResolvedUrl(
          url = currentUrl,
          cookie = cookies.takeIf { it.isNotEmpty() }?.joinToString("; "),
          contentType = lastContentType
        )
      }
    }
    return ResolvedUrl(
      url = currentUrl,
      cookie = cookies.takeIf { it.isNotEmpty() }?.joinToString("; "),
      contentType = lastContentType
    )
  }
}
