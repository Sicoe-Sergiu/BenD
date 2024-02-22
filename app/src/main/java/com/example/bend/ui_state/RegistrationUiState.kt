package com.example.bend.ui_state

data class RegistrationUiState(
    var first_name:String = "",
    var last_name:String = "",
    var username:String = "",
    var email:String = "",
    var password:String = "",
    var account_type:String = "",

//    conditionals
    var phone:String = "",
    var stage_name:String = "",
//

    var first_name_error: Boolean = true,
    var last_name_error: Boolean = true,
    var username_error: Boolean = true,
    var password_error: Boolean = true,
    var email_error: Boolean = true,
//
    var phone_error: Boolean = true,
    var stage_name_error: Boolean = true,
)
