package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.bend.components.BottomNavigationBar2
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventComponent
import com.example.bend.model.Event
import com.example.bend.view_models.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FeedScreen(
    navController: NavController,
    homeViewModel: HomeViewModel
) {
    val events = homeViewModel.events.observeAsState()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "BenD",
                icons = listOf {
                })
        },
        bottomBar = {
            BottomNavigationBar2(
                navController = navController,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { selectedItemIndex = it}
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
        onRefresh = {
            homeViewModel.loadData()
        },
        modifier = Modifier
            .background(Color.White)
    ) {
        LazyColumn(
            state = homeViewModel.homeScreenScrollState,
            modifier = modifier
                .background(Color.White),
            userScrollEnabled = true
        ) {
            itemsIndexed(events) { index, event ->


                EventComponent(
                    event = event,
                    founder = homeViewModel.getFounderByUUID(event.founderUUID),
                    artists = homeViewModel.getEventArtists(event),
                    viewModel = homeViewModel,
                    navController = navController,
                )

            }
        }
    }
}