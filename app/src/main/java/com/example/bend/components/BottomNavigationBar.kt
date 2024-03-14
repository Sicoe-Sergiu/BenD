package com.example.bend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.ui.theme.Primary
import com.example.bend.ui.theme.Secondary
import com.example.bend.ui.theme.green
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalTime

data class BottomNavigationItem1(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar2(
    navController: NavController,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        BottomNavigationItem1("Home", Icons.Filled.Home, Icons.Outlined.Home, Constants.NAVIGATION_HOME_PAGE),
        BottomNavigationItem1("Search", Icons.Filled.Search, Icons.Outlined.Search, Constants.NAVIGATION_SEARCH_PAGE),
        BottomNavigationItem1("My Events", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, Constants.NAVIGATION_MY_EVENTS),
        BottomNavigationItem1("Profile", Icons.Filled.Person, Icons.Outlined.Person, Constants.userProfileNavigation(FirebaseAuth.getInstance().uid.toString())),
    )

    NavigationBar (
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(listOf(Secondary, Primary)),
        )
//            .drawWithContent {
//                drawContent()
//                drawLine(
//                    color = green,
//                    start = Offset(0f, 0f),
//                    end = Offset(size.width, 0f),
//                    strokeWidth = 0.dp.toPx()
//                )
//            }
            .height(50.dp),
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        tonalElevation = 50.dp
    ){
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    onItemSelected(index)
                    navController.navigate(item.route)
                },
                modifier = Modifier
                    .padding(top = 0.dp)
                ,
                alwaysShowLabel = false,
                colors = NavigationBarItemColors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.DarkGray,
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.LightGray,
                    disabledIconColor = Color.LightGray,
                    disabledTextColor = Color.LightGray,
                    selectedIndicatorColor = green
                    ),
                label = {
                    Text(text = item.title, fontSize = 10.sp)
                },
                icon = {
                    Box (
                    ){
                        Icon(
                            modifier = Modifier.size(25.dp)

                            ,
                            imageVector = if (index == selectedItemIndex) {
                                item.selectedIcon
                            } else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                }
            )
        }
    }
}