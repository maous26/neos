package com.neostream.app.vpn

import kotlinx.coroutines.delay

class MockEngine : VpnEngine {
  private var running = false
  override suspend fun start(config: VpnConfig): Boolean { delay(500); running = true; return true }
  override suspend fun stop(): Boolean { running = false; return true }
  override fun isRunning() = running
}
