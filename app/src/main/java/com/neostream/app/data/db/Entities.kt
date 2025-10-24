package com.neostream.app.data.db

import androidx.room.*

@Entity(tableName = "channels", indices = [Index("groupName"), Index("quality"), Index("kind")])
data class ChannelEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val url: String,
  val groupName: String?,
  val countryTag: String?,   // FR/AR/ES/IT/US/TR…
  val quality: String?,      // uhd|fhd|hd|hevc
  val kind: String,          // live|series|movie|radio
  val hasEpg: Boolean = false,
  val isNew: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
