package com.example.bend.model

data class UserEvent (
    val uuid: String,
    val user:User,
    val event:Event
){
    constructor() : this("", User(), Event())
}