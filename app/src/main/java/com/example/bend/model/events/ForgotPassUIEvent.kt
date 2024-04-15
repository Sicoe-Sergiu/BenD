package com.example.bend.model.events

import androidx.navigation.NavController

sealed class ForgotPassUIEvent {
    data class EmailChanged(val email:String) : ForgotPassUIEvent()

    data class ResetButtonClicked(val navController: NavController) : ForgotPassUIEvent()
}