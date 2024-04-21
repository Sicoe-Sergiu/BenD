package com.example.bend.view.screens

import com.example.bend.view.components.ArtistsSelectSearchInputText
import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bend.view.components.AddPosterButton
import com.example.bend.view.components.ArtistComponent
import com.example.bend.view.components.CustomTopBar
import com.example.bend.view.components.DatePicker
import com.example.bend.view.components.MyButtonComponent
import com.example.bend.view.components.MyNumberPickerComponent
import com.example.bend.view.components.MyTextFieldComponent
import com.example.bend.view.components.TimePicker
import com.example.bend.model.events.CreateEventUIEvent
import com.example.bend.model.Artist
import com.example.bend.view.theme.PrimaryText
import com.example.bend.viewmodel.AddEditEventViewModel
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    navController: NavController,
    addEditEventViewModel: AddEditEventViewModel = viewModel(),
    editMode: Boolean = false,
    eventUUID: String? = null
) {
    val context = LocalContext.current
    val errorMessage = addEditEventViewModel.errorMessages.observeAsState()
    val isRefreshing by addEditEventViewModel.isLoading.observeAsState(initial = false)
    val isLoading = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(errorMessage.value) {
        isLoading.value = false
        if (errorMessage.value != "") {
            errorMessage.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).apply {
                    show()
                }
                addEditEventViewModel.clearError()
            }
        }
    }


    val artistsLiveData = addEditEventViewModel.artistsLiveData.observeAsState()
    var isUiStatePopulated by remember { mutableStateOf(true) }


    val artists: List<Artist> = artistsLiveData.value ?: emptyList()
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var posterButtonText = "Add Poster"
    var saveButtonText = "Add Event"

    if (editMode) {
        posterButtonText = "Edit Poster"
        saveButtonText = "Save Changes"
        LaunchedEffect(key1 = 1) {
            isUiStatePopulated = false
            addEditEventViewModel.populateUiState(eventUUID = eventUUID)
            selectedImageUri = addEditEventViewModel.createEventUiState.value.posterUri
            isUiStatePopulated = true
        }
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
                }?.let { addEditEventViewModel.onEvent(it) }
            }
        }
    )

    if (!isUiStatePopulated) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                CustomTopBar(
                    {
                        BackButton {
                            navController.popBackStack()
                        }
                    },
                    text = "Create Event",
                    icons = listOf()
                )
            },
            bottomBar = {
            },

            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {

                if (isRefreshing || isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
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
                                value = posterButtonText,
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
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.LocationChanged(
                                            it
                                        )
                                    )
                                },
                                initialValue = addEditEventViewModel.createEventUiState.value.location,
                                errorStatus = addEditEventViewModel.createEventUiState.value.locationError
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
                                        addEditEventViewModel.onEvent(
                                            CreateEventUIEvent.EntranceFeeChanged(
                                                it
                                            )
                                        )
                                    },
                                    errorStatus = addEditEventViewModel.createEventUiState.value.entranceFeeError,
                                    modifier = Modifier.weight(0.8f),
                                    initialValue = addEditEventViewModel.createEventUiState.value.entranceFee
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
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.StartDateChanged(
                                            it
                                        )
                                    )
                                },
                                errorStatus = addEditEventViewModel.createEventUiState.value.startDateError,
                                graterThan = LocalDate.now().minusDays(1),
                                initialValue = addEditEventViewModel.createEventUiState.value.startDate
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            DatePicker(
                                labelValue = "End Date",
                                onTextSelected = {
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.EndDateChanged(
                                            it
                                        )
                                    )
                                },
                                errorStatus = addEditEventViewModel.createEventUiState.value.endDateError,
                                graterThan = addEditEventViewModel.createEventUiState.value.startDate,
                                initialValue = addEditEventViewModel.createEventUiState.value.endDate
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            TimePicker(
                                labelValue = "Start Time",
                                onTextSelected = {
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.StartTimeChanged(
                                            it
                                        )
                                    )
                                },
                                errorStatus = addEditEventViewModel.createEventUiState.value.startTimeError,
                                initialValue = addEditEventViewModel.createEventUiState.value.startTime

                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            TimePicker(
                                labelValue = "End Time",
                                onTextSelected = {
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.EndTimeChanged(
                                            it
                                        )
                                    )
                                },
                                errorStatus = addEditEventViewModel.createEventUiState.value.endTimeError,
                                initialValue = addEditEventViewModel.createEventUiState.value.endTime

                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            ArtistsSelectSearchInputText(
                                listOfItems = artists,
                                modifier = Modifier.fillMaxWidth(),
                                onArtistsChanged = {
                                    addEditEventViewModel.onEvent(
                                        CreateEventUIEvent.ArtistsChanged(
                                            it
                                        )
                                    )
                                },
                                enable = true,
                                placeholder = "Artists...",
                                colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PrimaryText,
                                    focusedLabelColor = PrimaryText,
                                    cursorColor = PrimaryText
                                ),
                                dropdownItem = { artist ->
                                    ArtistComponent(
                                        artist = artist,
                                        modifier = Modifier.width(230.dp)
                                    )
                                },
                                isError = addEditEventViewModel.createEventUiState.value.artistsError,
                                selectedItems = addEditEventViewModel.createEventUiState.value.artists
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            MyButtonComponent(
                                value = saveButtonText,
                                onButtonClicked = {
                                    addEditEventViewModel.isLoading.value = true
                                    isLoading.value = true
                                    if (editMode) {
                                        addEditEventViewModel.onEvent(
                                            CreateEventUIEvent.EditEventButtonClicked(
                                                navController
                                            )
                                        )
                                    } else {
                                        addEditEventViewModel.onEvent(
                                            CreateEventUIEvent.CreateEventButtonClicked(
                                                navController
                                            )
                                        )
                                    }
                                },
                                isEnabled = true
                            )
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }


}
