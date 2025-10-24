package com.neostream.app.data.db

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface ChannelDao {
  @Query("DELETE FROM channels")
  suspend fun clear()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<ChannelEntity>)

  @Query("SELECT * FROM channels WHERE kind=:kind AND (:groupName IS NULL OR groupName=:groupName) AND (:quality IS NULL OR quality=:quality) AND (:country IS NULL OR countryTag=:country) ORDER BY isNew DESC, createdAt DESC, title ASC")
  fun page(kind: String, groupName: String?, quality: String?, country: String?): PagingSource<Int, ChannelEntity>

  @Query("SELECT groupName, COUNT(*) AS cnt FROM channels WHERE kind=:kind GROUP BY groupName ORDER BY cnt DESC LIMIT 50")
  suspend fun topGroups(kind: String): List<TopGroup>

  @Query("SELECT * FROM channels WHERE kind='live' AND (countryTag='FR' OR groupName LIKE 'FRANCE%') ORDER BY quality DESC, title ASC")
  fun pageLiveFr(): PagingSource<Int, ChannelEntity>
}

data class TopGroup(val groupName: String?, val cnt: Int)
