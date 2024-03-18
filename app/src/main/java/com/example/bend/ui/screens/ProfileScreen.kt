package com.example.bend.ui.screens

import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bend.Constants
import com.example.bend.components.CustomTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.bend.R
import com.example.bend.components.BottomNavigationBar2
import com.example.bend.model.Event
import com.example.bend.ui.theme.green
import com.example.bend.view_models.ProfileViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    userUUID: String
) {
    var selectedItemIndex by rememberSaveable { mutableStateOf(3) }

    Scaffold(
        topBar = { ProfileTopBar(navController, userUUID) },
        bottomBar = {
            BottomNavigationBar2(
                navController = navController,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { selectedItemIndex = it}
            )
        },
    ) { innerPadding ->
        ProfileContent(navController, viewModel, userUUID, innerPadding)
    }
}

@Composable
fun ProfileTopBar(navController: NavController, userUUID: String) {
    val userData by remember {
        mutableStateOf<Pair<String, MutableMap<String, Any>?>>(
            Pair(
                "",
                null
            )
        )
    }

    CustomTopBar(
        {
            BackButton {
                navController.popBackStack()
            }
        },
        userData.second?.get("username")?.toString() ?: "Default username",
        icons = listOf(
            {
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
            },
            {
                Icon(
                    painter = painterResource(id = R.drawable.lines_menu),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { //TODO: add click action
                        }
                )
            }

        ))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    userUUID: String,
    innerPadding: PaddingValues
) {
    var userData by remember {
        mutableStateOf<Pair<String, MutableMap<String, Any>?>>(
            Pair(
                "",
                null
            )
        )
    }
    var userEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var userFollowers by remember { mutableStateOf(0) }
    var userFollowing by remember { mutableStateOf(0) }
    var followButtonState by remember { mutableStateOf(true) }


    LaunchedEffect(key1 = userUUID) {
        val userDataDeferred = async { viewModel.getUserDataPair(userUUID) }
        val userEventsDeferred = async { viewModel.getUserEvents(userUUID) }
        val userFollowersDeferred = async { viewModel.getUserFollowers(userUUID) }
        val userFollowingDeferred = async { viewModel.getUserFollowing(userUUID) }

        val results = awaitAll(
            userDataDeferred,
            userEventsDeferred,
            userFollowingDeferred,
            userFollowersDeferred
        )

        userData = results[0] as Pair<String, MutableMap<String, Any>?>
        userEvents = results[1] as List<Event>
        userFollowers = results[2] as Int
        userFollowing = results[3] as Int
    }

    LaunchedEffect(followButtonState) {
        followButtonState = !viewModel.ifFollow(userUUID)
        userFollowers = viewModel.getUserFollowers(userUUID)
        userFollowing = viewModel.getUserFollowing(userUUID)
    }


    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileSection(userData, userEvents.size, userFollowers, userFollowing)
        Spacer(modifier = Modifier.height(10.dp))
        ButtonSection(
            modifier = Modifier.fillMaxWidth(),
            viewModel,
            userUUID,
            isFollowButtonVisible = followButtonState,
            onFollowButtonClick = {
                followButtonState = !followButtonState
            }
        )
        Spacer(modifier = Modifier.height(5.dp))

        PostTabView(
            imageWithTexts = listOf(
                ImageWithText(
                    image = painterResource(id = R.drawable.future),
                    text = "Future Events"
                ),
                ImageWithText(
                    image = painterResource(id = R.drawable.time_past),
                    text = "Past Events"
                ),
            )
        ) {
            selectedTabIndex = it
        }
        when (selectedTabIndex) {
            0 -> PostSection(
                events = userEvents.filter { event ->
                    LocalDate.parse(
                        event.endDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    ) > LocalDate.now()
                },
                modifier = Modifier.fillMaxWidth(),
                navController
            )

            1 -> PostSection(
                events = userEvents.filter { event ->
                    LocalDate.parse(
                        event.endDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    ) < LocalDate.now()
                },
                modifier = Modifier.fillMaxWidth(),
                navController
            )
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
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        Spacer(modifier = Modifier.height(10.dp))
        ProfileImageSection(imageUrl = userData.second?.get("profilePhotoURL")?.toString() ?: "")

        // User statistics (events, followers, following)
        Spacer(modifier = Modifier.height(10.dp))

        StatSection(eventsNo = userEvents, followersNo = userFollowers, followingNo = userFollowing)

        // User description (name, userType)
        Spacer(modifier = Modifier.height(10.dp))

        ProfileDescription(
            displayName = (
                    (userData.second?.get("lastName")?.toString() ?: "Default last name") +
                            " " +
                            (userData.second?.get("firstName")?.toString()
                                ?: "Default first name") +
                            " (" +
                            when (userData.first) {
                                "user" -> "User"
                                "event_founder" -> "Event Organizer"
                                "artist" -> "Artist"
                                else -> "Unknown"
                            } + ")"
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
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    userUUID: String,
    isFollowButtonVisible: Boolean,
    onFollowButtonClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = 20.dp
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center

    ) {
        if (isFollowButtonVisible) {
            FollowButton(viewModel = viewModel, userUUID = userUUID, onClick = onFollowButtonClick)
        } else {
            FollowingButton(
                viewModel = viewModel,
                userUUID = userUUID,
                onClick = onFollowButtonClick
            )
        }
    }
}

@Composable
fun StatSection(
    modifier: Modifier = Modifier,
    eventsNo: Int, followersNo: Int, followingNo: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        ProfileStat(numberText = eventsNo.toString(), text = "Events")
        ProfileStat(numberText = followersNo.toString(), text = "Followers")
        ProfileStat(numberText = followingNo.toString(), text = "Following")
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
fun FollowButton(viewModel: ProfileViewModel, userUUID: String, onClick: () -> Unit) {
    Button(
        onClick = {
            viewModel.follow(userUUID)
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
            "Follow",
        )
    }
}

@Composable
fun FollowingButton(viewModel: ProfileViewModel, userUUID: String, onClick: () -> Unit) {
    Button(
        onClick = {
            viewModel.unfollow(userUUID)
            onClick()
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(30.dp)
            .width(110.dp)
        ,
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
                Text(text = item, modifier = Modifier
                    .padding(10.dp)
                , fontSize = 15.sp
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
            )
        ,
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
