package com.example.bend.model

import java.util.UUID

data class User(
    val uuid: String,
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