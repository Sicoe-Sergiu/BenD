package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventDate
import com.example.bend.model.Event
import com.example.bend.model.Notification
import com.example.bend.model.Review
import com.example.bend.view_models.HomeViewModel
import com.example.bend.view_models.ReviewsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun NotificationsScreen(
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            CustomTopBar({
                BackButton {
                    navController.popBackStack()
                }
            }, text = "Notifications", icons = listOf {})
        },
        bottomBar = {},

        ) { innerPadding ->
        NotificationsList(
            modifier = Modifier.padding(innerPadding),
            homeViewModel = homeViewModel,
            navController = navController
        )
    }
}
@Composable
fun NotificationsList(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val isRefreshing by homeViewModel.isLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val notifications = homeViewModel.notifications.observeAsState(emptyList())

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { homeViewModel.loadData() },
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        val sortedNotifications = notifications.value.sortedByDescending { it.timestamp }
        if (notifications.value.isEmpty()) EmptyPlaceholder(text = "No Notifications to display.") else {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(sortedNotifications) { _, notification ->
                    NotificationItem(notification, homeViewModel, navController)
                }
            }
        }
    }
}
@Composable
fun NotificationItem(
    notification: Notification,
    homeViewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
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
                .clickable { }
        ) {
            Text(text = "aaaaa")
        }
    }
}