package com.example.bend.view.ui_state

data class LoginUiState (
    var email:String = "",
    var password:String = "",

    var email_error: Boolean = true,
    var password_error: Boolean = true,
)