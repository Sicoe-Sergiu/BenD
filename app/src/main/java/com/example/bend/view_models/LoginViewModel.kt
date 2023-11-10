package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bend.UIEvent
import com.example.bend.ui_state.RegistrationUiState

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var registration_ui_state = mutableStateOf(RegistrationUiState())

    fun onEvent(event: UIEvent){
        when(event){
            is UIEvent.FirstNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(first_name = event.first_name)
                printState()
            }
            is UIEvent.LastNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(last_name = event.last_name)
                printState()
            }
            is UIEvent.UsernameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(username = event.username)
                printState()
            }
            is UIEvent.EmailChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(email = event.email)
                printState()
            }
            is UIEvent.PasswordChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(password = event.password)
                printState()
            }
            is UIEvent.AccountTypeChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(account_type = event.account_type)
                printState()
            }

//            conditionals
            is UIEvent.PhoneChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(phone = event.phone)
                printState()
            }
            is UIEvent.StageNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(stage_name = event.stage_name)
                printState()
            }

            UIEvent.RegisterButtonClicked -> {
                signUp()
            }
        }
    }

    private fun signUp(){
        Log.d(TAG,"Inside_signUp")
        printState()
    }
    private fun printState(){
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,registration_ui_state.toString())
    }
}