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
    val rating: Double,

//    val past_events: List<Event>,
//    val future_events: List<Event>,

//    val followers: List<User>,
//    val following: List<User>
)