package com.example.bend.ui.screens

import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bend.Constants
import com.example.bend.components.CustomTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.bend.R
import com.example.bend.components.BottomNavigationBar2
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.model.Event
import com.example.bend.ui.theme.green
import com.example.bend.view_models.ProfileViewModel
import com.example.bend.view_models.RegisterViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    registerViewModel: RegisterViewModel = viewModel(),
    userUUID: String
) {
    var selectedItemIndex by rememberSaveable { mutableStateOf(3) }

    Scaffold(
        topBar = { ProfileTopBar(userUUID, navController, registerViewModel, profileViewModel) },
        bottomBar = {
            BottomNavigationBar2(
                navController = navController,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { selectedItemIndex = it }
            )
        },
    ) { innerPadding ->
        ProfileContent(navController, profileViewModel, userUUID, innerPadding)
    }
}

@Composable
fun ProfileTopBar(
    userUUID: String,
    navController: NavController,
    registerViewModel: RegisterViewModel,
    profileViewModel: ProfileViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val userData by profileViewModel.userData.observeAsState()


    CustomTopBar(
        {
            BackButton {
                navController.popBackStack()
            }
        },
        text = userData?.second?.get("username")?.toString() ?: "Default username",
        icons = listOf(
            {
                if (userData?.first == "event_founder" && userUUID == FirebaseAuth.getInstance().currentUser?.uid){
                    Icon(
                        painter = painterResource(id = R.drawable.plus_sym),
                        contentDescription = "Add event",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                navController.navigate(Constants.NAVIGATION_CREATE_EVENT_PAGE)
                            }
                    )
                }
            },
            {
                Icon(
                    painter = painterResource(id = R.drawable.lines_menu),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            expanded = true
                        }
                )
            }

        ))
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(x = 220.dp, y = 100.dp)
    ) {
        DropdownMenuItem(onClick = {
            navController.navigate(Constants.editUserNavigation(FirebaseAuth.getInstance().currentUser!!.uid))
            expanded = false
        }) {
            MenuItem(icon = Icons.Default.Edit, text = "Edit Profile")
        }
        DropdownMenuItem(onClick = {
            registerViewModel.onEvent(RegistrationUIEvent.LogOutButtonClicked(navController))
            expanded = false
        }) {
            MenuItem(icon = Icons.Default.Logout, text = "Logout...")
        }
    }
}

@Composable
fun MenuItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    contentDescription: String? = null
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    userUUID: String,
    innerPadding: PaddingValues
) {
    val userData by viewModel.userData.observeAsState()
    val userEvents by viewModel.userEvents.observeAsState(emptyList())
    val userFollowers by viewModel.userFollowers.observeAsState(0)
    val userFollowing by viewModel.userFollowing.observeAsState(0)
    val isRefreshing by viewModel.isLoading.observeAsState(initial = false)
    val followState by viewModel.followState.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    LaunchedEffect(key1 = userUUID) {
        viewModel.refreshUserData(userUUID)
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshUserData(userUUID) },
        modifier = Modifier.background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    userData?.let {
                        ProfileSection(it, userEvents.size, userFollowers, userFollowing)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(vertical = 15.dp)
                    ) {
                        ButtonSection(
                            viewModel = viewModel,
                            userUUID = userUUID,
                            isFollowButtonVisible = followState != true,
                            onFollowButtonClick = {
                                if (followState == true) {
                                    viewModel.unfollow(userUUID)
                                } else {
                                    viewModel.follow(userUUID)
                                }
                            }, navController = navController
                        )
                    }
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
                        )
                    ) {
                        selectedTabIndex = it
                    }
                    val futureEvents = userEvents.filter { event ->
                        LocalDate.parse(
                            event.endDate,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        ) > LocalDate.now()
                    }
                    val pastEvents = userEvents.filter { event ->
                        LocalDate.parse(
                            event.endDate,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        ) < LocalDate.now()
                    }
                    Box(modifier = Modifier.background(Color.LightGray)) {
                        when (selectedTabIndex) {
                            0 -> if (futureEvents.isEmpty()) EmptyPlaceholder(text = "No future Events to display.") else PostSection(
                                events = futureEvents,
                                modifier = Modifier.fillMaxWidth(),
                                navController
                            )

                            1 -> if (pastEvents.isEmpty()) EmptyPlaceholder(text = "No past Events to display.") else PostSection(
                                events = pastEvents,
                                modifier = Modifier.fillMaxWidth(),
                                navController
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileSection(
    userData: Pair<String, MutableMap<String, Any>?>,
    userEvents: Int,
    userFollowers: Int,
    userFollowing: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        ProfileImageSection(imageUrl = userData.second?.get("profilePhotoURL")?.toString() ?: "")

        Spacer(modifier = Modifier.height(20.dp))

        StatSection(
            eventsNo = userEvents,
            followersNo = userFollowers,
            followingNo = userFollowing,
            rating = userData.second?.get("rating") as? Double ?: 0.0,
            ratingNo = userData.second?.get("ratingsNumber") as? Long ?: 0L,
            displayRating = (userData.first == "event_founder" || userData.first == "artist")

        )

        Spacer(modifier = Modifier.height(20.dp))

        ProfileDescription(
            displayName = (
                    (userData.second?.get("lastName")?.toString() ?: "Default last name") +
                            " " +
                            (userData.second?.get("firstName")?.toString()
                                ?: "Default first name") +
                            when (userData.first) {
                                "user" -> ""
                                "event_founder" -> " (Event Organizer)"
                                "artist" -> " (Artist)"
                                else -> "Unknown"
                            }
                    ),
            phone = (userData.second?.get("phone")?.toString() ?: ""),
            email = (userData.second?.get("email")?.toString() ?: ""),
            userType = userData.first
        )
    }
}

@Composable
fun ProfileImageSection(imageUrl: String) {
    RoundImage(
        imageUrl = imageUrl, modifier = Modifier
            .size(100.dp)
            .aspectRatio(1f)
    )
}

@Composable
fun ButtonSection(
    viewModel: ProfileViewModel,
    userUUID: String,
    isFollowButtonVisible: Boolean,
    onFollowButtonClick: () -> Unit,
    navController: NavController
) {
    val userData = viewModel.userData.observeAsState()

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(Color.White),
        horizontalArrangement = Arrangement.Center
    ) {
        if (userUUID != FirebaseAuth.getInstance().currentUser?.uid) {
            if (isFollowButtonVisible) {
                RoundCornersButton(
                    text = "Follow",
                    viewModel = viewModel,
                    userUUID = userUUID,
                    onClick = onFollowButtonClick
                )
            } else {
                GreenRoundCornersButton(
                    viewModel = viewModel,
                    userUUID = userUUID,
                    onClick = onFollowButtonClick
                )
            }
        }
        Log.d("PLM", userData.value?.first.toString())
        if (userData.value?.first == "event_founder" || userData.value?.first == "artist")
            RoundCornersButton(
                text = "Reviews",
                viewModel = viewModel,
                userUUID = userUUID,
                onClick = { navController.navigate(Constants.founderReviewNavigation(userUUID)) })
    }
}

@Composable
fun StatSection(
    modifier: Modifier = Modifier,
    eventsNo: Int,
    followersNo: Int,
    followingNo: Int,
    rating: Double?,
    ratingNo: Long?,
    displayRating: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        ProfileStat(numberText = eventsNo.toString(), text = "Events")
        ProfileStat(numberText = followersNo.toString(), text = "Followers")
        ProfileStat(numberText = followingNo.toString(), text = "Following")
        if (displayRating)
            ProfileStat(
                numberText = String.format("%.1f", (rating!! / ratingNo!!)) + " / 5",
                text = "Rating"
            )

    }
}

@Composable
fun ProfileStat(
    numberText: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(70.dp)
    ) {
        androidx.compose.material.Text(
            text = numberText,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text)
    }
}

@Composable
fun ProfileDescription(
    displayName: String,
    phone: String,
    email: String,
    userType: String,

    ) {
    val letterSpacing = 0.5.sp
    val lineHeight = 20.sp
    Spacer(modifier = Modifier.width(20.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = displayName,
            fontWeight = FontWeight.Bold,
            letterSpacing = letterSpacing,
            lineHeight = lineHeight
        )
        if (userType == "event_founder") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.phone),
                    contentDescription = "phone",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(15.dp)
                )
                Text(
                    text = phone,
                    letterSpacing = letterSpacing,
                    lineHeight = lineHeight
                )
            }
        }
        if (userType == "event_founder" || userType == "artist") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.email),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(15.dp)
                )
                Text(
                    text = email,
                    letterSpacing = letterSpacing,
                    lineHeight = lineHeight
                )
            }
        }


    }
}

@Composable
fun RoundCornersButton(
    text: String,
    viewModel: ProfileViewModel,
    userUUID: String,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(30.dp)
            .width(110.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(Color.LightGray),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(
            text,
        )
    }
}

@Composable
fun GreenRoundCornersButton(viewModel: ProfileViewModel, userUUID: String, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(30.dp)
            .width(110.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = green),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(
            "Unfollow",
        )
    }
}

@Composable
fun PostTabView(
    modifier: Modifier = Modifier,
    imageWithTexts: List<ImageWithText>,
    onTabSelected: (selectedIndex: Int) -> Unit
) {
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    val inactiveColor = Color(0xFF777777)
    TabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent,
        contentColor = Color.Black,
        modifier = modifier
            .background(Color.White)
    ) {
        imageWithTexts.forEachIndexed { index, item ->
            Tab(
                selected = selectedTabIndex == index,
                selectedContentColor = Color.Black,
                unselectedContentColor = inactiveColor,
                onClick = {
                    selectedTabIndex = index
                    onTabSelected(index)
                }
            ) {
                androidx.compose.material.Icon(
                    painter = item.image,
                    contentDescription = item.text,
                    tint = if (selectedTabIndex == index) Color.Black else inactiveColor,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TabViewWithText(
    modifier: Modifier = Modifier,
    texts: List<String>,
    onTabSelected: (selectedIndex: Int) -> Unit
) {
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    val inactiveColor = Color(0xFF777777)
    TabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent,
        contentColor = Color.Black,
        modifier = modifier
    ) {
        texts.forEachIndexed { index, item ->
            Tab(
                selected = selectedTabIndex == index,
                selectedContentColor = Color.Black,
                unselectedContentColor = inactiveColor,
                onClick = {
                    selectedTabIndex = index
                    onTabSelected(index)
                }
            ) {
                Text(
                    text = item, modifier = Modifier
                        .padding(10.dp), fontSize = 15.sp
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PostSection(
    events: List<Event>,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .scale(1.01f)

    ) {
        items(events.size) {
            AsyncImage(
                model = events[it].posterDownloadLink,
                contentDescription = "Event Poster",
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(
                        width = 1.dp,
                        color = Color.White
                    )
                    .clickable {
                        navController.navigate(Constants.singleEventNavigation(events[it].uuid))
                    },
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

@Composable
fun RoundImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {

    AsyncImage(
        model = imageUrl,
        contentDescription = "poster image",
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun RoundImageNoBorder(
    imageURL: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageURL,
        contentDescription = "poster image",
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .padding(3.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun BackButton(onBackPressed: () -> Unit) {
    IconButton(
        onClick = { onBackPressed() },
        modifier = Modifier.size(18.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "back button"
        )
    }
}

data class ImageWithText(
    val image: Painter,
    val text: String
)
