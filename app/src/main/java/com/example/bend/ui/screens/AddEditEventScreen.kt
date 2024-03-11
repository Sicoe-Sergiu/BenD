package com.example.bend.ui.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.bend.view_models.EditEventViewModel

@Composable
fun EditEventScreen(
    eventUUID: String,
    viewModel: EditEventViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavHostController
) {
    Text(text = "edit event screen")

}