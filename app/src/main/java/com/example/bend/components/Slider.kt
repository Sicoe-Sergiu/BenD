package com.example.bend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.SliderValueHorizontal
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.LayoutDirection


@Composable
fun CustomSliderWithLabels(){

}
@Composable
fun CustomSlider(modifier: Modifier = Modifier, range: ClosedFloatingPointRange<Float>) {

    var stateSlider3 by remember { mutableStateOf(2.5f) }
    SliderValueHorizontal(
        stateSlider3, { stateSlider3 = it },
        valueRange = range,
        modifier = modifier
            .clip(shape = RoundedCornerShape(30.dp))
            .fillMaxWidth()
            .background(Color.LightGray)
            .height(35.dp)
        ,
        thumbHeightMax = true,
        track = { modifier: Modifier,
                  fraction: Float,
                  interactionSource: MutableInteractionSource,
                  tickFractions: List<Float>,
                  enabled: Boolean ->

            DefaultTrack1(
                modifier
                    .clip(shape = RoundedCornerShape(30.dp))
                ,
                fraction,
                interactionSource,
                tickFractions,
                enabled,
                height = 4.dp
            )
        }, thumb = { modifier: Modifier,
                     offset: Dp,
                     interactionSource: MutableInteractionSource,
                     enabled: Boolean,
                     thumbSize: DpSize ->

            DefaultThumb(
                modifier
                    .clip(shape = RoundedCornerShape(30.dp))
                , offset, interactionSource, enabled, thumbSize,
                color = Color.Black,
                scaleOnPress = 1.3f
            )
        },
        thumbSizeInDp = DpSize(5.dp, 18.dp),

    )
}


@Composable
fun DefaultTrack1(
    modifier: Modifier,
    progress: Float,
    interactionSource: MutableInteractionSource,
    tickFractions: List<Float>,
    enabled: Boolean,
    colorTickTrack: Color = Color(0xff7000F8),
    height: Dp = 4.dp
) {
    Canvas(
        Modifier
            .then(modifier)
            .height(height)

    ) {

        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight

        val trackBrush = Brush.horizontalGradient(
            colors = listOf(Color.Red, Color.Yellow, Color.Green),
            startX = sliderStart.x,
            endX = sliderEnd.x
        )

        drawLine(
            trackBrush,
            sliderStart,
            sliderEnd,
            size.height,
            StrokeCap.Round,
            alpha = if (enabled) 1f else ALPHA_WHEN_DISABLED
        )


    }
}

private const val ALPHA_WHEN_DISABLED = 0.6f