package com.example.bend.model

data class Notification (
    val uuid: String,
    val fromUserUUID: String,
    val toUserUUID: String,
    val eventUUID:String,
    val text: String,
    val timestamp: Long,
    val sensitive:Boolean,
    val seen: Boolean = false
){
    constructor(): this("","","","","", System.currentTimeMillis(), false)
}