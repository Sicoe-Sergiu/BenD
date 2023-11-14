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

    var first_name_error: Boolean = false,
    var last_name_error: Boolean = false,
    var username_error: Boolean = false,
    var password_error: Boolean = false,
    var email_error: Boolean = false,
//
    var phone_error: Boolean = false,
    var stage_name_error: Boolean = false,
)
