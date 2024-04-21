package com.example.bend.view.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bend.model.events.RegistrationUIEvent
import com.example.bend.view.components.NormalTextComponent
import com.example.bend.view.components.BoldTextComponent
import com.example.bend.view.components.ClickableLoginRegisterText
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.MyDropDownMenuComponent
import com.example.bend.view.components.MyPasswordFieldComponent
import com.example.bend.view.components.MyTextFieldComponent
import com.example.bend.viewmodel.RegisterViewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.theme.green

@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel(),
    editMode: Boolean = false,
    userUUID: String? = null
) {
    val context = LocalContext.current
    val errorMessage = registerViewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                registerViewModel.clearError()
            }
        }
    }


    if (editMode) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    {
                        BackButton {
                            navController.popBackStack()
                        }
                    },
                    text = "Edit Profile",
                    icons = listOf()
                )
            },
            bottomBar = {
            },

            ) {

            RegisterScreenContent(
                navController = navController,
                registerViewModel = registerViewModel,
                editMode = editMode,
                userUUID = userUUID,
                modifier = Modifier.padding(it)
            )
        }
    } else {
        RegisterScreenContent(
            navController = navController,
            registerViewModel = registerViewModel,
            editMode = editMode,
            userUUID = userUUID
        )
    }

}

@Composable
fun RegisterScreenContent(
    navController: NavController,
    registerViewModel: RegisterViewModel,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    userUUID: String? = null
) {
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val isUserSet by registerViewModel.isUserSet.collectAsState()
    val userTypeLiveData = registerViewModel.userType.observeAsState()



    if (editMode) {
        LaunchedEffect(key1 = userUUID) {
            registerViewModel.setUser(userUUID!!)
        }
//        registerViewModel.validateEdit()
        selectedImageUri = Uri.parse(registerViewModel.registrationUiState.value.photoUri)
    }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            run {
                selectedImageUri = uri
                selectedImageUri?.let {
                    RegistrationUIEvent.ProfilePhotoChanged(
                        it
                    )
                }?.let { registerViewModel.onEvent(it) }
            }
        }
    )

    if (isUserSet || (!editMode && !registerViewModel.signUpInProgress.value)) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(28.dp)

            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!editMode) {
                        NormalTextComponent(value = "Hey there,")
                        BoldTextComponent(value = "Create an account")
                        Spacer(modifier = Modifier.height(25.dp))
                    }

                    if (editMode) {
                        RoundImage(
                            imageUrl = selectedImageUri.toString(),
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable {
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    MyTextFieldComponent(
                        labelValue = "First Name",
                        onTextSelected = {
                            registerViewModel.onEvent(
                                RegistrationUIEvent.FirstNameChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = registerViewModel.registrationUiState.value.firstNameError,
                        initialValue = registerViewModel.registrationUiState.value.firstName
                    )
                    MyTextFieldComponent(
                        labelValue = "Last Name",
                        onTextSelected = {
                            registerViewModel.onEvent(
                                RegistrationUIEvent.LastNameChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = registerViewModel.registrationUiState.value.lastNameError,
                        initialValue = registerViewModel.registrationUiState.value.lastName
                    )
                    MyTextFieldComponent(
                        labelValue = "Username",
                        onTextSelected = {
                            registerViewModel.onEvent(
                                RegistrationUIEvent.UsernameChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = registerViewModel.registrationUiState.value.userNameError,
                        initialValue = registerViewModel.registrationUiState.value.username
                    )
                    if (!editMode) {
                        MyTextFieldComponent(
                            labelValue = "Email",
                            onTextSelected = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.EmailChanged(
                                        it
                                    )
                                )
                            },
                            errorStatus = registerViewModel.registrationUiState.value.emailError,
                            initialValue = registerViewModel.registrationUiState.value.email
                        )
                    }
                    if (!editMode) {
                        MyPasswordFieldComponent(
                            labelValue = "Password",
                            onTextSelected = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.PasswordChanged(
                                        it
                                    )
                                )
                            },
                            errorStatus = registerViewModel.registrationUiState.value.passwordError
                        )
                    }
                    if (!editMode) {
                        MyDropDownMenuComponent(
                            label_value = "Account Type",
                            options = arrayOf(
                                "Regular Account",
                                "Artist account",
                                "Event Organizer account"
                            ),
                            onTextSelected = {
                                registerViewModel.onEvent(RegistrationUIEvent.AccountTypeChanged(it))
                            })
                    }

                    if (registerViewModel.registrationUiState.value.accountType == "Artist account" || userTypeLiveData.value == "artist") {
                        MyTextFieldComponent(
                            "Stage Name",
                            onTextSelected = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.StageNameChanged(
                                        it
                                    )
                                )
                            },
                            errorStatus = registerViewModel.registrationUiState.value.stageNameError,
                            initialValue = registerViewModel.registrationUiState.value.stageName
                        )
                    }
                    if (registerViewModel.registrationUiState.value.accountType == "Event Organizer account" || userTypeLiveData.value == "event_founder") {
                        MyTextFieldComponent(
                            "Phone",
                            onTextSelected = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.PhoneChanged(
                                        it
                                    )
                                )
                            },
                            errorStatus = registerViewModel.registrationUiState.value.passwordError,
                            initialValue = registerViewModel.registrationUiState.value.phone
                        )
                    }
                    if (editMode) {
                        Spacer(modifier = Modifier.height(30.dp))
                        MyButtonComponent(
                            value = "Save Changes",
                            onButtonClicked = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.SaveEditChangesButtonClicked(
                                        navController
                                    )
                                )
                            },
                            isEnabled = true
//                            registerViewModel.photoUriValidationsPassed.value &&
//                                    registerViewModel.firstNameValidationsPassed.value &&
//                                    registerViewModel.lastNameValidationsPassed.value &&
//                                    registerViewModel.usernameValidationsPassed.value &&
//                                    (
//                                            (registerViewModel.userType.value.toString() == "event_founder" && registerViewModel.phoneValidationsPassed.value) ||
//                                                    (registerViewModel.userType.value.toString() == "artist" && registerViewModel.stageNameValidationsPassed.value) ||
//                                                    registerViewModel.userType.value.toString() == "user"
//                                            )
                        )
                    }

                }
                if (!editMode) {
                    Column {
                        Spacer(modifier = Modifier.height(620.dp))
                        MyButtonComponent(
                            value = "Register",
                            onButtonClicked = {
                                registerViewModel.onEvent(
                                    RegistrationUIEvent.RegisterButtonClicked(
                                        navController
                                    )
                                )
                            },
                            isEnabled = true
//                            registerViewModel.firstNameValidationsPassed.value &&
//                                    registerViewModel.lastNameValidationsPassed.value &&
//                                    registerViewModel.usernameValidationsPassed.value &&
//                                    registerViewModel.emailValidationsPassed.value &&
//                                    registerViewModel.passwordValidationsPassed.value &&
//                                    (
//                                            (registerViewModel.registrationUiState.value.accountType == "Event Organizer account" && registerViewModel.phoneValidationsPassed.value) ||
//                                                    (registerViewModel.registrationUiState.value.accountType == "Artist account" && registerViewModel.stageNameValidationsPassed.value) ||
//                                                    registerViewModel.registrationUiState.value.accountType == "Regular Account"
//                                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ClickableLoginRegisterText(
                            onTextSelected = { navController.navigate(Constants.NAVIGATION_LOGIN_PAGE) },
                            initial_text = "Already have an account? ",
                            action_text = "Login",
                            span_style = SpanStyle(
                                color = green,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }

            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
