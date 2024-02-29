package com.example.bend.events

import android.net.Uri
import androidx.navigation.NavController
import com.example.bend.model.Artist
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

sealed class CreateEventUIEvent {
    data class PosterChanged(val posterUri: Uri) : CreateEventUIEvent()
    data class EntranceFeeChanged(val entranceFee:Int) : CreateEventUIEvent()
    data class LocationChanged(val location:String) : CreateEventUIEvent()
    data class StartDateChanged(val startDate: LocalDate) : CreateEventUIEvent()
    data class EndDateChanged(val endDate:LocalDate) : CreateEventUIEvent()
    data class StartTimeChanged(val startTime: LocalTime) : CreateEventUIEvent()
    data class EndTimeChanged(val endTime:LocalTime) : CreateEventUIEvent()
    data class ArtistsUsernamesChanged(val artistsUsernames:List<String>) : CreateEventUIEvent()

    data class CreateEventButtonClicked(val navController: NavController) : CreateEventUIEvent()

}