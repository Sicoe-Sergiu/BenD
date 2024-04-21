package com.example.bend.view.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.R
import com.example.bend.model.Artist
import com.example.bend.view.components.BottomNavigationBar2
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.EventComponent
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FeedScreen(
    navController: NavController,
    homeViewModel: HomeViewModel
) {
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val newNotifications = homeViewModel.newNotifications.observeAsState()
    val isLoading = homeViewModel.isLoading.observeAsState()
    homeViewModel.founders.observeAsState()
    homeViewModel.artists.observeAsState()

    val swipeRefreshState = isLoading.value?.let { rememberSwipeRefreshState(isRefreshing = it) }

    val context = LocalContext.current
    val errorMessage = homeViewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != "") {
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                homeViewModel.clearError()
            }
        }
    }

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
        Log.d("LOADING", isLoading.value.toString())
        if (isLoading.value == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Black)
            }

        } else {
            if (swipeRefreshState != null) {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        homeViewModel.loadEvents()
                    },
                    modifier = Modifier
                        .background(Color.LightGray)
                        .padding(innerPadding)
                ) {
                    EventsList(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun EventsList(
    modifier: Modifier,
    navController: NavController,
    homeViewModel: HomeViewModel

) {
    val events = homeViewModel.events.observeAsState()
    val founders = homeViewModel.founders.observeAsState()


    if (events.value!!.isEmpty()) EmptyPlaceholder(text = "No Events to display.") else {
        LazyColumn(
            state = homeViewModel.homeScreenScrollState,
            modifier = modifier
                .background(Color.LightGray),
            userScrollEnabled = true
        ) {
            itemsIndexed(events.value.orEmpty()) { index, event ->
                val context = LocalContext.current
                val eventArtists = remember {
                    mutableStateOf<List<Artist>>(emptyList())
                }
                LaunchedEffect(key1 = event) {
                    eventArtists.value =
                        HomeViewModel.getEventArtistsFromFirebase(context, event.second)
                }


                if (event.first == null) {
                    EventComponent(
                        event = event.second,
                        founder = founders.value?.find { founder -> founder.uuid == event.second.founderUUID },
                        artists = eventArtists.value,
                        viewModel = homeViewModel,
                        navController = navController,
                    )
                } else {
                    EventReposted(
                        whoRepost = event.first!!,
                        event = event.second,
                        founder = founders.value?.find { founder -> founder.uuid == event.second.founderUUID },
                        artists = eventArtists.value,
                        viewModel = homeViewModel,
                        navController = navController,
                    )
                }


            }
        }
    }
}