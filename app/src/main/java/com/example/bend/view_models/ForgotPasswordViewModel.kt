package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.events.ForgotPassUIEvent
import com.example.bend.register_login.Validator
import com.example.bend.ui_state.ForgotPasswordUiState
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel: ViewModel() {
    private val TAG = ForgotPasswordViewModel::class.simpleName
    var forgot_pass_ui_state = mutableStateOf(ForgotPasswordUiState())

    var email_validation_passed = mutableStateOf(false)

    var forgot_pass_in_progress = mutableStateOf(false)

    lateinit var navController: NavController

    fun onEvent(event: ForgotPassUIEvent) {
        when (event) {
            is ForgotPassUIEvent.EmailChanged -> {
                forgot_pass_ui_state.value = forgot_pass_ui_state.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is ForgotPassUIEvent.ResetButtonClicked -> {
                validateEmailDataWithRules()
                navController = event.navController
                if(email_validation_passed.value)
                    resetPass(navController)
            }
        }
    }
    private fun validateEmailDataWithRules(){
        val email_result = Validator.validateEmail(
            email = forgot_pass_ui_state.value.email
        )
        forgot_pass_ui_state.value = forgot_pass_ui_state.value.copy(
            email_error = email_result.status,
        )
        email_validation_passed.value = email_result.status
    }
    private fun printState() {
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,forgot_pass_ui_state.toString())
    }

    private fun resetPass(navController: NavController) {

        val email = forgot_pass_ui_state.value.email

        FirebaseAuth
            .getInstance()
            .sendPasswordResetEmail(email)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    forgot_pass_in_progress.value = false
                    navController.popBackStack()
                }
                Log.d(TAG,"InCompleteListener")
                Log.d(TAG,"isSuccesfull  = ${it.isSuccessful}")
            }
            .addOnFailureListener{
                Log.d(TAG,"InFailureListener")
                Log.d(TAG,"Exception = ${it.message}")
                Log.d(TAG,"Exception = ${it.localizedMessage}")
                forgot_pass_in_progress.value = false
            }
    }
}

