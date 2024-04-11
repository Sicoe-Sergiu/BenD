package com.example.bend.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bend.Constants
import com.example.bend.components.CustomTopBar
import com.example.bend.components.EventDate
import com.example.bend.components.EventPoster
import com.example.bend.model.Event
import com.example.bend.model.Notification
import com.example.bend.model.Review
import com.example.bend.model.User
import com.example.bend.ui.theme.green
import com.example.bend.view_models.HomeViewModel
import com.example.bend.view_models.ReviewsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val newNotifications = homeViewModel.newNotifications.observeAsState()
    Scaffold(
        topBar = {
            CustomTopBar({
                BackButton {
                    navController.popBackStack()
                    for (notification in newNotifications.value!!)
                        homeViewModel.seeNotification(notification.uuid)

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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (sortedNotifications.filter { !it.seen }.isNotEmpty()) {

                    TextDivider(text = "New")
                    LazyColumn() {
                        itemsIndexed(sortedNotifications.filter { !it.seen }) { _, notification ->
                            NotificationItem(notification, homeViewModel, navController)
                        }
                    }
                }
                TextDivider(text = "Old")
                LazyColumn() {
                    itemsIndexed(sortedNotifications.filter { it.seen }) { _, notification ->
                        NotificationItem(notification, homeViewModel, navController)
                    }
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
    var userType by remember { mutableStateOf("") }
    var userProfilePhotoDownloadUrl by remember { mutableStateOf("") }
    var userUsername by remember { mutableStateOf("") }
    var event by remember { mutableStateOf(Event()) }

    LaunchedEffect(key1 = notification) {
        userType = homeViewModel.getAccountType(notification.fromUserUUID)
        event = HomeViewModel.getEventByUUID(notification.eventUUID) ?: Event()
        when (userType) {
            "user" -> {
                val user = HomeViewModel.getUserByUUID(notification.fromUserUUID)
                if (user != null) {
                    userProfilePhotoDownloadUrl = user.profilePhotoURL
                    userUsername = user.username
                }
            }

            "event_founder" -> {
                val user = HomeViewModel.getFounderByUUID(notification.fromUserUUID)
                if (user != null) {
                    userProfilePhotoDownloadUrl = user.profilePhotoURL
                    userUsername = user.username
                }
            }

            "artist" -> {
                val user = HomeViewModel.getArtistByUUID(notification.fromUserUUID)
                if (user != null) {
                    userProfilePhotoDownloadUrl = user.profilePhotoURL
                    userUsername = user.username
                }
            }
        }
    }
    var newModifier = Modifier
        .padding(10.dp)
        .clip(shape = RoundedCornerShape(10.dp))

    if (notification.sensitive) {
        newModifier = Modifier
            .padding(10.dp)
            .border(width = 1.dp, color = Color.Red, shape = RoundedCornerShape(10.dp))
            .clip(shape = RoundedCornerShape(10.dp))
    }

    Box(
        modifier = newModifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundImage(
                imageUrl = userProfilePhotoDownloadUrl,
                modifier = Modifier
                    .size(50.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .clickable { navController.navigate(Constants.userProfileNavigation(notification.fromUserUUID)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            CustomStyledText(
                userUsername = userUsername,
                notification = notification,
                modifier = Modifier.weight(0.7f),
                homeViewModel = homeViewModel
            )
            if (notification.eventUUID != "" && event.posterDownloadLink != "")
                EventPoster(
                    posterUrl = event.posterDownloadLink,
                    modifier = Modifier
                        .weight(0.15f)
                        .height(60.dp)
                        .shadow(
                            elevation = 9.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true,
                            ambientColor = Color.Black
                        )
                        .clip(shape = RoundedCornerShape(10.dp))
                        .border(width = 1.dp, green, shape = RoundedCornerShape(10.dp))
                        .clickable {
                            navController.navigate(
                                Constants.singleEventNavigation(
                                    notification.eventUUID
                                )
                            )
                        }
                )

        }
    }
}

@Composable
fun CustomStyledText(
    userUsername: String,
    notification: Notification,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel
) {
    val formattedText = buildAnnotatedString {
        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
            append(userUsername)
        }
        append(" " + notification.text + "\n")
        withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Gray)) {
            append(homeViewModel.getTimeDifferenceDisplay(notification.timestamp))
        }
    }

    Text(text = formattedText, fontSize = 16.sp, modifier = modifier)
}

@Composable
fun TextDivider(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(
            modifier = Modifier.weight(1f)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
            modifier = Modifier.weight(1f)
        )
    }
}