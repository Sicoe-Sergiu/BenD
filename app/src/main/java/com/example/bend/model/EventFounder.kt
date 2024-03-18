package com.example.bend.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

data class EventFounder(
    val uuid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val profilePhotoURL:String,

    val rating:Float,
    val ratingsNumber:Int
){
    constructor() : this("", "", "", "", "", "","", -1f, -1)
}