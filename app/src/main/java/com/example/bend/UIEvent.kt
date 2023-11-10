package com.example.bend

sealed class UIEvent{

    data class FirstNameChanged(val first_name:String) : UIEvent()
    data class LastNameChanged(val last_name:String) : UIEvent()
    data class UsernameChanged(val username:String) : UIEvent()
    data class EmailChanged(val email:String) : UIEvent()
    data class PasswordChanged(val password:String) : UIEvent()
    data class AccountTypeChanged(val account_type:String) : UIEvent()
//    conditionals
    data class PhoneChanged(val phone:String) : UIEvent()
    data class StageNameChanged(val stage_name:String) : UIEvent()

    object RegisterButtonClicked :UIEvent()
}
