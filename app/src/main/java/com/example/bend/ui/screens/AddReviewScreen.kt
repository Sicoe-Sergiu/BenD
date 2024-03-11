package com.example.bend.ui.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.view_models.AddReviewViewModel

@Composable
fun AddReviewScreen(
    viewModel: AddReviewViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController,
    eventUUID: String
){
    Text(text = "AddReview screen")
}