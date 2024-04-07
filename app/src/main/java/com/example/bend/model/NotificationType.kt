package com.example.bend.model

enum class NotificationType(val displayName: String) {
    NEW_FOLLOWER("started following you."),

    NEW_EVENT("added a new Event."),
    EDITED_EVENT("has modified an event you are attending."),
    DELETED_EVENT("has deleted an event you wanted to attend."),

    EVENT_INVITE("Event Invite"),
    FOLLOWER("New Follower"),
    LIKED_POST("Liked Your Post");

    fun displayNotification(): String {
        return "You have a new $displayName."
    }
}