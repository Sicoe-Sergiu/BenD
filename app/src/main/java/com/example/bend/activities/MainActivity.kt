package com.example.bend.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bend.Constants
import com.example.bend.register_login.ResetPasswordScreen
import com.example.bend.register_login.SignInScreen
import com.example.bend.register_login.SignUpScreen
import com.example.bend.ui.screens.AddReviewScreen
import com.example.bend.ui.screens.AddEditEventScreen
import com.example.bend.ui.screens.FeedScreen
import com.example.bend.ui.screens.MyEventsScreen
import com.example.bend.ui.screens.ProfileScreen
import com.example.bend.ui.screens.SearchScreen
import com.example.bend.ui.screens.SingleEventScreen
import com.example.bend.view_models.HomeViewModel
import com.example.bend.view_models.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            val mainViewModel: HomeViewModel = viewModel()
            val profileViewModel: ProfileViewModel = viewModel()

            val startDestination = if (currentUser != null) {
                Constants.NAVIGATION_HOME_PAGE
            } else {
                Constants.NAVIGATION_REGISTER_PAGE
            }

            NavHost(
                navController = navController,
                startDestination = Constants.NAVIGATION_SEARCH_PAGE
            ) {
                // Screen for signing in
                composable(Constants.NAVIGATION_LOGIN_PAGE) {
                    SignInScreen(navController = navController)
                }
                // Screen for signing up
                composable(Constants.NAVIGATION_REGISTER_PAGE) {
                    SignUpScreen(navController = navController)
                }
                // Screen for resetting password
                composable(Constants.NAVIGATION_FORGOT_PASS_PAGE) {
                    ResetPasswordScreen(navController = navController)
                }
                // Home feed screen
                composable(Constants.NAVIGATION_HOME_PAGE) {
                    FeedScreen(navController = navController, homeViewModel = mainViewModel)
                }
                // Profile screen for a specific user
                composable(
                    Constants.NAVIGATION_PROFILE_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_USER_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString(Constants.NAVIGATION_USER_UUID_ARGUMENT)
                    userId?.let { uid ->
                        ProfileScreen(userUUID = uid, navController = navController, viewModel = profileViewModel)
                    }
                }
                // Screen for a single event
                composable(
                    Constants.NAVIGATION_SINGLE_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    eventId?.let { eid ->
                        SingleEventScreen(eventUUID = eid, viewModel = mainViewModel, navController = navController)
                    }
                }
                // Screen for editing an event
                composable(
                    Constants.NAVIGATION_EDIT_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    eventId?.let { eid ->
                        AddEditEventScreen(eventUUID = eid, navController = navController, editMode = true)
                    }
                }
                // Screen for adding a review to an event
                composable(
                    Constants.NAVIGATION_ADD_REVIEW_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    eventId?.let { eid ->
                        AddReviewScreen(eventUUID = eid, navController = navController)
                    }
                }
                // Screen for searching events
                composable(Constants.NAVIGATION_SEARCH_PAGE) {
                    SearchScreen(navController = navController)
                }
                // Screen for creating or editing an event
                composable(Constants.NAVIGATION_CREATE_EVENT_PAGE) {
                    AddEditEventScreen(navController = navController)
                }
                // Screen for displaying user's events
                composable(Constants.NAVIGATION_MY_EVENTS) {
                    MyEventsScreen(navController = navController)
                }
            }
        }
    }
}
