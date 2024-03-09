package com.example.bend.ui.screens

import com.example.bend.components.ArtistsSelectSearchInputText
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bend.components.AddPosterButton
import com.example.bend.components.BottomNavigationBar
import com.example.bend.components.BottomNavigationItem
import com.example.bend.components.CustomTopBar
import com.example.bend.components.DatePicker
import com.example.bend.components.MyButtonComponent
import com.example.bend.components.MyNumberPickerComponent
import com.example.bend.components.MyTextFieldComponent
import com.example.bend.components.TimePicker
import com.example.bend.events.CreateEventUIEvent
import com.example.bend.ui.theme.PrimaryText
import com.example.bend.view_models.CreateEventViewModel
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    createEventViewModel: CreateEventViewModel = viewModel()
) {
    val artistsStageNamesState = createEventViewModel.artistsStageNamesLiveData.observeAsState()
    val artistsStageNames: List<String> = artistsStageNamesState.value ?: emptyList()
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val scrollState = rememberScrollState()
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            run {
                selectedImageUri = uri
                selectedImageUri?.let {
                    CreateEventUIEvent.PosterChanged(
                        it
                    )
                }?.let { createEventViewModel.onEvent(it) }
            }
        }
    )
    Scaffold(
        topBar = {
            CustomTopBar(
                text = "BenD",
                icons = listOf {
                })
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = BottomNavigationItem.FEED,

            )
        },

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 55.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    AddPosterButton(
                        value = "Add Poster",
                        onButtonClicked = {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        isEnabled = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if (selectedImageUri != null)
                        Box(
                            modifier = Modifier
                                .size(height = 400.dp, width = 250.dp)
                                .shadow(
                                    10.dp,
                                    shape = RoundedCornerShape(10.dp),
                                )
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Poster",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(10.dp))
                                    .border(
                                        2.dp,
                                        Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    if (selectedImageUri != null)
                        Spacer(modifier = Modifier.height(20.dp))
                    MyTextFieldComponent(
                        labelValue = "Location",
                        onTextSelected = {
                            createEventViewModel.onEvent(
                                CreateEventUIEvent.LocationChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = createEventViewModel.createEventUiState.value.locationError
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MyNumberPickerComponent(
                            labelValue = "Entrance Fee",
                            onNumberSelected = {
                                createEventViewModel.onEvent(
                                    CreateEventUIEvent.EntranceFeeChanged(
                                        it.toInt()
                                    )
                                )
                            },
                            errorStatus = createEventViewModel.createEventUiState.value.entranceFeeError,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = "RON",
                            modifier = Modifier
                                .weight(0.2f)
                                .padding(start = 15.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    DatePicker(
                        labelValue = "Start Date",
                        onTextSelected = {
                            createEventViewModel.onEvent(CreateEventUIEvent.StartDateChanged(it))
                        },
                        errorStatus = createEventViewModel.createEventUiState.value.startDateError,
                        LocalDate.now().minusDays(1)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    DatePicker(
                        labelValue = "End Date",
                        onTextSelected = {
                            createEventViewModel.onEvent(
                                CreateEventUIEvent.EndDateChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = createEventViewModel.createEventUiState.value.endDateError,
                        createEventViewModel.createEventUiState.value.startDate
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TimePicker(
                        labelValue = "Start Time",
                        onTextSelected = {
                            createEventViewModel.onEvent(
                                CreateEventUIEvent.StartTimeChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = createEventViewModel.createEventUiState.value.startTimeError
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TimePicker(
                        labelValue = "End Time",
                        onTextSelected = {
                            createEventViewModel.onEvent(
                                CreateEventUIEvent.EndTimeChanged(
                                    it
                                )
                            )
                        },
                        errorStatus = createEventViewModel.createEventUiState.value.endTimeError
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    ArtistsSelectSearchInputText(
                        listOfItems = artistsStageNames,
                        modifier = Modifier.fillMaxWidth(),
                        onArtistsChanged = {
                            createEventViewModel.onEvent(CreateEventUIEvent.ArtistsUsernamesChanged(it))
                        },
                        enable = true,
                        placeholder = "Artists...",
                        colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryText,
                            focusedLabelColor = PrimaryText,
                            cursorColor = PrimaryText
                        ),
                        dropdownItem = { name ->
//                            TODO:add profile photo
                            Text(name, modifier = Modifier.weight(0.9f))
                        },
                        isError = createEventViewModel.createEventUiState.value.artistsUsernamesError
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    MyButtonComponent(
                        value = "Add Event",
                        onButtonClicked = {
                            createEventViewModel.onEvent(CreateEventUIEvent.CreateEventButtonClicked(navController))
                        },
                        is_enabled = true
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
