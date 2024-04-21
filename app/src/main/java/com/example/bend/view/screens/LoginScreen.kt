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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.view.components.BoldTextComponent
import com.example.bend.view.components.ClickableLoginRegisterText
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.MyPasswordFieldComponent
import com.example.bend.view.components.MyTextFieldComponent
import com.example.bend.view.components.NormalTextComponent
import com.example.bend.model.events.LoginUIEvent
import com.example.bend.view.theme.PrimaryText
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController:NavController,
    loginViewModel: LoginViewModel = viewModel()
) {

    val context = LocalContext.current
    val errorMessage = loginViewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                loginViewModel.clearError()
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
                NormalTextComponent(value = "Hey there,")
                BoldTextComponent(value = "Welcome Back !")
                Spacer(modifier = Modifier.height(100.dp))
                MyTextFieldComponent(
                    labelValue = "Email",
                    onTextSelected = {
                        loginViewModel.onEvent(LoginUIEvent.EmailChanged(it))
                    },
                    errorStatus = loginViewModel.loginUiState.value.emailError
                )
                MyPasswordFieldComponent(
                    labelValue = "Password",
                    onTextSelected = {
                        loginViewModel.onEvent(LoginUIEvent.PasswordChanged(it))
                    },
                    errorStatus = loginViewModel.loginUiState.value.passwordError
                )
                Spacer(modifier = Modifier.height(10.dp))
                ClickableLoginRegisterText(
                    onTextSelected = { navController.navigate(Constants.NAVIGATION_FORGOT_PASS_PAGE) },
                    initial_text = "",
                    action_text = "Forgot your password?",
                    span_style = SpanStyle(
                        color = PrimaryText,
                        textDecoration = TextDecoration.Underline
                    )
                )
                Spacer(modifier = Modifier.height(150.dp))
                MyButtonComponent(
                    value = "Login",
                    onButtonClicked = {
                                      loginViewModel.onEvent(LoginUIEvent.LoginButtonClicked(navController))
                    },
                    isEnabled = loginViewModel.passwordValidationsPassed.value && loginViewModel.emailValidationPassed.value
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClickableLoginRegisterText(
                    onTextSelected = { navController.navigate(Constants.NAVIGATION_REGISTER_PAGE) },
                    initial_text = "Don't have an account yet? ",
                    action_text = "Register",
                    span_style = SpanStyle(color = green, textDecoration = TextDecoration.Underline)
                )


            }
        }
        if(loginViewModel.signInInProgress.value)
            CircularProgressIndicator()
    }
}