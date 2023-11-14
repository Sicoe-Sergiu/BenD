package com.example.bend.ui_state

data class LoginUiState (
    var email:String = "",
    var password:String = "",

    var email_error: Boolean = false,
    var password_error: Boolean = false,
)