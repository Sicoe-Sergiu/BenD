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
)
