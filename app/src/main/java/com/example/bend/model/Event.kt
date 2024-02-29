package com.example.bend.model

import java.util.UUID

data class Event(
    val uuid: UUID,
    val organizerUUID: String,
    val posterDownloadLink: String,
    val entranceFee: Int,
    val location: String,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val artistStageNames: List<String>
)