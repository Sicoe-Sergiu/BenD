package com.example.bend.view.ui_state

data class LoginUiState (
    var email:String = "",
    var password:String = "",

    var emailError: Boolean = true,
    var passwordError: Boolean = true,
)