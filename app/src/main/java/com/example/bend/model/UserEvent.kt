package com.example.bend.model

data class UserEvent (
    val uuid: String,
    val userUUID:String,
    val eventUUID:String
){
    constructor() : this("", "", "")
}