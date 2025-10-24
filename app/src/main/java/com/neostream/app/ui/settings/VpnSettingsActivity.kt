package com.neostream.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.neostream.app.databinding.ActivityVpnSettingsBinding
import com.neostream.app.vpn.*

class VpnSettingsActivity: AppCompatActivity() {
  private lateinit var vb: ActivityVpnSettingsBinding
  private val engine: VpnEngine = MockEngine()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityVpnSettingsBinding.inflate(layoutInflater); setContentView(vb.root)

    vb.toggleVpn.setOnCheckedChangeListener { _, checked ->
      if (checked) startSmartVpn() else stopSmartVpn()
    }
    vb.btnTestPath.setOnClickListener { testPath() }
  }

  private fun startSmartVpn() {
    val recentVideoHostsCidrs = BypassManager.computeExclusions(listOf())
    val cfg = VpnConfig(serverHost = "vpn.example.com", serverPort = 1194, excludeCidrs = recentVideoHostsCidrs)
    val svc = getSystemService(SmartVpnService::class.java)
    if (svc == null) {
      Toast.makeText(this, "Service VPN indisponible", Toast.LENGTH_SHORT).show()
      vb.toggleVpn.isChecked = false
      return
    }
    svc.startTunnel(cfg, engine) { ok ->
      Toast.makeText(this, if (ok) "VPN actif (split)" else "Échec VPN", Toast.LENGTH_SHORT).show()
      vb.toggleVpn.isChecked = ok
    }
  }

  private fun stopSmartVpn() {
    val svc = getSystemService(SmartVpnService::class.java) ?: return
    svc.stopTunnel(engine) { Toast.makeText(this, "VPN coupé", Toast.LENGTH_SHORT).show() }
  }

  private fun testPath() { Toast.makeText(this, "Test chemin (TODO)", Toast.LENGTH_SHORT).show() }
}
