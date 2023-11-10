package com.example.bend.register_login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bend.components.BoldTextComponent
import com.example.bend.components.ClickableLoginRegisterText
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyClickableText
import com.example.bend.components.MyDropDownMenuComponent
import com.example.bend.components.MyPasswordFieldComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.components.NormalTextComponent

@Composable
fun SignInScreen(

) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)

    ){
        val currentUsername = remember {
            mutableStateOf("")
        }
        val currentPassword= remember {
            mutableStateOf("")
        }


        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NormalTextComponent(value = "Hey there,")
            BoldTextComponent(value = "Welcome Back !")
            Spacer(modifier = Modifier.height(100.dp))
            MyTextFieldComponent("Email/Username", onTextSelected = {})
            MyPasswordFieldComponent("Password", onTextSelected = {})
            Spacer(modifier = Modifier.height(10.dp))
            MyClickableText(value = "Forgot your password?")
            Spacer(modifier = Modifier.height(150.dp))
            MyButtonComponent(value = "Login", onButtonClicked = {})
            Spacer(modifier = Modifier.height(8.dp))
            ClickableLoginRegisterText(onTextSelected = {}, "Don't have an account yet? ","Register")




        }
    }
}

@Preview
@Composable
fun PreviewSisgnUpScreen(){
    SignInScreen()
}