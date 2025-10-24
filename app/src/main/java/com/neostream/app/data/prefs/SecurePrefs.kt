package com.neostream.app.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefs(ctx: Context) {
  private val masterKey = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
  private val p = EncryptedSharedPreferences.create(
    ctx, "secure", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )
  fun setPlaylistUrl(url: String) { p.edit().putString("playlist_url", url).apply() }
  fun getPlaylistUrl(): String? = p.getString("playlist_url", null)
}
