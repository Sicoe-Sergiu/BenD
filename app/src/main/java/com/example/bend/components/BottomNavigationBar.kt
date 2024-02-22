package com.example.bend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.ui.theme.Primary
import com.example.bend.ui.theme.Secondary

enum class BottomNavigationItem(val icon: ImageVector, val route: String ){
    FEED(Icons.Default.Home, Constants.NAVIGATION_HOME_PAGE),
    SEARCH(Icons.Default.Search, Constants.NAVIGATION_SEARCH_PAGE),
    PROFILE(Icons.Default.Person, Constants.NAVIGATION_PROFILE_PAGE)
}
@Composable
fun BottomNavigationBar(
    navController: NavController,
    selectedItem: BottomNavigationItem
) {
    Row (modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(
            brush = Brush.horizontalGradient(listOf(Secondary, Primary)),
        )
    ){
        for (item in BottomNavigationItem.values()){
            Image(
                imageVector = item.icon,
                contentDescription = "ImageItem",
                modifier = Modifier
                    .size(55.dp)
                    .weight(1f)
                    .padding(5.dp)
                    .clickable {
                        navController.navigate(item.route)
                    },
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}