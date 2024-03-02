package com.example.bend.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.R
import com.example.bend.view_models.ProfileState
import com.example.bend.view_models.ProfileViewModel
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import com.example.bend.Constants
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem
import com.example.bend.components.CustomTopBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val data = profileViewModel.userData.observeAsState()
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.FEED
            )
        },

        ) {
        if (profileViewModel.profileState.value is ProfileState.Success) {
            val userData = (profileViewModel.profileState.value as ProfileState.Success).userData

            Column(modifier = Modifier.fillMaxSize()) {
                CustomTopBar(
                    userData["username"].toString(),
                    icons = listOf (
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
                ProfileSection(userData, profileViewModel)
                Spacer(modifier = Modifier.height(10.dp))
                ButtonSection(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                PostTabView(
                    imageWithTexts = listOf(
                        ImageWithText(
                            image = painterResource(id = R.drawable.future),
                            text = "Posts"
                        ),
                        ImageWithText(
                            image = painterResource(id = R.drawable.time_past),
                            text = "Reels"
                        ),
                    )
                ) {
                    selectedTabIndex = it
                }
                when (selectedTabIndex) {
                    0 -> PostSection(
                        posts = listOf(
                            painterResource(id = R.drawable.lines_menu),
                            painterResource(id = R.drawable.baseline_list_alt_24),
                            painterResource(id = R.drawable.baseline_message_24),
                            painterResource(id = R.drawable.baseline_face_6_24),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (profileViewModel.profileState.value is ProfileState.Loading) {
//            TODO: add loading screen
            {}
        } else if (profileViewModel.profileState.value is ProfileState.Error) {
            val errorMessage =
                (profileViewModel.profileState.value as ProfileState.Error).errorMessage
            Text("Error: $errorMessage")
        }
    }
}

@Composable
fun ProfileSection(userData: Map<String, Any>, profileViewModel: ProfileViewModel) {
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
                image = painterResource(
                    id = R.drawable.baseline_man_24
                ),
                modifier = Modifier
                    .size(100.dp)
                    .weight(3f)
            )
            StatSection(modifier = Modifier.weight(7f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        ProfileDescription(
            displayName = userData["last_name"].toString() + " " + userData["first_name"] + " (" + profileViewModel.accountType + ")",
            phone = userData["phone"].toString(),
            email = userData["email"].toString(),
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
    image: Painter,
    modifier: Modifier = Modifier
) {
    Image(
        painter = image,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
    )
}
@Composable
fun RoundImageNoBorder(
    image: Painter,
    modifier: Modifier = Modifier
) {
    Image(
        painter = image,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .padding(3.dp)
            .clip(CircleShape)
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
    modifier: Modifier = Modifier
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
                        // Handle the click event for the "Follow" button
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
                        // Handle the click event for the "Following" button
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
    posts: List<Painter>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .scale(1.01f)
    ) {
        items(posts.size) {
            Image(
                painter = posts[it],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(
                        width = 1.dp,
                        color = Color.White
                    )
            )
        }
    }
}

data class ImageWithText(
    val image: Painter,
    val text: String
)