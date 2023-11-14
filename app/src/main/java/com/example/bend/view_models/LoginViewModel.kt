package com.example.bend.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.events.LoginUIEvent
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.ui_state.LoginUiState
import com.example.bend.ui_state.RegistrationUiState

class LoginViewModel: ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var login_ui_state = mutableStateOf(LoginUiState())
    var all_validations_passed = mutableStateOf(false)

    var sign_in_in_progress = mutableStateOf(false)

    lateinit var navController: NavController

    fun onEvent(event: LoginUIEvent) {
        validateDataWithRules()
        when (event) {
            is LoginUIEvent.EmailChanged -> {
                login_ui_state.value = login_ui_state.value.copy(email = event.email)
                printState()
            }

            is LoginUIEvent.PasswordChanged -> {
                login_ui_state.value = login_ui_state.value.copy(password = event.password)
                printState()
            }

            is LoginUIEvent.LoginButtonClicked -> {
                navController = event.navController
                signIn(navController)
            }
        }
    }
}

    private fun printState() {
        TODO("Not yet implemented")
    }

    private fun signIn(navController: NavController) {
        TODO("Not yet implemented")
    }

    private fun validateDataWithRules() {
        TODO("Not yet implemented")
    }
