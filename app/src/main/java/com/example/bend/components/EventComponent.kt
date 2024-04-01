package com.example.bend.components

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bend.model.Event
import com.example.bend.R
import com.example.bend.model.Artist
import com.example.bend.model.EventFounder
import com.example.bend.ui.screens.RoundImage
import com.example.bend.ui.screens.RoundImageNoBorder
import com.example.bend.view_models.HomeViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import com.example.bend.Constants
import com.example.bend.ui.theme.green

@Composable
fun EventComponent(
    event: Event,
    founder: EventFounder?,
    artists: List<Artist>,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    navController: NavController,
) {
    var expanded by remember { mutableStateOf(false) }

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
            EventHeader(event, founder, navController)
            Spacer(modifier = Modifier.height(10.dp))
            EventPoster(
                posterUrl = event.posterDownloadLink,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .shadow(elevation = 9.dp, shape = RoundedCornerShape(10.dp), clip = true, ambientColor = Color.Black)
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

@Composable
fun EventHeader(
    event: Event,
    founder: EventFounder?,
    navController: NavController,
    modifier: Modifier = Modifier

) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FounderProfile(founder, navController)

        Spacer(modifier = Modifier.weight(1f))

        EventDate(event.startDate)
    }
}

@Composable
fun FounderProfile(founder: EventFounder?, navController: NavController, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .clickable {
                navController.navigate(
                    Constants.userProfileNavigation(
                        founder?.uuid ?: "Invalid profile UUID"
                    )
                )
            }

    ) {
        RoundImage(
            imageUrl = founder?.profilePhotoURL ?: "",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = founder?.username ?: "",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.width(140.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun EventDate(date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.calendar),
            contentDescription = "calendar icon",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = date,
            fontWeight = FontWeight.Light,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ExpandIcon(expanded: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(25.dp)
    ) {
        Icon(
            imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Toggle expanded",
            tint = Color.Gray
        )
    }
}

@Composable
fun EventPoster(posterUrl: String, modifier: Modifier) {
    AsyncImage(
        model = posterUrl,
        contentDescription = "poster image",
        modifier = modifier,
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun EventDetails(event: Event, artists: List<Artist>, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DetailItem(icon = R.drawable.location, text = event.location)
        DetailItem(
            icon = R.drawable.baseline_access_time_24,
            text = "${event.startTime} - ${event.endTime}"
        )
        DetailItem(icon = R.drawable.money, text = "${event.entranceFee} RON")

        ArtistsSection(artists = artists, navController = navController)
    }
}

@Composable
fun DetailItem(@DrawableRes icon: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 1.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ArtistsSection(artists: List<Artist>, navController: NavController) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.people),
            contentDescription = "artists icon",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (artists.size > 1) "Artists:" else "Artist:",
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
        )
    }
    ArtistsComponentList(artists = artists, navController = navController)
}

@Composable
fun ArtistsComponentList(artists: List<Artist>, navController: NavController) {
    val chunkedArtists = artists.chunked(3)
    chunkedArtists.forEach { artistChunk ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            artistChunk.forEach { artist ->
                ArtistComponent(
                    modifier = Modifier
                        .widthIn(max = 122.dp)
                        .padding(horizontal = 4.dp),
                    artist = artist,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ArtistComponent(modifier: Modifier = Modifier, artist: Artist, navController: NavController? = null) {
    Row(
        modifier = modifier
            .background(Color.White)
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = Color.Black, shape = CircleShape)
            .height(40.dp)
            .padding(3.dp)
            .clickable { navController?.navigate(Constants.userProfileNavigation(artist.uuid)) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundImageNoBorder(
            imageURL = artist.profilePhotoURL,
            modifier = Modifier
                .size(40.dp)
                .weight(0.3f)
        )
        Text(
            text = artist.stageName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.7f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun ActionBarEvent(
    viewModel: HomeViewModel,
    event: Event
) {
    var attend by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val operationMessage by viewModel.operationCompletedMessage.observeAsState()

    LaunchedEffect(key1 = event) {
        attend = viewModel.ifAttend(event)
    }

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.operationCompletedMessage.value = null
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            text = "Are you sure you want to repost this for all your followers?",
            onConfirm = {
                viewModel.repostEvent(event)
                showConfirmationDialog = false
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    ActionBarLayout(
        attend = attend,
        onAttendClick = {
            attend = !attend
            if (attend) {
                viewModel.addEventToUserList(event)
            } else {
                viewModel.removeEventFromUserList(event)
            }
        },
        attendeesCount = viewModel.eventsAttendees.observeAsState().value?.find { it.first == event }?.second
            ?: 0,
        onRepostClick = { showConfirmationDialog = true },
        enabled = viewModel.accountType.value == "user"
    )
}


@Composable
fun ActionBarLayout(
    attend: Boolean,
    onAttendClick: () -> Unit,
    attendeesCount: Int,
    onRepostClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(10.dp), clip = true, ambientColor = Color.Black)

            .border(1.dp, green, shape = RoundedCornerShape(16.dp))
            .background(color = Color.White, shape = RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        if (enabled)
            MyIconButton(
                painter = painterResource(
                    id = if (attend) R.drawable.attend_checked else R.drawable.attend_uncheckedpng
                ),
                onClick = onAttendClick,
                enabled = enabled,
                modifier = Modifier

            )


        Text(
            text = "$attendeesCount People Attend",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically),
            maxLines = 1
        )


        MyIconButton(
            painter = painterResource(id = R.drawable.repost),
            onClick = onRepostClick,
            modifier = Modifier,
            enabled = true
        )
    }
}

@Composable
fun ConfirmationDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        text = {
            Text(
                text = text,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        },
        onDismissRequest = onDismiss,
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Red)
                }
                TextButton(onClick = onConfirm) {
                    Text("Confirm", color = Color.Green)
                }
            }
        }
    )
}


@Composable
fun MyIconButton(
    onClick: () -> Unit,
    painter: Painter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(5.dp)
            .alpha(alpha),
        enabled = enabled
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}