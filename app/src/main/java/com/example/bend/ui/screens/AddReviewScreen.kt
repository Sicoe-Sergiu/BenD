package com.example.bend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bend.components.ArtistComponent
import com.example.bend.components.CustomSlider
import com.example.bend.components.CustomTopBar
import com.example.bend.components.FounderProfile
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.components.WriteReviewComponent
import com.example.bend.events.AddReviewUIEvent
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.ui.theme.green
import com.example.bend.view_models.AddReviewViewModel

@Composable
fun AddReviewScreen(
    viewModel: AddReviewViewModel = viewModel(),
    navController: NavController,
    eventUUID: String
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.observeAsState()
    val founder by viewModel.founder.observeAsState()
    val artists by viewModel.artists.observeAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadData(eventUUID)
    }
    Scaffold(
        topBar = {
            CustomTopBar(
                {
                    BackButton {
                        navController.popBackStack()
                    }
                },
                text = "Add Review",
                icons = listOf()
            )
        },
        bottomBar = { },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.LightGray)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(50.dp)
                )
            } else {
                ReviewContent(
                    event = event,
                    founder = founder,
                    artists = artists,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ReviewContent(
    event: Event?,
    artists: List<Artist>?,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddReviewViewModel,
    founder: EventFounder?
) {
    Column(
        modifier = modifier
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FounderProfile(
            founder = founder,
            navController = navController,
            modifier = Modifier.padding(10.dp)
        )
        AsyncImage(
            model = event?.posterDownloadLink,
            contentDescription = "poster image",
            modifier = Modifier
                .size(200.dp)
                .shadow(
                    elevation = 9.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = true,
                    ambientColor = Color.Black
                )
                .clip(shape = RoundedCornerShape(10.dp))
                .border(width = 1.dp, green, shape = RoundedCornerShape(12.dp)),
            contentScale = ContentScale.FillBounds
        )
        Spacer(modifier = Modifier.height(10.dp))
        Divider(modifier = Modifier.width(80.dp))
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Please rate the Event:", fontSize = 20.sp)
        CustomSliderWithLabels(viewModel = viewModel, sliderNo = 0)
        WriteReviewComponent(
            labelValue = "Write a review for ${founder?.username} ...",
            onTextSelected = { viewModel.onEvent(AddReviewUIEvent.ReviewsChanged(it, 0)) },
            errorStatus = true,

            )
        Divider(modifier = Modifier.width(80.dp))
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Please rate the Artists:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(10.dp))
        artists?.forEachIndexed { index, artist ->
            ArtistComponent(
                artist = artist,
                modifier = Modifier.width(150.dp),
                navController = navController
            )
            CustomSliderWithLabels(viewModel = viewModel, sliderNo = index + 1)
            WriteReviewComponent(
                labelValue = "Write a review for ${artist.stageName} ...",
                onTextSelected = { viewModel.onEvent(AddReviewUIEvent.ReviewsChanged(it, index + 1)) },
                errorStatus = true,
                )
        }
        MyButtonComponent(
            value = "Add Review",
            onButtonClicked = { viewModel.onEvent(AddReviewUIEvent.AddReviewButtonClicked(navController)) },
            modifier = Modifier.padding(15.dp),
            isEnabled = true
        )
    }
}

@Composable
fun CustomSliderWithLabels(viewModel: AddReviewViewModel, sliderNo: Int) {
    val sliderRange = 0f..5f


    Column(
        modifier = Modifier
            .padding(16.dp)
            .shadow(
                elevation = 9.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true,
                ambientColor = Color.Black
            )
            .background(color = Color.LightGray, shape = RoundedCornerShape(12.dp))
    ) {
        CustomSlider(
            range = sliderRange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onValueChange = {
                viewModel.onEvent(AddReviewUIEvent.SlidersChanged(it, sliderNo))
            }

        )
        SliderLabels(
            range = sliderRange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SliderLabels(range: ClosedFloatingPointRange<Float>, modifier: Modifier = Modifier) {
    val labels = remember(range) { (range.start.toInt()..range.endInclusive.toInt()).toList() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(text = label.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}