package com.example.bend.events

import androidx.navigation.NavController

sealed class LoginUIEvent {
    data class EmailChanged(val email:String) : LoginUIEvent()
    data class PasswordChanged(val password:String) : LoginUIEvent()

    data class LoginButtonClicked(val navController: NavController) : LoginUIEvent()

}