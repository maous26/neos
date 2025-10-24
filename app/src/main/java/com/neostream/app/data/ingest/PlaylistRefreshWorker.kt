package com.neostream.app.data.ingest

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neostream.app.data.db.NeostreamDb
import com.neostream.app.data.prefs.SecurePrefs

class PlaylistRefreshWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result = try {
    val url = SecurePrefs(applicationContext).getPlaylistUrl() ?: return Result.success()
    // reset 'isNew' flags
    NeostreamDb.get(applicationContext).dao().clear()
    M3uImporter(applicationContext).importFromUrl(url)
    Result.success()
  } catch (t: Throwable) { Result.retry() }
}
