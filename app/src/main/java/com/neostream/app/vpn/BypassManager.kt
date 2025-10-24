package com.neostream.app.vpn

import java.net.InetAddress
import java.net.URI

object BypassManager {
  fun computeExclusions(urls: List<String>): List<String> = buildList {
    urls.mapNotNull { safeHost(it) }.distinct().forEach { host ->
      try { InetAddress.getAllByName(host).forEach { add("${it.hostAddress}/32") } } catch (_: Throwable) {}
    }
  }

  private fun safeHost(u: String): String? = try { URI(u).host } catch (_: Throwable) { null }
}
