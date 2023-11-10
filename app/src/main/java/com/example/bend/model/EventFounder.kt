package com.example.bend.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_founder", indices = [Index(value = ["uuid"], unique = true)])
data class EventFounder(
    @PrimaryKey() val uuid: UUID,

    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "first_name") val first_name: String,
    @ColumnInfo(name = "last_name") val last_name: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,

    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "rating") val rating: Float,

)