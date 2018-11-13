package io.github.e_vent.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.e_vent.vo.ClientEvent

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts : List<ClientEvent>)

    @Query("SELECT * FROM events ORDER BY id ASC")
    fun posts() : DataSource.Factory<Int, ClientEvent>

    @Query("DELETE FROM events")
    fun delete()
}
