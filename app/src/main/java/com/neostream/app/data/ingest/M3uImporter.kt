package com.neostream.app.data.ingest

import android.content.Context
import com.neostream.app.data.db.ChannelEntity
import com.neostream.app.data.db.NeostreamDb
import com.neostream.app.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.source

class M3uImporter(private val ctx: Context, private val client: OkHttpClient = OkHttpClient()) {

  suspend fun importFromUrl(url: String) = withContext(Dispatchers.IO) {
    val dao = NeostreamDb.get(ctx).dao()
    val resp = client.newCall(Request.Builder().url(url).build()).execute()
    resp.body!!.source().buffer().use { buf ->
      val batch = ArrayList<ChannelEntity>(4000)
      var pendingTitle: String? = null
      var pendingGroup: String? = null
      var pendingHasEpg = false

      fun push(urlLine: String) {
        val title = pendingTitle ?: "Unknown"
        val group = pendingGroup
        val kind = detectKind(title, group)
        val qual = detectQuality("$title ${group.orEmpty()}")
        val country = detectCountry(title, group)
        batch += ChannelEntity(
          title = title, url = urlLine.trim(),
          groupName = group, countryTag = country, quality = qual, kind = kind,
          hasEpg = pendingHasEpg, isNew = true
        )
        pendingTitle = null; pendingGroup = null; pendingHasEpg = false
        if (batch.size >= 4000) { dao.insertAll(batch.toList()); batch.clear() }
      }

      while (true) {
        val line = buf.readUtf8Line() ?: break
        val l = line.trim()
        if (l.isEmpty() || l.startsWith("#EXTM3U")) continue
        if (l.startsWith("#EXTGRP:")) { pendingGroup = l.substringAfter(":", "").ifBlank { pendingGroup }; continue }
        if (l.startsWith("#EXTINF")) {
          // attrs
          val title = l.substringAfter(",", "Unknown").trim()
          pendingTitle = title
          pendingHasEpg = l.contains("tvg-id=")
          val grp = Regex("""group-title="([^"]*)"""").find(l)?.groupValues?.get(1)
          if (!grp.isNullOrBlank()) pendingGroup = grp
          continue
        }
        if (l.startsWith("#")) continue
        if (l.startsWith("http")) { push(l) }
      }
      if (batch.isNotEmpty()) dao.insertAll(batch)
    }
  }
}
