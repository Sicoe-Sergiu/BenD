package com.example.bend.model

data class User(
    val uuid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePhotoURL:String
){
    constructor() : this("", "", "", "", "","")
}