package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.R
import com.example.bend.components.BottomNavigationBar2
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventComponent
import com.example.bend.model.Event
import com.example.bend.ui.theme.green
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
    val newNotifications = homeViewModel.newNotifications.observeAsState()

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "BenD",
                icons = listOf {
                    if (newNotifications.value?.isNotEmpty() == false) {
                        Icon(
                            painter = painterResource(id = R.drawable.notifications_inactive),
                            contentDescription = "Notifications",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(55.dp)
                                .clip(shape = RoundedCornerShape(40.dp))

                                .clickable {
                                    navController.navigate(Constants.NAVIGATION_NOTIFICATIONS_PAGE)
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_notifications_active_24),
                            contentDescription = "Notifications",
                            tint = green,
                            modifier = Modifier
                                .size(55.dp)
                                .clip(shape = RoundedCornerShape(40.dp))

                                .clickable {
                                    navController.navigate(Constants.NAVIGATION_NOTIFICATIONS_PAGE)
                                }
                        )
                    }
                })
        },
        bottomBar = {
            BottomNavigationBar2(
                navController = navController,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { selectedItemIndex = it }
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