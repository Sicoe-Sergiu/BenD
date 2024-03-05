package com.example.bend.model

import java.util.UUID

data class Followers(
    val uuid: String,
    val userUUID: String,
    val followedUserUUID: String,

){
    constructor() : this("", "", "")
}