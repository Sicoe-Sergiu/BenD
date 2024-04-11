package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.bend.R
import com.example.bend.components.BottomNavigationBar2
import com.example.bend.components.CustomTopBar
import com.example.bend.components.SmallEventComponent
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.view_models.MyEventsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate

@Composable
fun MyEventsScreen(
    navController: NavHostController,
    viewModel: MyEventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val events = viewModel.events.observeAsState()
    var selectedItemIndex by rememberSaveable { mutableStateOf(2) }

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "My Events",
                icons = listOf {
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
        MyEventsList(
            events = events.value ?: emptyList(),
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
fun MyEventsList(
    events: List<Event>,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MyEventsViewModel
) {
    val isRefreshing by viewModel.isLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val futureEvents = events.filter { event ->
        LocalDate.parse(event.endDate).isAfter(today.minusDays(1))
    }
    val pastEvents = events.filter { event ->
        LocalDate.parse(event.endDate).isBefore(today)
    }

    Column(modifier = modifier) {
        PostTabView(
            imageWithTexts = listOf(
                ImageWithText(
                    image = painterResource(id = R.drawable.future),
                    text = "Future Events"
                ),
                ImageWithText(
                    image = painterResource(id = R.drawable.time_past),
                    text = "Past Events"
                )
            ),
            onTabSelected = { selectedTabIndex = it },
        )
        when (selectedTabIndex) {
            0 -> eventsList(swipeRefreshState, futureEvents, viewModel, navController, 0)
            1 -> eventsList(swipeRefreshState, pastEvents, viewModel, navController, 1)
        }
    }
}

@Composable
fun eventsList(
    swipeRefreshState: SwipeRefreshState,
    events: List<Event>,
    viewModel: MyEventsViewModel,
    navController: NavController,
    selectedTab: Int
) {
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.loadMyEvents() },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        if (events.isEmpty()) EmptyPlaceholder(text = "No Events to display.") else {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(events) { _, event ->

                    val founder = remember { mutableStateOf<EventFounder?>(null) }

                    LaunchedEffect(key1 = event.founderUUID) {
                        founder.value = MyEventsViewModel.getEventFounderByUuid(event.founderUUID)
                    }

                    SmallEventComponent(
                        event = event,
                        founder = founder.value,
                        viewModel = viewModel,
                        navController = navController,
                        selectedTab = selectedTab
                    )
                }
            }
        }
    }
}

