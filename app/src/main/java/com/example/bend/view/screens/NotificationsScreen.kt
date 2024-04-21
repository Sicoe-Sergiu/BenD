package com.example.bend.view.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.EventPoster
import com.example.bend.model.Event
import com.example.bend.model.Notification
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun NotificationsScreen(
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val errorMessage = homeViewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                homeViewModel.clearError()
            }
        }
    }

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
    val isRefreshing = homeViewModel.isLoading.observeAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing.value!!)
    val notifications = homeViewModel.notifications.observeAsState(emptyList())

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { homeViewModel.loadEvents() },
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        val sortedNotifications = notifications.value.sortedByDescending { it.timestamp }
        if (notifications.value.isEmpty()) EmptyPlaceholder(text = "No Notifications to display.") else {
            val combinedNotifications: List<Any> by derivedStateOf {
                mutableListOf<Any>().apply {
                    val newNotifications = sortedNotifications.filter { !it.seen }
                    if (newNotifications.isNotEmpty()) {
                        add("New")
                        addAll(newNotifications)
                    }
                    add("Old")
                    addAll(sortedNotifications.filter { it.seen })
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                    itemsIndexed(combinedNotifications) { index, item ->
                        when (item) {
                            is String -> {
                                TextDivider(text = item)
                            }
                            is Notification -> {
                                NotificationItem(item, homeViewModel, navController)
                            }
                            else -> Unit
                        }
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

    val context = LocalContext.current

    LaunchedEffect(key1 = notification) {
        userType = HomeViewModel.getAccountType(context, notification.fromUserUUID)
        event = HomeViewModel.getEventByUUID(context, notification.eventUUID) ?: Event()
        when (userType) {
            "user" -> {
                val user = HomeViewModel.getUserByUUID(context, notification.fromUserUUID)
                if (user != null) {
                    userProfilePhotoDownloadUrl = user.profilePhotoURL
                    userUsername = user.username
                }
            }

            "event_founder" -> {
                val user = HomeViewModel.getFounderByUUID(context, notification.fromUserUUID)
                if (user != null) {
                    userProfilePhotoDownloadUrl = user.profilePhotoURL
                    userUsername = user.username
                }
            }

            "artist" -> {
                val user = HomeViewModel.getArtistByUUID(context, notification.fromUserUUID)
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