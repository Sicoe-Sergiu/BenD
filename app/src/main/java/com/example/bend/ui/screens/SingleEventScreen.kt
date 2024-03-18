package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bend.Constants
import com.example.bend.R
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventComponent
import com.example.bend.model.Event
import com.example.bend.view_models.HomeViewModel

@Composable
fun SingleEventScreen(
    eventUUID: String,
    viewModel: HomeViewModel,
    navController: NavHostController

) {
    var event by remember { mutableStateOf(Event()) }



    LaunchedEffect(key1 = eventUUID) {
        event = viewModel.getEventByUUID(eventUUID) ?: Event()
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                {
                    BackButton {
                        navController.popBackStack()
                    }
                },
                viewModel.getFounderByUUID(event.founderUUID)?.username + " presents:",
                icons = listOf(

                ))
        },
        bottomBar = {
        },

        ) { innerPadding ->
        Box (
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.LightGray)
        ){
            EventComponent(
                event = event,
                founder = viewModel.getFounderByUUID(event.founderUUID),
                artists = viewModel.getEventArtists(event),
                viewModel = viewModel,
                navController = navController,

                )
        }
    }
}