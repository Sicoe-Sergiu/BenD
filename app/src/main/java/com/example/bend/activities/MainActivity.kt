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
import com.example.bend.ui.screens.CreateEventScreen
import com.example.bend.ui.screens.EditEventScreen
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
            var startDestination = Constants.NAVIGATION_REGISTER_PAGE
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            val mainViewModel: HomeViewModel = viewModel()
            val profileViewModel: ProfileViewModel = viewModel()

            if (currentUser != null) {
                startDestination = Constants.NAVIGATION_HOME_PAGE
            }


            NavHost(
                navController = navController,
                startDestination = Constants.NAVIGATION_HOME_PAGE
            ) {
//                LoginScreen
                composable(Constants.NAVIGATION_LOGIN_PAGE) { SignInScreen(navController = navController) }
//                RegisterScreen
                composable(Constants.NAVIGATION_REGISTER_PAGE) { SignUpScreen(navController = navController) }
//                ForgotPasswordScreen
                composable(Constants.NAVIGATION_FORGOT_PASS_PAGE) {
                    ResetPasswordScreen(
                        navController = navController
                    )
                }
//                FeedScreen
                composable(Constants.NAVIGATION_HOME_PAGE) {
                    FeedScreen(
                        navController = navController,
                        homeViewModel = mainViewModel
                    )
                }
//                ProfileScreen
                composable(
                    Constants.NAVIGATION_PROFILE_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_USER_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_USER_UUID_ARGUMENT)
                    if (userId != null) {
                        ProfileScreen(
                            userUUID = userId, navController = navController,
                            viewModel = profileViewModel
                        )
                    }
                }
//                SingleEventScreen
                composable(
                    Constants.NAVIGATION_SINGLE_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    if (eventId != null) {
                        SingleEventScreen(
                            eventUUID = eventId,
                            viewModel = mainViewModel,
                            navController = navController
                        )
                    }
                }
//                editEventScreen
                composable(
                    Constants.NAVIGATION_EDIT_EVENT_PAGE,
                    arguments = listOf(navArgument(Constants.NAVIGATION_EVENT_UUID_ARGUMENT) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString(Constants.NAVIGATION_EVENT_UUID_ARGUMENT)
                    if (eventId != null) {
                        EditEventScreen(
                            eventUUID = eventId,
                            navController = navController
                        )
                    }
                }
//                SearchScreen
                composable(Constants.NAVIGATION_SEARCH_PAGE) { SearchScreen(navController = navController) }
//                CreateEventScreen
                composable(Constants.NAVIGATION_CREATE_EVENT_PAGE) { CreateEventScreen(navController = navController) }
//                MyEventsScreen
                composable(Constants.NAVIGATION_MY_EVENTS) {
                    MyEventsScreen(
                        navController = navController

                    )
                }
            }
        }
    }
}