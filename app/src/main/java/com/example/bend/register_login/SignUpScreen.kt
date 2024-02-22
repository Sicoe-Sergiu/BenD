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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.components.NormalTextComponent
import com.example.bend.components.BoldTextComponent
import com.example.bend.components.ClickableLoginRegisterText
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyDropDownMenuComponent
import com.example.bend.components.MyPasswordFieldComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.view_models.RegisterViewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.ui.theme.green

@Composable
fun SignUpScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
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
                    onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.FirstNameChanged(it)) },
                    errorStatus = registerViewModel.registration_ui_state.value.first_name_error
                )
                MyTextFieldComponent(
                    labelValue = "Last Name",
                    onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.LastNameChanged(it)) },
                    errorStatus = registerViewModel.registration_ui_state.value.last_name_error
                )
                MyTextFieldComponent(
                    labelValue = "Username",
                    onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.UsernameChanged(it)) },
                    errorStatus = registerViewModel.registration_ui_state.value.username_error
                )
                MyTextFieldComponent(
                    labelValue = "Email",
                    onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.EmailChanged(it)) },
                    errorStatus = registerViewModel.registration_ui_state.value.email_error
                )
                MyPasswordFieldComponent(
                    labelValue = "Password",
                    onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.PasswordChanged(it)) },
                    errorStatus = registerViewModel.registration_ui_state.value.password_error
                )
                MyDropDownMenuComponent(
                    label_value = "Account Type",
                    options = arrayOf("Regular Account", "Artist account", "Event Organizer account"),
                    onTextSelected = {
                        registerViewModel.onEvent(RegistrationUIEvent.AccountTypeChanged(it))
                    })

                if (registerViewModel.registration_ui_state.value.account_type == "Artist account") {
                    MyTextFieldComponent(
                        "Stage Name",
                        onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.StageNameChanged(it)) },
                        errorStatus = registerViewModel.registration_ui_state.value.stage_name_error
                    )
                }
                if (registerViewModel.registration_ui_state.value.account_type == "Event Organizer account") {
                    MyTextFieldComponent(
                        "Phone",
                        onTextSelected = { registerViewModel.onEvent(RegistrationUIEvent.PhoneChanged(it)) },
                        errorStatus = registerViewModel.registration_ui_state.value.password_error
                    )
                }

            }
            Column{
                Spacer(modifier = Modifier.height(620.dp))
                MyButtonComponent(
                    value = "Register",
                    onButtonClicked = {
                        registerViewModel.onEvent(RegistrationUIEvent.RegisterButtonClicked(navController))
                    },
                    is_enabled = registerViewModel.first_name_validations_passed.value &&
                            registerViewModel.last_name_validations_passed.value &&
                            registerViewModel.username_validations_passed.value &&
                            registerViewModel.email_validations_passed.value &&
                            registerViewModel.password_validations_passed.value &&
                            (
                                    (registerViewModel.registration_ui_state.value.account_type == "Event Organizer account" && registerViewModel.phone_validations_passed.value) ||
                                            (registerViewModel.registration_ui_state.value.account_type == "Artist account" && registerViewModel.stage_name_validations_passed.value) ||
                                            registerViewModel.registration_ui_state.value.account_type == "Regular Account"
                                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClickableLoginRegisterText(
                    onTextSelected = { navController.navigate(Constants.NAVIGATION_LOGIN_PAGE)},
                    initial_text = "Already have an account? ",
                    action_text = "Login",
                    span_style = SpanStyle(color = green, textDecoration = TextDecoration.Underline)
                )
            }
        }

        if(registerViewModel.sign_up_in_progress.value)
            CircularProgressIndicator()

    }

}
