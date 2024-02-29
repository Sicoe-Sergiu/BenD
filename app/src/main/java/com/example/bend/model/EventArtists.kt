package com.example.bend.model

import java.util.UUID

data class EventArtists (
    val uuid: UUID,
    val eventUUID: UUID,
    val artistUUID: UUID,
)