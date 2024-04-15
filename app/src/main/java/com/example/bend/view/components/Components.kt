package com.example.bend.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bend.view.theme.PrimaryText
import com.example.bend.view.theme.Secondary
import com.example.bend.view.theme.green
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddPosterButton(
    value: String,
    onButtonClicked: () -> Unit,
    isEnabled: Boolean = false
) {
    Button(
        onClick = { onButtonClicked.invoke() },
        modifier = Modifier
            .heightIn(48.dp)
            .width(200.dp)
            .shadow(
                25.dp,
                shape = RoundedCornerShape(50.dp),
            ),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        enabled = isEnabled,


        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(Secondary, green)),
                    shape = RoundedCornerShape(50.dp)
                )
                .heightIn(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNumberPickerComponent(
    labelValue: String,
    onNumberSelected: (Int) -> Unit,
    errorStatus: Boolean = false,
    modifier: Modifier,
    initialValue: Int = 0
) {
    val numberValue = remember {
        mutableStateOf(initialValue)
    }

    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = labelValue) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryText,
            focusedLabelColor = PrimaryText,
            cursorColor = PrimaryText,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        value = numberValue.value.toString(),
        onValueChange = {
            if (it.isNotBlank()) {
                numberValue.value = it.toInt()
                onNumberSelected(numberValue.value)
            } else {
                numberValue.value = 0
                onNumberSelected(0)
            }
        },
        singleLine = true,
        maxLines = 1,
        isError = !errorStatus
        // TODO: leading icon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    labelValue: String,
    onTextSelected: (LocalDate) -> Unit,
    errorStatus: Boolean = false,
    graterThan: LocalDate?,
    initialValue: LocalDate?

) {

    var pickedDate by remember {
        mutableStateOf(LocalDate.now())
    }
    val dateDialogState = rememberMaterialDialogState()

    var defaultDate by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        label = { Text(text = labelValue) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryText,
            focusedLabelColor = PrimaryText,
            cursorColor = PrimaryText,
        ),
        keyboardOptions = KeyboardOptions.Default,
        value = if (defaultDate) pickedDate.toString() else {
            initialValue?.toString() ?: ""
        },
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        maxLines = 1,
        isError = !errorStatus,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            dateDialogState.show()
                        }
                    }
                }
            }
    )


    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(text = "Ok") {
                defaultDate = true
            }
            negativeButton(text = "Cancel")
        },
        border = BorderStroke(1.dp, Color.Black)

    ) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Pick a date",
            allowedDateValidator = {
                if (graterThan != null)
                    it > graterThan
                else
                    true
            },
        ) {
            pickedDate = it
            onTextSelected(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    labelValue: String,
    onTextSelected: (LocalTime) -> Unit,
    errorStatus: Boolean = false,
    initialValue: LocalTime?
) {

    var pickedTime by remember {
        mutableStateOf(LocalTime.NOON)
    }
    val timeDialogState = rememberMaterialDialogState()

    var defaultTime by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        label = { Text(text = labelValue) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryText,
            focusedLabelColor = PrimaryText,
            cursorColor = PrimaryText,
        ),
        keyboardOptions = KeyboardOptions.Default,
        value = if (defaultTime) pickedTime.toString() else {
            initialValue?.toString() ?: ""
        },
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        maxLines = 1,
        isError = !errorStatus,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            timeDialogState.show()
                        }
                    }
                }
            }
    )
    MaterialDialog(
        dialogState = timeDialogState,
        buttons = {
            positiveButton(
                text = "Ok"
            ) {
                defaultTime = true
            }
            negativeButton(text = "Cancel")
        },
        border = BorderStroke(1.dp, Color.Black)
    ) {
        timepicker(
            initialTime = LocalTime.NOON,
            title = "Pick a time",
            colors = TimePickerDefaults.colors(
                activeBackgroundColor = green,
                activeTextColor = PrimaryText,
                inactiveBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                inactiveTextColor = PrimaryText,
                selectorColor = green,
                headerTextColor = PrimaryText
            ),
            is24HourClock = true
        ) {
            pickedTime = it
            onTextSelected(it)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewComponent(
    labelValue: String,
    initialValue: String = "",
    onTextSelected: (String) -> Unit,
    errorStatus: Boolean = false
) {
    val textValue = remember {
        mutableStateOf(initialValue)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        label = { Text(text = labelValue) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryText,
            focusedLabelColor = PrimaryText,
            cursorColor = PrimaryText,
        ),
        keyboardOptions = KeyboardOptions.Default,
        value = textValue.value,
        onValueChange = {
            textValue.value = it
            onTextSelected(it)
        },
        singleLine = false,
        maxLines = 10,
        isError = !errorStatus
//        TODO: leading icon

//        leadingIcon = { Icon(painter = , contentDescription = )}
    )
}

