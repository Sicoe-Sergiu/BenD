package com.example.bend.view.screens

import android.net.Uri
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.view.components.BoldTextComponent
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.NormalTextComponent
import com.example.bend.viewmodel.ProfilePhotoViewModel

@Composable
fun SetProfilePhotoScreen(
    navController: NavController,
    profilePhotoViewModel: ProfilePhotoViewModel = viewModel()
) {


    var selectedImageUri by remember {
        mutableStateOf<Uri?>(Uri.parse(Constants.DEFAULT_PROFILE_PHOTO_URL))
    }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            run {
                selectedImageUri = uri
                profilePhotoViewModel.setPhoto(uri)
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(28.dp)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                NormalTextComponent(value = "Hey there,")
                BoldTextComponent(value = "Let's set a profile photo")
                Spacer(modifier = Modifier.height(25.dp))
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
                MyButtonComponent(
                    value = "Save Photo",
                    onButtonClicked = {
                        profilePhotoViewModel.uploadPhotoToFirebase(navController = navController)
                    },
                    isEnabled = true
                )
            }
        }
    }
}

