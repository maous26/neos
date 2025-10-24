package com.neostream.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChannelEntity::class], version = 1, exportSchema = false)
abstract class NeostreamDb : RoomDatabase() {
  abstract fun dao(): ChannelDao

  companion object {
    @Volatile private var I: NeostreamDb? = null
    fun get(ctx: Context) = I ?: synchronized(this) {
      I ?: Room.databaseBuilder(ctx, NeostreamDb::class.java, "neostream.db").fallbackToDestructiveMigration().build().also { I = it }
    }
  }
}
