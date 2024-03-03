package com.example.bend.model

import java.util.UUID

data class Event(
    val uuid: String,
    val founderUUID: String,
    val posterDownloadLink: String,
    val entranceFee: Int,
    val location: String,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val artistStageNames: List<String>,
    val creationTimestamp: Long
){
    constructor() : this(
        UUID.randomUUID().toString(),
        "",
        "",
        0,
        "",
        "",
        "",
        "",
        "",
        emptyList(),
        System.currentTimeMillis()
    )
}