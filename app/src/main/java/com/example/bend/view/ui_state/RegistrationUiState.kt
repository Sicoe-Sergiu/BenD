package com.example.bend.view.ui_state

data class RegistrationUiState(

    var uuid: String = "",
    var first_name:String = "",
    var last_name:String = "",
    var username:String = "",
    var email:String = "",
    var password:String = "",
    var account_type:String = "",
    var photoUri: String = "",

//    conditionals
    var phone:String = "",
    var stage_name:String = "",

// errors validation

    var first_name_error: Boolean = true,
    var last_name_error: Boolean = true,
    var username_error: Boolean = true,
    var password_error: Boolean = true,
    var email_error: Boolean = true,
    var photoError: Boolean = true,
//
    var phone_error: Boolean = true,
    var stage_name_error: Boolean = true,
)
