package com.example.bend.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bend.Constants
import com.example.bend.register_login.ForgotPasswordScreen
import com.example.bend.register_login.SignInScreen
import com.example.bend.register_login.SignUpScreen
import com.example.bend.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()


            NavHost(navController = navController, startDestination = Constants.NAVIGATION_LOGIN_PAGE){
//                LoginScreen
                composable(Constants.NAVIGATION_LOGIN_PAGE){SignInScreen(navController = navController)}
//                RegisterScreen
                composable(Constants.NAVIGATION_REGISTER_PAGE){SignUpScreen(navController = navController)}
//                ForgotPasswordScreen
                composable(Constants.NAVIGATION_FORGOT_PASS_PAGE){ForgotPasswordScreen(navController = navController)}
//                HomeScreen
                composable(Constants.NAVIGATION_HOME_PAGE){HomeScreen(navController = navController)}
            }
        }
    }
}