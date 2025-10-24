package com.neostream.app.vpn

interface VpnEngine {
  suspend fun start(config: VpnConfig): Boolean
  suspend fun stop(): Boolean
  fun isRunning(): Boolean
}

data class VpnConfig(
  val serverHost: String,
  val serverPort: Int,
  val username: String? = null,
  val password: String? = null,
  val excludeCidrs: List<String> = emptyList()
)
