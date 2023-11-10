package com.example.bend.register_login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bend.UIEvent
import com.example.bend.components.NormalTextComponent
import com.example.bend.components.BoldTextComponent
import com.example.bend.components.ClickableLoginRegisterText
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyDropDownMenuComponent
import com.example.bend.components.MyPasswordFieldComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.view_models.LoginViewModel
import androidx.compose.ui.Alignment
@Composable
fun SignUpScreen(
    loginViewModel: LoginViewModel = viewModel()
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
            NormalTextComponent(value = "Hey there,", )
            BoldTextComponent(value = "Create an account")
            Spacer(modifier = Modifier.height(25.dp))


            MyTextFieldComponent(
                labelValue = "First Name",
                onTextSelected = { loginViewModel.onEvent(UIEvent.FirstNameChanged(it)) })
            MyTextFieldComponent(
                labelValue = "Last Name",
                onTextSelected = { loginViewModel.onEvent(UIEvent.LastNameChanged(it)) })
            MyTextFieldComponent(
                labelValue = "Username",
                onTextSelected = { loginViewModel.onEvent(UIEvent.UsernameChanged(it)) })
            MyTextFieldComponent(
                labelValue = "Email",
                onTextSelected = { loginViewModel.onEvent(UIEvent.EmailChanged(it)) })
            MyPasswordFieldComponent(
                labelValue = "Password",
                onTextSelected = { loginViewModel.onEvent(UIEvent.PasswordChanged(it)) })
            MyDropDownMenuComponent(
                label_value = "Account Type",
                options = arrayOf("Regular Account", "Artist account", "Event Organizer account"),
                onTextSelected = { loginViewModel.onEvent(UIEvent.AccountTypeChanged(it)) })

            if (loginViewModel.registration_ui_state.value.account_type == "Artist account") {
                MyTextFieldComponent(
                    "Stage Name",
                    onTextSelected = { loginViewModel.onEvent(UIEvent.StageNameChanged(it)) })
            }
            if (loginViewModel.registration_ui_state.value.account_type == "Event Organizer account") {
                MyTextFieldComponent(
                    "Phone",
                    onTextSelected = { loginViewModel.onEvent(UIEvent.PhoneChanged(it)) })
            }

        }
        Column{
            Spacer(modifier = Modifier.height(620.dp))
            MyButtonComponent(
                value = "Register",
                onButtonClicked = {
                    loginViewModel.onEvent(UIEvent.RegisterButtonClicked)
            })
            Spacer(modifier = Modifier.height(8.dp))
            ClickableLoginRegisterText(
                onTextSelected = {},
                initial_text = "Already have an account? ",
                action_text = "Login")
        }
    }
}

@Preview
@Composable
fun PreviewSignUpScreen(){
    SignUpScreen()
}