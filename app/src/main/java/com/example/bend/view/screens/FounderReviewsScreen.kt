package com.example.bend.view.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.EventDate
import com.example.bend.model.Event
import com.example.bend.model.Review
import com.example.bend.model.User
import com.example.bend.viewmodel.ReviewsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FounderReviewsScreen(
    founderUUID: String,
    navController: NavController,
    reviewsViewModel: ReviewsViewModel = viewModel()
) {
    LaunchedEffect(key1 = founderUUID) {
        reviewsViewModel.loadData(founderUUID)
    }

    Scaffold(
        topBar = {
            CustomTopBar({
                BackButton {
                    navController.popBackStack()
                }
            }, text = "Reviews", icons = listOf {})
        },
        bottomBar = {},

        ) { innerPadding ->
        ReviewsList(
            modifier = Modifier.padding(innerPadding),
            reviewsViewModel = reviewsViewModel,
            navController = navController,
            founderUUID = founderUUID
        )
    }
}

@Composable
fun ReviewsList(
    modifier: Modifier = Modifier,
    reviewsViewModel: ReviewsViewModel,
    navController: NavController,
    founderUUID: String
) {
    val isRefreshing by reviewsViewModel.isLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val events = reviewsViewModel.events.observeAsState(emptyList())

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { reviewsViewModel.loadData(founderUUID) },
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        val sortedEvents = events.value.sortedByDescending { it.startDate }
        if (events.value.isEmpty()) EmptyPlaceholder(text = "No Reviews to display.") else {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(sortedEvents) { _, event ->

                    val reviews = remember { mutableStateOf<List<Review>?>(emptyList()) }

                    LaunchedEffect(key1 = event.founderUUID) {
                        reviews.value = ReviewsViewModel.getReviewsForEventAndFounder(event.uuid, founderUUID)
                    }
//                    Log.d("USER ID", founderUUID)
//                    Log.d("EVENT", event.toString())
//                    Log.d("REVIEWS", reviews.value.toString())

                    if (reviews.value!!.isNotEmpty()) {
                        EventAndReviews(
                            event = event,
                            reviews = reviews.value ?: emptyList(),
                            viewModel = reviewsViewModel,
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventAndReviews(
    event: Event,
    reviews: List<Review>,
    viewModel: ReviewsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(10.dp)
                .clickable { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventDate(event.startDate)
                val sortedReviews = reviews.sortedByDescending { it.creationTimestamp }
                for (review in sortedReviews) {
                    ReviewComponent(
                        review = review,
                        navController = navController,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }
        }
    }
}

@Composable
fun ReviewComponent(
    review: Review,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val writer = remember { mutableStateOf<User?>(null) }

    // Fetch the writer's information based on the review's writer UUID
    LaunchedEffect(key1 = review.writerUUID) {
        writer.value = ReviewsViewModel.getUserByUUID(review.writerUUID)
    }

    // Main container for the review component
    Column(modifier = modifier.padding(16.dp)) {
        // User profile section
        UserProfile(user = writer.value, navController = navController)

        Spacer(modifier = Modifier.height(8.dp))

        // Review text section
        ReviewText(text = review.reviewText)
    }
}

@Composable
fun ReviewText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
    )
}

@Composable
fun UserProfile(
    user: User?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .clickable {
                navController.navigate(
                    Constants.userProfileNavigation(
                        user?.uuid ?: "Invalid profile UUID"
                    )
                )
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundImage(
            imageUrl = user?.profilePhotoURL ?: "",
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = user?.username ?: "",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(140.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
