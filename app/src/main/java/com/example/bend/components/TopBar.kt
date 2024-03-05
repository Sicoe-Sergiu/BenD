package com.example.bend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bend.ui.theme.Primary
import com.example.bend.ui.theme.Secondary

@Composable
fun CustomTopBar(
    firstIcon: @Composable() (() -> Unit)? = null,
    name: String,
    icons: List<@Composable () -> Unit> = emptyList()
) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(Secondary, Primary)),
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(Secondary, Primary)),
                )
        ) {
            Box(
                modifier = Modifier
                    .weight(5f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,

                ) {
                    if (firstIcon != null) {
                        firstIcon()
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                    Text(
                        text = name,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(5f)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    icons.forEachIndexed { index, icon ->
                        Box(
                            modifier = Modifier
                                .padding(start = if (index > 0) 8.dp else 0.dp)
                        ) {
                            icon()
                        }
                    }
                }
            }
        }
    }
}
