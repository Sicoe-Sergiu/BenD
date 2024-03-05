package com.example.bend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.ui.theme.Primary
import com.example.bend.ui.theme.Secondary
import com.example.bend.ui.theme.green
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalTime

enum class BottomNavigationItem(val icon: ImageVector, val route: String){
    FEED(Icons.Default.Home, Constants.NAVIGATION_HOME_PAGE),
    SEARCH(Icons.Default.Search, Constants.NAVIGATION_SEARCH_PAGE),
    PROFILE(Icons.Default.Person, Constants.userProfileNavigation(FirebaseAuth.getInstance().uid.toString()))
}
@Composable
fun BottomNavigationBar(
    navController: NavController,
    selectedItem: BottomNavigationItem,
) {
    var selected by remember { mutableStateOf(selectedItem) }

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
                    .size(45.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .weight(1f)
                    .clickable {
                        selected = item
                        navController.navigate(item.route)
                    },
                colorFilter = ColorFilter.tint(if (item == selected) Color.Black else Color.DarkGray)
            )
        }
    }
}