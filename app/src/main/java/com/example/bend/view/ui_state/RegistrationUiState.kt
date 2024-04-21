package com.example.bend.view.ui_state

data class RegistrationUiState(

    var uuid: String = "",
    var firstName:String = "",
    var lastName:String = "",
    var username:String = "",
    var email:String = "",
    var password:String = "",
    var accountType:String = "",
    var photoUri: String = "",

//    conditionals
    var phone:String = "",
    var stageName:String = "",

// errors validation

    var firstNameError: Boolean = true,
    var lastNameError: Boolean = true,
    var userNameError: Boolean = true,
    var passwordError: Boolean = true,
    var emailError: Boolean = true,
    var photoError: Boolean = true,
//
    var phoneError: Boolean = true,
    var stageNameError: Boolean = true,
)
