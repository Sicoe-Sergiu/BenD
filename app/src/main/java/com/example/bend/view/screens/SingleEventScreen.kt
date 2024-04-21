package com.example.bend.view.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.bend.model.Artist
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.EventComponent
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.viewmodel.HomeViewModel

@Composable
fun SingleEventScreen(
    eventUUID: String,
    viewModel: HomeViewModel,
    navController: NavHostController

) {
    var event by remember { mutableStateOf(Event()) }
    val founder = remember { mutableStateOf(EventFounder()) }
    val artists = remember { mutableStateOf(listOf<Artist>()) }

    val context = LocalContext.current
    val errorMessage = viewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(key1 = founder, key2 = artists, key3 = eventUUID){
        event = HomeViewModel.getEventByUUID(context, eventUUID) ?: Event()
        founder.value = HomeViewModel.getFounderByUUID(context, event.founderUUID) ?: EventFounder()
        artists.value = HomeViewModel.getEventArtistsFromFirebase(context, event)
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                {
                    BackButton {
                        navController.popBackStack()
                    }
                },
                "Event",
                icons = listOf(

                ))
        },
        bottomBar = {
        },

        ) { innerPadding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.LightGray)
        ){
            EventComponent(
                event = event,
                founder = founder.value,
                artists = artists.value,
                viewModel = viewModel,
                navController = navController,

                )
        }
    }
}