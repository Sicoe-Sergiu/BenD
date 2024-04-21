package com.example.bend.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.view.components.BoldTextComponent
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.MyTextFieldComponent
import com.example.bend.view.components.NormalTextComponent
import com.example.bend.model.events.ForgotPassUIEvent
import com.example.bend.viewmodel.ForgotPasswordViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    resetPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    val context = LocalContext.current
    val errorMessage = resetPasswordViewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                resetPasswordViewModel.clearError()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(28.dp)

        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                BoldTextComponent(value = "Find your password")
                Spacer(modifier = Modifier.height(100.dp))
                NormalTextComponent(value = "Please enter your email:")
                MyTextFieldComponent(
                    labelValue = "Email",
                    onTextSelected = {
                        resetPasswordViewModel.onEvent(ForgotPassUIEvent.EmailChanged(it))
                    },
                    errorStatus = resetPasswordViewModel.forgotPassUiState.value.emailError
                )
                Spacer(modifier = Modifier.height(25.dp))

                MyButtonComponent(
                    value = "Reset Password",
                    onButtonClicked = {
                        resetPasswordViewModel.onEvent(ForgotPassUIEvent.ResetButtonClicked(navController))
                    },
                    isEnabled = resetPasswordViewModel.emailValidationPassed.value
                )
                Spacer(modifier = Modifier.height(8.dp))

            }
        }
        if(resetPasswordViewModel.forgotPassInProgress.value)
            CircularProgressIndicator()
    }
}