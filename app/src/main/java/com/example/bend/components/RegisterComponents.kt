package com.example.bend.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bend.ui.theme.Primary
import com.example.bend.ui.theme.PrimaryText
import com.example.bend.ui.theme.Secondary
import com.example.bend.ui.theme.green


@Composable
fun NormalTextComponent(value:String){
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        color = PrimaryText,
        textAlign = TextAlign.Center
    )
}

@Composable
fun BoldTextComponent(value:String){
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        color = PrimaryText,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPasswordFieldComponent(
    labelValue:String,
    onTextSelected: (String) -> Unit,
    errorStatus:Boolean = false
){
    val password = remember {
        mutableStateOf("")
    }
    val passVisible = remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = {Text(text = labelValue)},
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryText,
            focusedLabelColor = PrimaryText,
            cursorColor = PrimaryText,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        value = password.value,
        onValueChange = {
            password.value = it
            onTextSelected(it)
        },
        singleLine = true,
        maxLines = 1,
        trailingIcon = {

            val iconImage = if(passVisible.value){
                Icons.Filled.Visibility
            }else{
                Icons.Filled.VisibilityOff
            }
            val description = if(passVisible.value){
                "Hide Password"
            } else{
                "Show Password"
            }

            IconButton(onClick = {passVisible.value = !passVisible.value}) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },
        visualTransformation = if(passVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        isError = !errorStatus


//        TODO: leading icon

//        leadingIcon = { Icon(painter = , contentDescription = )}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextFieldComponent(
    labelValue:String,
    initialValue: String = "",
    onTextSelected: (String) -> Unit,
    errorStatus:Boolean = false
){
    val textValue = remember {
        mutableStateOf(initialValue)
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = {Text(text = labelValue)},
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
        singleLine = true,
        maxLines = 1,
        isError = !errorStatus
//        TODO: leading icon

//        leadingIcon = { Icon(painter = , contentDescription = )}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDropDownMenuComponent(
    label_value:String,
    options:Array<String>,
    onTextSelected: (String) -> Unit
    ){
    val options = options
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    Box(
        modifier = Modifier
            .fillMaxWidth()

    ){
        onTextSelected(selectedText)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded},

            ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),

                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryText,
                    focusedLabelColor = PrimaryText,
                    cursorColor = PrimaryText,
                ),
                keyboardOptions = KeyboardOptions.Default,
                label = { Text(text = label_value) },
                singleLine = true,
                maxLines = 1,

            )
            ExposedDropdownMenu(

                expanded = expanded,
                onDismissRequest = { expanded = false},
                modifier = Modifier.background(Color.White)

            ) {
                options.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(text = type) },
                        onClick = {
                            selectedText = type
                            expanded = false
                            onTextSelected(selectedText)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun MyButtonComponent(
    value:String,
    onButtonClicked : () -> Unit,
    is_enabled:Boolean = false
    ){
    Button(
        onClick = { onButtonClicked.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        enabled = is_enabled

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(Secondary, Primary)),
                    shape = RoundedCornerShape(50.dp)
                )
                .heightIn(48.dp),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
    }
}

@Composable
fun ClickableLoginRegisterText(onTextSelected: (String) -> Unit, initial_text:String, action_text:String, span_style: SpanStyle){
    val initial_text = initial_text
    val login_register_text = action_text

    val annotatedString = buildAnnotatedString{
        withStyle(
            style = SpanStyle(color = PrimaryText)
        ){
            pushStringAnnotation(tag = initial_text, annotation = initial_text)
            append(initial_text)
        }
        withStyle(
            style = span_style
        ){
            pushStringAnnotation(tag = login_register_text, annotation = login_register_text)
            append(login_register_text)
        }
    }
    ClickableText(
        text = annotatedString,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset)
                .firstOrNull()?.also { span ->
                    Log.d("ClickableLoginRegisterText","{${span.item}}")
                    if (span.item == login_register_text){
                        onTextSelected(span.item)
                    }
                }
        },
    )
}







