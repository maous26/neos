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

class M3uImporter(private val ctx: Context, private val client: OkHttpClient = OkHttpClient()) {

  /**
   * Télécharge et importe une playlist M3U.
   * - Vide la table existante
   * - Retourne le nombre d'entrées insérées
   * - Lève une exception si la réponse HTTP n'est pas 2xx ou si 0 chaînes détectées
   */
  suspend fun importFromUrl(url: String): Int = withContext(Dispatchers.IO) {
    val dao = NeostreamDb.get(ctx).dao()

    val req = Request.Builder()
      .url(url)
      .header("User-Agent", "Mozilla/5.0 (Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0 Safari/537.36")
      .build()

    val resp = client.newCall(req).execute()
    if (!resp.isSuccessful) {
      val code = resp.code
      try { resp.close() } catch (_: Throwable) {}
      throw IllegalStateException("HTTP $code sur le téléchargement M3U")
    }

    val body = resp.body ?: run { resp.close(); throw IllegalStateException("Réponse vide") }
    val buf = body.source().buffer()

    // Reset previous content to avoid mixing old/new
    dao.clear()

    val batch = ArrayList<ChannelEntity>(4000)
    var pendingTitle: String? = null
    var pendingGroup: String? = null
    var pendingHasEpg = false
    var inserted = 0

    suspend fun flush() {
      if (batch.isNotEmpty()) {
        dao.insertAll(batch.toList())
        inserted += batch.size
        batch.clear()
      }
    }

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
    }

    var sawHeader = false

    try {
      while (true) {
        val line = buf.readUtf8Line() ?: break
        val l = line.trim()
        if (l.isEmpty()) continue
        if (l.startsWith("#EXTM3U")) { sawHeader = true; continue }
        if (l.startsWith("#EXTGRP:")) { pendingGroup = l.substringAfter(":", "").ifBlank { pendingGroup }; continue }
        if (l.startsWith("#EXTINF")) {
          val title = l.substringAfter(",", "Unknown").trim()
          pendingTitle = title
          pendingHasEpg = l.contains("tvg-id=")
          val grp = Regex("""group-title="([^"]*)"""").find(l)?.groupValues?.get(1)
          if (!grp.isNullOrBlank()) pendingGroup = grp
          continue
        }
        if (l.startsWith("#")) continue
        if (l.startsWith("http")) {
          push(l)
          if (batch.size >= 4000) { flush() }
        }
      }
      flush()
    } finally {
      try { buf.close() } catch (_: Throwable) {}
      try { body.close() } catch (_: Throwable) {}
      try { resp.close() } catch (_: Throwable) {}
    }

    if (!sawHeader && inserted == 0) {
      throw IllegalStateException("Le contenu obtenu n'est pas une M3U valide. Vérifiez l'URL/identifiants.")
    }
    if (inserted == 0) {
      throw IllegalStateException("Aucune chaîne détectée dans la playlist.")
    }
    inserted
  }
}
