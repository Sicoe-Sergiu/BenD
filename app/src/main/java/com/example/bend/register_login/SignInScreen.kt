package com.example.bend.register_login

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.components.BoldTextComponent
import com.example.bend.components.ClickableLoginRegisterText
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyPasswordFieldComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.components.NormalTextComponent
import com.example.bend.events.LoginUIEvent
import com.example.bend.ui.theme.PrimaryText
import com.example.bend.ui.theme.green
import com.example.bend.view_models.LoginViewModel

@Composable
fun SignInScreen(
    navController:NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
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
                    errorStatus = loginViewModel.login_ui_state.value.email_error
                )
                MyPasswordFieldComponent(
                    labelValue = "Password",
                    onTextSelected = {
                        loginViewModel.onEvent(LoginUIEvent.PasswordChanged(it))
                    },
                    errorStatus = loginViewModel.login_ui_state.value.password_error
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
                    isEnabled = loginViewModel.password_validations_passed.value && loginViewModel.email_validation_passed.value
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
        if(loginViewModel.sign_in_in_progress.value)
            CircularProgressIndicator()
    }
}