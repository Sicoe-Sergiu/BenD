package com.example.bend.model

import java.util.UUID

data class EventFeedback(
    val uuid: UUID,
    val eventUUID: UUID,
    val userUUID: UUID,
    val organizerRating: Double,
    val artistRating: Double,
    val review: String
)