package com.example.bend.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen (
    navController: NavController,
){

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavigationBar(navController = navController, selectedItem = BottomNavigationItem.FEED)
        },

        ) {
        Text(text = "Search Screen")
    }
}