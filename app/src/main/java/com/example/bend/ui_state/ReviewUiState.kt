package com.example.bend.ui_state

data class ReviewUiState (
    var rates: MutableList<Float> = MutableList(100) { 2.5f },
    var reviews: MutableList<String> = MutableList(100) { "" },
)