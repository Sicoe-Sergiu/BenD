package com.example.bend.model

data class Review (
    val uuid: String,
    val writerUUID: String,
    val userUUID: String,
    val eventUUID: String,
    val reviewText: String,
    val creationTimestamp: Long

){
    constructor(): this("","","","","", System.currentTimeMillis())
}