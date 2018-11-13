package io.github.e_vent.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.e_vent.vo.Event

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts : List<Event>)

    @Query("SELECT * FROM posts ORDER BY indexInResponse ASC")
    fun posts() : DataSource.Factory<Int, Event>

    @Query("DELETE FROM posts")
    fun delete()

    @Query("SELECT MAX(indexInResponse) + 1 FROM posts")
    fun getNextIndex() : Int
}