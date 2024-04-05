package com.example.bend.events

import androidx.navigation.NavController
import com.example.bend.model.Event

sealed class AddReviewUIEvent{

    data class SlidersChanged(val sliderValue:Float, val sliderNo: Int) : AddReviewUIEvent()
    data class ReviewsChanged(val reviewText:String, val reviewNo: Int) : AddReviewUIEvent()


    data class AddReviewButtonClicked(val navController: NavController) : AddReviewUIEvent()
}