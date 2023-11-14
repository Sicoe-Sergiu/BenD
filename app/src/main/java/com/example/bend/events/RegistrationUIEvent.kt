package com.example.bend.events

import androidx.navigation.NavController

sealed class RegistrationUIEvent{

    data class FirstNameChanged(val first_name:String) : RegistrationUIEvent()
    data class LastNameChanged(val last_name:String) : RegistrationUIEvent()
    data class UsernameChanged(val username:String) : RegistrationUIEvent()
    data class EmailChanged(val email:String) : RegistrationUIEvent()
    data class PasswordChanged(val password:String) : RegistrationUIEvent()
    data class AccountTypeChanged(val account_type:String) : RegistrationUIEvent()
//    conditionals
    data class PhoneChanged(val phone:String) : RegistrationUIEvent()
    data class StageNameChanged(val stage_name:String) : RegistrationUIEvent()
//

    data class RegisterButtonClicked(val navController: NavController) : RegistrationUIEvent()
    data class LogOutButtonClicked(val navController: NavController) : RegistrationUIEvent()
}
