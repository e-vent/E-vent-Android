package io.github.e_vent.vo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class ClientEvent(
        @PrimaryKey
        val id: Int,
        val name: String,
        val desc: String,
        val bg: String
)
