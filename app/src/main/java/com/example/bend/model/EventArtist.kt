package com.example.bend.model

import java.util.UUID

data class EventArtist(
    val artistUUID: String,
    val eventUUID:String
){
    constructor() : this("", "")
}
