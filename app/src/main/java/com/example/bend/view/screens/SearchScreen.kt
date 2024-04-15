package com.example.bend.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bend.Constants
import com.example.bend.R
import com.example.bend.view.components.BottomNavigationBar2
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.DetailItem
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.MyTextFieldComponent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.model.User
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.MyEventsViewModel
import com.example.bend.viewmodel.SearchViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun SearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = viewModel()
) {

    var selectedItemIndex by rememberSaveable { mutableStateOf(1) }

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "Search",
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

        SearchScreenContent(
            searchViewModel = searchViewModel,
            navController = navController,
            modifier = Modifier.padding(
                innerPadding
            )
        )
    }
}

@Composable
fun SearchScreenContent(
    searchViewModel: SearchViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val events = searchViewModel.events.observeAsState()
    val founders = searchViewModel.founders.observeAsState()
    val artists = searchViewModel.artists.observeAsState()
    val users = searchViewModel.users.observeAsState()

    val isRefreshing by searchViewModel.isLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        MySearchBar(searchViewModel = searchViewModel)
        TabViewWithText(
            texts = listOf(
                "Events", "Founders", "Artists", "Users"
            ),
            onTabSelected = { selectedTabIndex = it },
        )
        when (selectedTabIndex) {
            0 -> if (events.value?.size == 0) EmptyPlaceholder(text = "No Events to display.") else SearchedEventsList(
                swipeRefreshState = swipeRefreshState,
                events = events.value!!,
                navController = navController
            )

            1 -> if (founders.value?.size == 0) EmptyPlaceholder(text = "No Founders to display.")else SearchedFoundersList(
                swipeRefreshState = swipeRefreshState,
                founders = founders.value!!,
                navController = navController
            )
            2 -> if (artists.value?.size == 0) EmptyPlaceholder(text = "No Artists to display.")else SearchedArtistsList(
                swipeRefreshState = swipeRefreshState,
                artists = artists.value!!,
                navController = navController
            )
            3 -> if (users.value?.size == 0) EmptyPlaceholder(text = "No Users to display.")else SearchedUsersList(
                swipeRefreshState = swipeRefreshState,
                users = users.value!!,
                navController = navController
            )
        }
    }
}

@Composable
fun EmptyPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Text(
            text = text,
            color = Color.DarkGray,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun MySearchBar(
    searchViewModel: SearchViewModel
) {
    var queryValue by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MyTextFieldComponent(
            labelValue = "Search...",
            onTextSelected = { queryValue = it },
            errorStatus = true,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.8f)
                .height(56.dp)
        )
        MyButtonComponent(
            value = "Search",
            onButtonClicked = { searchViewModel.search(queryValue) },
            isEnabled = true,
            modifier = Modifier
                .padding(4.dp)
                .weight(0.2f)
                .height(56.dp)
                .padding(top = 5.dp)

        )
    }
}

@Composable
fun SearchedEventsList(
    swipeRefreshState: SwipeRefreshState,
    events: List<Event>,
    navController: NavController,
) {
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { },//TODO:
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            itemsIndexed(events) { _, event ->
                SimpleEventView(
                    event = event,
                    navController = navController,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
@Composable
fun SearchedFoundersList(
    swipeRefreshState: SwipeRefreshState,
    founders: List<EventFounder>,
    navController: NavController,
) {
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { },//TODO:
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            itemsIndexed(founders) { _, founder ->
                SimpleFounderView(
                    founder = founder,
                    navController = navController,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
@Composable
fun SearchedArtistsList(
    swipeRefreshState: SwipeRefreshState,
    artists: List<Artist>,
    navController: NavController,
) {
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { },//TODO:
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            itemsIndexed(artists) { _, artist ->
                SimpleArtistView(
                    artist = artist,
                    navController = navController,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
@Composable
fun SearchedUsersList(
    swipeRefreshState: SwipeRefreshState,
    users: List<User>,
    navController: NavController,
) {
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { },//TODO:
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            itemsIndexed(users) { _, user ->
                SimpleUserView(
                    user = user,
                    navController = navController,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun SimpleEventView(
    event: Event,
    navController: NavController,
) {
    val founder = remember { mutableStateOf<EventFounder?>(null) }

    LaunchedEffect(key1 = event.founderUUID) {
        founder.value = MyEventsViewModel.getEventFounderByUuid(event.founderUUID)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { navController.navigate(Constants.singleEventNavigation(eventUUID = event.uuid)) },

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(5.dp),

            ) {
            AsyncImage(
                model = event.posterDownloadLink,
                contentDescription = "poster image",
                modifier = Modifier
                    .size(50.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = green,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            androidx.compose.material.Text(
                text = founder.value?.username ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.width(140.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(50.dp))
            DetailItem(icon = R.drawable.location, text = event.location)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {

            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.Center)
            ){
                DetailItem(icon = R.drawable.calendar, text = event.startDate)
                Text(text = " - ")
                DetailItem(icon = R.drawable.calendar, text = event.startDate)
            }
        }

    }
}
@Composable
fun SimpleFounderView(
    founder: EventFounder,
    navController: NavController,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { navController.navigate(Constants.userProfileNavigation(founder.uuid)) },

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(5.dp),

            ) {
            AsyncImage(
                model = founder.profilePhotoURL,
                contentDescription = "poster image",
                modifier = Modifier
                    .size(50.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = green,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            Text(
                text = founder.username,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.width(140.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}
@Composable
fun SimpleArtistView(
    artist: Artist,
    navController: NavController,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { navController.navigate(Constants.userProfileNavigation(artist.uuid)) },

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(5.dp),

            ) {
            AsyncImage(
                model = artist.profilePhotoURL,
                contentDescription = "poster image",
                modifier = Modifier
                    .size(50.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = green,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            Text(
                text = artist.stageName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.width(140.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}
@Composable
fun SimpleUserView(
    user: User,
    navController: NavController,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { navController.navigate(Constants.userProfileNavigation(user.uuid)) },

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(5.dp),

            ) {
            AsyncImage(
                model = user.profilePhotoURL,
                contentDescription = "poster image",
                modifier = Modifier
                    .size(50.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = green,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.width(140.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}