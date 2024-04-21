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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.view.components.ActionBarEvent
import com.example.bend.view.components.EventDetails
import com.example.bend.view.components.EventHeader
import com.example.bend.view.components.EventPoster
import com.example.bend.view.components.ExpandIcon
import com.example.bend.view.theme.green
import com.example.bend.viewmodel.HomeViewModel

@Composable
fun EventReposted(
    event: Event,
    founder: EventFounder?,
    artists: List<Artist>,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    navController: NavController,
    whoRepost: String
){
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val errorMessage = viewModel.errorMessages.observeAsState()

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value != ""){
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                viewModel.clearError()
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.LightGray)
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RepostHeader(whoRepost = whoRepost, navController = navController)
            Spacer(modifier = Modifier.height(5.dp))
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))

                    .padding(10.dp)
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                EventHeader(event, founder, navController)
                Spacer(modifier = Modifier.height(10.dp))
                EventPoster(
                    posterUrl = event.posterDownloadLink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .shadow(
                            elevation = 9.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true,
                            ambientColor = Color.Black
                        )
                        .clip(shape = RoundedCornerShape(10.dp))
                        .border(width = 1.dp, green, shape = RoundedCornerShape(10.dp))
                )
                ExpandIcon(expanded) { expanded = !expanded }
                if (expanded) {
                    EventDetails(event, artists, navController = navController)
                }
                ActionBarEvent(viewModel = viewModel, event = event)
            }
        }
        
    }
}
@Composable
fun RepostHeader(
    whoRepost: String,
    navController: NavController
){
    var userType by remember {mutableStateOf("") }
    var userUUID by remember {mutableStateOf("") }
    var userProfilePhoto by remember {mutableStateOf("") }
    var userUsername by remember {mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(key1 = userType){
        userType = HomeViewModel.getAccountType(context , whoRepost)
        when(userType){
            "artist" -> {
                val artist = HomeViewModel.getArtistByUUID(context, whoRepost)
                if (artist != null) {
                    userUUID = artist.uuid
                    userProfilePhoto = artist.profilePhotoURL
                    userUsername = artist.username
                }
            }
            "user" -> {
                val user = HomeViewModel.getUserByUUID(context, whoRepost)
                if (user != null) {
                    userUUID = user.uuid
                    userProfilePhoto = user.profilePhotoURL
                    userUsername = user.username
                }
            }
            "event_founder" -> {
                val founder = HomeViewModel.getFounderByUUID(context, whoRepost)
                if (founder != null) {
                    userUUID = founder.uuid
                    userProfilePhoto = founder.profilePhotoURL
                    userUsername = founder.username
                }
            }
        }
    }
    
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        UserProfile(userUUID = userUUID, userProfilePhotoUrl = userProfilePhoto, userUsername = userUsername, navController = navController)
        Text(text = " reposted this Event:", fontSize = 18.sp)
    }
    
    
}

@Composable
fun UserProfile(userUUID: String, userProfilePhotoUrl: String, userUsername: String, navController: NavController, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .clickable {
                navController.navigate(
                    Constants.userProfileNavigation(
                        userUUID
                    )
                )
            }

    ) {
        RoundImage(
            imageUrl = userProfilePhotoUrl,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = userUsername,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.width(90.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}