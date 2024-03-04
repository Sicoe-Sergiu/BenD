package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventComponent
import com.example.bend.model.Event
import com.example.bend.view_models.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val events = homeViewModel.events.observeAsState()

    Scaffold(
        topBar = {
            CustomTopBar(
                name = "BenD",
                icons = listOf {
                })
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.FEED
            )
        },

        ) { innerPadding ->
        EventsList(
            events = events.value ?: emptyList(),
            navController = navController,
            homeViewModel = homeViewModel,
            modifier = Modifier
                .padding(innerPadding)
        )
    }
}

@Composable
fun EventsList(
    events: List<Event>,
    modifier: Modifier,
    navController: NavController,
    homeViewModel: HomeViewModel

) {
    val isRefreshing by homeViewModel.isLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = homeViewModel::loadData,
        modifier = Modifier
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = modifier
                .background(Color.White)
        ) {
            itemsIndexed(events) { index, event ->

                EventComponent(
                    event = event,
                    founder = homeViewModel.getFounderByUUID(event.founderUUID),
                    artists = homeViewModel.getEventArtists(event),
                    viewModel = homeViewModel,
                    navController = NavController
                )
                if (index < events.size - 1) {
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 150.dp)
                    )
                }
            }
        }
    }
}