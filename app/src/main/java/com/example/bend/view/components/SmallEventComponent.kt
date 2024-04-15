package com.example.bend.view.components

import android.util.Log
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.Constants.addReviewNavigation
import com.example.bend.Constants.editEventNavigation
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.view.theme.LightPrimaryColor
import com.example.bend.view.theme.LightRed
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.MyEventsViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SmallEventComponent(
    event: Event,
    founder: EventFounder?,
    viewModel: MyEventsViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    selectedTab:Int
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var removeEventTrigger by remember { mutableStateOf(0) }
    var accountType by remember { mutableStateOf("") }
    var alreadyReviewed by remember { mutableStateOf(false) }


    if (showConfirmationDialog) {
        ConfirmationDialog(
            text = "Are you sure you want to delete this event from your list?",

            onConfirm = {
                removeEventTrigger++
                showConfirmationDialog = false
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }
    LaunchedEffect(removeEventTrigger) {
        if (removeEventTrigger > 0) {
            viewModel.removeEvent(event)
        }
        accountType = FirebaseAuth.getInstance().currentUser?.uid?.let { MyEventsViewModel.getAccountType(it)}
            .toString()
        alreadyReviewed = MyEventsViewModel.checkReviewExists(event.uuid)
        Log.d("ALREADYREV", alreadyReviewed.toString())
    }

    Box(
        modifier = modifier
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventHeader(event, founder, navController)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    CustomIconButton(
                        icon = Icons.Filled.Info,
                        contentDescription = "Info",
                        onClick = {
                            navController.navigate(Constants.singleEventNavigation(event.uuid))
                        },
                        buttonColor = LightPrimaryColor
                    )
                    if (selectedTab == 0 && event.founderUUID == FirebaseAuth.getInstance().currentUser?.uid){
                        CustomIconButton(
                            icon = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            onClick = { navController.navigate(editEventNavigation(event.uuid)) },
                            buttonColor = green

                        )
                    }

                    if (selectedTab == 1 && accountType == "user" && !alreadyReviewed){
                        CustomIconButton(
                            icon = Icons.Filled.Reviews,
                            contentDescription = "Review",
                            onClick = { navController.navigate(addReviewNavigation(event.uuid)) },
                            buttonColor = green
                        )
                    }
                    CustomIconButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = "Remove",
                        onClick = { showConfirmationDialog = true },
                        buttonColor = LightRed
                    )
                }
            }
        }
    }
}

@Composable
fun CustomIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = Color.Black,
    buttonColor: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 15.dp)
            .height(30.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(buttonColor),
        elevation = ButtonDefaults.buttonElevation(8.dp),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = iconColor,
        )
    }
}