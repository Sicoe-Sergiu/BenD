package com.example.bend.model

data class User(
    val uuid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePhotoURL:String

//    val followedArtists: List<Artist>,
//    val followed_organizers: List<EventOrganizer>,
//    val interests: List<String>,
//    val favorite_genres: List<String>,
){
    constructor() : this("", "", "", "", "","")
}