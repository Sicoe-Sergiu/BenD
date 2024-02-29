package com.example.bend.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
data class Artist(
    val uuid: UUID,
    val name: String,
    val genre: String,
    val firstName: String,
    val username: String,
    val lastName: String,
    val stageName: String
) {
    constructor() : this(UUID.randomUUID(), "", "", "", "", "", "")
}
