package com.example.bend.model

import java.sql.Timestamp
import java.util.UUID

data class Repost (
    val uuid: String,
    val userUUID:String,
    val eventUUID: String,
    val timestamp: Long
){
    constructor() : this("", "", "", System.currentTimeMillis() )
}
