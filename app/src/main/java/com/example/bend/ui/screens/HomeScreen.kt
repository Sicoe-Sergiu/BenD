package com.example.bend.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.R
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem
import com.example.bend.components.CustomTopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            CustomTopBar(
                name = "BenD",
                icons = listOf {
                })
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.FEED
            )
        },

        ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {

        }
    }
}