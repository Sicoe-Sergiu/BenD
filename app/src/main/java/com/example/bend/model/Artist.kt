package com.example.bend.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
data class Artist(
    val uuid: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val stageName: String,
    val profilePhotoURL:String
) {
    constructor() : this("", "", "", "", "", "","")
}
