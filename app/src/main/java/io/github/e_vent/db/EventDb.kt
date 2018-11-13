package io.github.e_vent.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import io.github.e_vent.vo.Event

/**
 * Database schema used by the DbRedditPostRepository
 */
@Database(
        entities = arrayOf(Event::class),
        version = 1,
        exportSchema = false
)
abstract class EventDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory : Boolean): EventDb {
            val databaseBuilder = if(useInMemory) {
                Room.inMemoryDatabaseBuilder(context, EventDb::class.java)
            } else {
                Room.databaseBuilder(context, EventDb::class.java, "reddit.db")
            }
            return databaseBuilder
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun posts(): EventDao
}