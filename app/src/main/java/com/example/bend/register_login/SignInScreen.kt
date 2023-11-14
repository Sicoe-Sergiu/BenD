package com.example.bend.register_login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.components.BoldTextComponent
import com.example.bend.components.ClickableLoginRegisterText
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyPasswordFieldComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.components.NormalTextComponent
import com.example.bend.ui.theme.PrimaryText
import com.example.bend.ui.theme.green

@Composable
fun SignInScreen(
    navController:NavController
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)

    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NormalTextComponent(value = "Hey there,")
            BoldTextComponent(value = "Welcome Back !")
            Spacer(modifier = Modifier.height(100.dp))
            MyTextFieldComponent("Email/Username", onTextSelected = {})
            MyPasswordFieldComponent("Password", onTextSelected = {})
            Spacer(modifier = Modifier.height(10.dp))
            ClickableLoginRegisterText(
                onTextSelected = { navController.navigate(Constants.NAVIGATION_FORGOT_PASS_PAGE)},
                initial_text = "",
                action_text = "Forgot your password?",
                span_style = SpanStyle(color = PrimaryText, textDecoration = TextDecoration.Underline)
            )
            Spacer(modifier = Modifier.height(150.dp))
            MyButtonComponent(value = "Login", onButtonClicked = {})
            Spacer(modifier = Modifier.height(8.dp))
            ClickableLoginRegisterText(
                onTextSelected = { navController.navigate(Constants.NAVIGATION_REGISTER_PAGE)},
                initial_text = "Don't have an account yet? ",
                action_text = "Register",
                span_style = SpanStyle(color = green, textDecoration = TextDecoration.Underline)
            )





        }
    }
}