package com.example.bend.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bend.Constants
import com.example.bend.view.screens.ResetPasswordScreen
import com.example.bend.view.screens.LoginScreen
import com.example.bend.view.screens.RegisterScreen
import com.example.bend.view.screens.AddReviewScreen
import com.example.bend.view.screens.AddEditEventScreen
import com.example.bend.view.screens.FeedScreen
import com.example.bend.view.screens.MyEventsScreen
import com.example.bend.view.screens.ProfileScreen
import com.example.bend.view.screens.SearchScreen
import com.example.bend.view.screens.SingleEventScreen
import com.example.bend.viewmodel.HomeViewModel
import com.example.bend.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bend.view.screens.FounderReviewsScreen
import com.example.bend.view.screens.NotificationsScreen
import com.example.bend.view.screens.SetProfilePhotoScreen
import com.example.bend.viewmodel.HomeViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {

            val navController = rememberNavController()
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser


            val mainViewModel: HomeViewModel = HomeViewModelFactory(appContext = this).create(HomeViewModel::class.java)
            val profileViewModel: ProfileViewModel = viewModel()

            val startDestination = if (currentUser != null) {
                Constants.NAVIGATION_HOME_PAGE
            } else {
                Constants.NAVIGATION_REGISTER_PAGE
            }



            NavHost(
                navController = navController,
                startDestination = Constants.NAVIGATION_HOME_PAGE
            ) {
                // Screen for signing in
                composable(Constants.NAVIGATION_LOGIN_PAGE) {
                    LoginScreen(navController = navController)
                }
                // Screen for signing up
                composable(Constants.NAVIGATION_REGISTER_PAGE) {
                    RegisterScreen(navController = navController)
                }
                // Screen for set profile photo
                composable(Constants.NAVIGATION_SET_PROFILE_PHOTO_PAGE) {
                    SetProfilePhotoScreen(navController = navController)
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
                    val userId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_USER_UUID_ARGUMENT)
                    userId?.let { uid ->
                        ProfileScreen(
                            userUUID = uid,
                            navController = navController,
                            profileViewModel = profileViewModel
                        )
                    }
                }
                // Screen for a single event
                composable(
                    Constants.NAVIGATION_SINGLE_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    eventId?.let { eid ->
                        SingleEventScreen(
                            eventUUID = eid,
                            viewModel = mainViewModel,
                            navController = navController
                        )
                    }
                }
                // Screen for editing an event
                composable(
                    Constants.NAVIGATION_EDIT_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    eventId?.let { eid ->
                        AddEditEventScreen(
                            eventUUID = eid,
                            navController = navController,
                            editMode = true
                        )
                    }
                }
                // Screen for editing an user
                composable(
                    Constants.NAVIGATION_EDIT_USER_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_USER_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userUID =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_USER_UUID_ARGUMENT)
                    userUID?.let { uid ->
                        RegisterScreen(
                            userUUID = uid,
                            navController = navController,
                            editMode = true
                        )
                    }
                }
                // Screen for founder reviews
                composable(
                    Constants.NAVIGATION_FOUNDER_REVIEWS_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_USER_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userUID =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_USER_UUID_ARGUMENT)
                    userUID?.let { uid ->
                        FounderReviewsScreen(
                            founderUUID = uid,
                            navController = navController
                        )
                    }
                }
                // Screen for adding a review to an event
                composable(
                    Constants.NAVIGATION_ADD_REVIEW_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
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
                // Screen for displaying user's notifications
                composable(Constants.NAVIGATION_NOTIFICATIONS_PAGE) {
                    NotificationsScreen(
                        homeViewModel = mainViewModel,
                        navController = navController
                    )
                }
            }

        }
    }
}
