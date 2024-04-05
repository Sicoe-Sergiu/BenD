package com.example.bend.model

data class Notification (
    val uuid: String,
    val text: String,
    val eventUUID:String,
    val timestamp: Long

){
    constructor(): this("","","", System.currentTimeMillis())
}