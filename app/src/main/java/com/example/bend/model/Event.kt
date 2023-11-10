package com.example.bend.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Time
import java.util.UUID

@Entity(tableName = "event", indices = [Index(value = ["uuid"], unique = true)])
class Event (
    @PrimaryKey() val uuid: UUID,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "entrance_fee") val entrance_fee: Int,
    @ColumnInfo(name = "start_time") val start_time: Long,
    @ColumnInfo(name = "end_time") val end_time: Long,
    @ColumnInfo(name = "organizer_uuid") val organizer_uuid: UUID,

    @Ignore
    val artists_uuids: List<UUID>?
){
    constructor(uuid: UUID, name: String, location: String, entrance_fee: Int, start_time: Long, end_time: Long,organizer_uuid: UUID) : this (uuid, name, location, entrance_fee, start_time, end_time, organizer_uuid, null)
}