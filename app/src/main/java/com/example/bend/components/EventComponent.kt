package com.example.bend.components

import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bend.model.Event
import com.example.bend.R
import com.example.bend.model.Artist
import com.example.bend.model.EventFounder
import com.example.bend.ui.screens.RoundImage
import com.example.bend.ui.screens.RoundImageNoBorder
import com.example.bend.ui.theme.PrimaryText
import java.util.UUID

@Composable
fun EventComponent(
    event: Event,
    founder: EventFounder?,
    artists:List<Artist>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(15.dp)
            .clip(shape = RoundedCornerShape(16.dp))

    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(30.dp))
                        .clickable {
//                            TODO:click
                        }
                ) {
                    RoundImage(
                        imageUrl = founder?.profilePhotoURL ?: "",
                        modifier = Modifier
                            .size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = founder?.username ?: "",
                        modifier = Modifier,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(50.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.calendar),
                        contentDescription = "calendar icon",
                        modifier = Modifier.size(20.dp)

                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = event.startDate,
                        modifier = Modifier,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Poster(
                posterUrl = event.posterDownloadLink,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .clip(shape = RoundedCornerShape(10.dp)),
            )
            Box(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                    }
            )
            {
                if (expanded) Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)
                ) else Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)

                )
            }

            if (expanded) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.location),
                                contentDescription = "location icon",
                                modifier = Modifier
                                    .size(20.dp)
                                    .height(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = event.location,
                                modifier = Modifier,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_access_time_24),
                                contentDescription = "money icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = event.startTime + " - " + event.endTime,
                                modifier = Modifier,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.money),
                            contentDescription = "money icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = event.entranceFee.toString(),
                            modifier = Modifier,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "RON",
                            modifier = Modifier,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                ArtistsSection(artists)
                Spacer(modifier = Modifier.height(5.dp))
            }
            ActionBarEvent()
        }
    }

}

@Composable
fun Poster(posterUrl: String, modifier: Modifier) {
    AsyncImage(
        model = posterUrl,
        contentDescription = "poster image",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ArtistsSection(
    artists: List<Artist>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.people),
            contentDescription = "artists icon",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        if (artists.size > 1)
            Text(
                text = "Artists:",
                modifier = Modifier,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp
            )
        else
            Text(
                text = "Artist:",
                modifier = Modifier,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp
            )
    }
    ArtistsComponentList(artists = artists)
}

@Composable
fun ArtistsComponentList(
    artists: List<Artist>
) {
    val chunkedArtists = artists.chunked(3)
    chunkedArtists.forEach { artistChunk ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,

            ) {
            artistChunk.forEach { artist ->
                ArtistComponent(
                    modifier = Modifier
                        .widthIn(max = 122.dp)
                        .padding(horizontal = 4.dp)
                    ,
                    artist = artist
                )
            }
        }
    }
}

@Composable
fun ArtistComponent(
    modifier: Modifier,
    artist: Artist
) {
    Row(
        modifier = modifier
            .background(Color.White)
            .clip(
                RoundedCornerShape(
                    topStart = CornerSize(16.dp),
                    bottomStart = CornerSize(16.dp),
                    topEnd = CornerSize(16.dp),
                    bottomEnd = CornerSize(16.dp)
                )
            )
            .clickable {
//              TODO:navigate to artist profile
            }
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = CircleShape
            )
            .height(40.dp)
            .padding(3.dp),
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.7f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun showToast(context: android.content.Context, message: String) {
    val density = LocalDensity.current.density
    val yOffset = (64 * density).toInt()

    Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, yOffset)
    }.show()
}

@Composable
fun ActionBarEvent(
) {
    var attend by remember { mutableStateOf(false) }
//    TODO: give value to attend
    var showMessage by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("false") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    if (showMessage) {
        showToast(context = context, message = toastMessage)
        showMessage = false
    }

    if (showConfirmationDialog) {
        AlertDialog(
            text = {
                Text(
                    text = "Are you sure you want to repost this for all your followers?",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            },
            onDismissRequest = {
                showConfirmationDialog = false
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            // TODO: Handle confirmation action
                            showConfirmationDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Green)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, shape = RoundedCornerShape(16.dp))
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            ),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MyIconButton(
            painter = painterResource(
                if (attend) {
                    R.drawable.attend_checked
                } else {
                    R.drawable.attend_uncheckedpng
                }
            ),
            text = "",
            onClick = {
                if (attend) {
                    toastMessage = "Event removed from your list."
                    showMessage = true
                } else {
                    toastMessage = "Event added to your list."
                    showMessage = true
                }
                attend = !attend
            },
            modifier = Modifier
        )
        Text(
//            TODO:give the actual number for
            text = "4556 People Attend",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically),
            maxLines = 1
        )

        MyIconButton(
            painter = painterResource(id = R.drawable.repost),
            text = "",
            onClick = {
                showConfirmationDialog = true
            },
            modifier = Modifier
        )
    }
}


@Composable
fun MyIconButton(
    onClick: () -> Unit,
    painter: Painter,
    text: String,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = CornerSize(16.dp),
                    bottomStart = CornerSize(16.dp),
                    topEnd = CornerSize(16.dp),
                    bottomEnd = CornerSize(16.dp)
                )
            )
            .clickable { onClick.invoke() }
            .padding(5.dp)

    ) {
        Text(
            text = text,
            modifier = Modifier,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
    }
}