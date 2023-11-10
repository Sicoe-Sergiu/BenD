package com.example.bend.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "event_artist")
data class Event_Artist (
    @PrimaryKey() val uuid: UUID,

    @ColumnInfo(name = "artist_uuid") val artist_uuid: UUID,
    @ColumnInfo(name = "event_uuid") val event_uuid: UUID,
)