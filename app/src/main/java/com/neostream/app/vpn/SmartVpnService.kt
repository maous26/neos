package com.neostream.app.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class SmartVpnService : VpnService() {
  private var vpnInterface: ParcelFileDescriptor? = null
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private var running = false

  override fun onCreate() { super.onCreate() }

  fun startTunnel(cfg: VpnConfig, engine: VpnEngine, onReady: (Boolean)->Unit) {
    scope.launch {
      val ok = engine.start(cfg)
      running = ok
      withContext(Dispatchers.Main) { onReady(ok) }
    }
  }

  fun stopTunnel(engine: VpnEngine, onDone: ()->Unit = {}) {
    scope.launch {
      engine.stop()
      vpnInterface?.close(); vpnInterface = null
      running = false
      withContext(Dispatchers.Main) { onDone() }
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
