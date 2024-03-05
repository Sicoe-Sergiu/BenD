package com.example.bend.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bend.R
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.example.bend.Constants
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem
import com.example.bend.components.CustomTopBar
import com.example.bend.model.Event
import com.example.bend.ui.theme.green
import com.example.bend.view_models.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    userUUID: String
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
        userData = viewModel.getUserMapById(userUUID) ?: Pair("", null)
        userEvents = viewModel.getUserEvents(userUUID)
        userFollowers = viewModel.getUserFollowers(userUUID)
        userFollowing = viewModel.getUserFollowing(userUUID)
    }
    LaunchedEffect(followButtonState) {
        followButtonState = !viewModel.ifFollow(userUUID)
        userFollowers = viewModel.getUserFollowers(userUUID)
        userFollowing = viewModel.getUserFollowing(userUUID)
    }


    var selectedTabIndex by remember {
        mutableStateOf(0)
    }

    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.PROFILE
            )
        },

        ) {innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Spacer(modifier = Modifier.height(5.dp))
            ProfileSection(userData, userEvents.size, userFollowers, userFollowing)
            Spacer(modifier = Modifier.height(10.dp))
            ButtonSection(
                modifier = Modifier.fillMaxWidth(),
                viewModel = viewModel,
                userUUID = userUUID,
                isFollowButtonVisible = followButtonState,
                onFollowButtonClick = {
                    followButtonState = !followButtonState
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
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

@Composable
fun ProfileSection(
    userData: Pair<String, MutableMap<String, Any>?>,
    userEvents: Int,
    userFollowers: Int,
    userFollowing: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            RoundImage(
                imageUrl = (userData.second?.get("profilePhotoURL")?.toString()
                    ?: "Default last name"),
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(1f)
                    .padding(3.dp)
                    .align(Alignment.CenterHorizontally) // Align Center Horizontally
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatSection(
                modifier = Modifier
                    .width(250.dp)
                    .align(Alignment.CenterHorizontally), // Align Center Horizontally
                userEvents,
                userFollowers,
                userFollowing
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 150.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        ProfileDescription(
            displayName = (userData.second?.get("lastName")?.toString()
                ?: "Default last name") + " " + (userData.second?.get("firstName")?.toString()
                ?: "Default last name") + " (" + userData.first + ")",
            phone = (userData.second?.get("phone")?.toString() ?: ""),
            email = (userData.second?.get("email")?.toString() ?: ""),
            userType = userData.first
        )
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
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
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
        androidx.compose.material.Text(text = text)
    }
}

@Composable
fun ButtonSection(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    userUUID: String,
    isFollowButtonVisible: Boolean,
    onFollowButtonClick: () -> Unit
) {
    val minWidth = 95.dp
    val height = 30.dp
//    var isFollowButtonVisible by remember { mutableStateOf(true) }
//    LaunchedEffect(key1 = userUUID) {
//        isFollowButtonVisible = !viewModel.ifFollow(userUUID)
//    }

    Row(modifier = Modifier.padding(horizontal = 20.dp)) {
        if (isFollowButtonVisible) {
            ActionButton(
                text = "Follow",
                modifier = Modifier
                    .defaultMinSize(minWidth = minWidth)
                    .height(height)
                    .clickable {
                        viewModel.follow(followedUserUUID = userUUID)
                        onFollowButtonClick.invoke()
                    }
            )
        } else {
            ActionButton(
                text = "Following",
                modifier = Modifier
                    .defaultMinSize(minWidth = minWidth)
                    .height(height)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = green)
                    .clickable {
                        viewModel.unfollow(unfollowedUserUUID = userUUID)
                        onFollowButtonClick.invoke()
                    }
            )
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(6.dp)
    ) {
        if (text != null) {
            androidx.compose.material.Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black
            )
        }
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

data class ImageWithText(
    val image: Painter,
    val text: String
)