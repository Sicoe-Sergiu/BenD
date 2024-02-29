package com.example.bend.model

import androidx.room.ColumnInfo
import java.util.UUID
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

data class User(
    val uuid: UUID,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,

//    val eventsAttending: List<Event>,
//    val followedArtists: List<Artist>,
//    val followed_organizers: List<EventOrganizer>,
//    val interests: List<String>,
//    val favorite_genres: List<String>,
//    val attendance_history: List<Event>
)