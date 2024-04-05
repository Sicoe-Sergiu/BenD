package com.example.bend

import com.google.firebase.messaging.FirebaseMessagingService

class PushNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        TODO: update user with new token
    }
}