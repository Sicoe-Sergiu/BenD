package com.example.bend.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.example.bend.view_models.HomeViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    userUUID: String
) {
    var userData by remember { mutableStateOf<Pair<String, MutableMap<String, Any>?>>(Pair("", null)) }
    var userEvents by remember { mutableStateOf<List<Event>>(emptyList()) }



    LaunchedEffect(key1 = userUUID) {
        userData = viewModel.getUserMapById(userUUID) ?: Pair("", null)
        userEvents = viewModel.getUserEvents(userUUID)
        Log.e("PROFILE USER DATA", userData.toString())
    }
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.PROFILE
            )
        },

        ) {

        Column(modifier = Modifier.fillMaxSize()) {
            CustomTopBar(
                {
                    BackButton {
                        navController.popBackStack()
                    }
                }
                ,
                userData.second?.get("username")?.toString() ?: "Default username"
                ,
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
            Spacer(modifier = Modifier.height(10.dp))
            ProfileSection(userData, viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            ButtonSection(modifier = Modifier.fillMaxWidth(),viewModel = viewModel, userUUID = userUUID)
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
                    postsURLs = listOf(
                        ""
                    ),
                    modifier = Modifier.fillMaxWidth()
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
fun ProfileSection(userData: Pair<String, MutableMap<String, Any>?>, viewModel: HomeViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            RoundImage(
                imageUrl = (userData.second?.get("profilePhotoURL")?.toString() ?: "Default last name"),
                modifier = Modifier
                    .size(100.dp)
                    .weight(3f)
            )
            StatSection(modifier = Modifier.weight(7f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        ProfileDescription(
            displayName = (userData.second?.get("lastName")?.toString() ?: "Default last name") + " " + (userData.second?.get("firstName")?.toString() ?: "Default last name") + " (" + userData.first + ")",
            phone = (userData.second?.get("phone")?.toString() ?: ""),
            email = (userData.second?.get("email")?.toString() ?: ""),
        )
    }
}

@Composable
fun ProfileDescription(
    displayName: String,
    phone: String,
    email: String,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.phone),
                contentDescription = "Menu",
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
fun StatSection(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        ProfileStat(numberText = "ex1", text = "Events")
        ProfileStat(numberText = "ex2", text = "Followers")
        ProfileStat(numberText = "ex3", text = "Following")
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
        modifier = modifier
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
    userUUID: String
) {
    val minWidth = 95.dp
    val height = 30.dp
    var isFollowButtonVisible by remember { mutableStateOf(true) }

    Row(modifier = Modifier.padding(horizontal = 20.dp)) {
        if (isFollowButtonVisible) {
            ActionButton(
                text = "Follow",
                modifier = Modifier
                    .defaultMinSize(minWidth = minWidth)
                    .height(height)
                    .clickable {
                        viewModel.follow(followedUserUUID = userUUID)
                        isFollowButtonVisible = false
                    }
            )
        } else {
            ActionButton(
                text = "Following",
                modifier = Modifier
                    .defaultMinSize(minWidth = minWidth)
                    .height(height)
                    .clickable {
                        viewModel.unfollow(unfollowedUserUUID = userUUID)
                        isFollowButtonVisible = true
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
    postsURLs: List<String>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .scale(1.01f)
    ) {
        items(postsURLs.size) {
            AsyncImage(
                model = postsURLs[it],
                contentDescription = "Event Poster",
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(
                        width = 1.dp,
                        color = Color.White
                    ),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

data class ImageWithText(
    val image: Painter,
    val text: String
)