package com.example.bend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bend.model.Artist
import com.example.bend.ui.theme.PrimaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsSelectSearchInputText(
    modifier: Modifier = Modifier,
    listOfItems: List<Artist>,
    onArtistsChanged: (List<Artist>) -> Unit,
    enable: Boolean = true,
    readOnly: Boolean = true,
    placeholder: String = "Select Option",
    openedIcon: ImageVector = Icons.Outlined.KeyboardArrowUp,
    closedIcon: ImageVector = Icons.Outlined.KeyboardArrowDown,
    parentTextFieldCornerRadius: Dp = 5.dp,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    dropdownItem: @Composable (Artist) -> Unit,
    isError: Boolean = false,
    selectedItems: List<Artist> = emptyList()
) {
    var selectedOptions by rememberSaveable { mutableStateOf(selectedItems) }
    var searchedOption by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var filteredItems by remember { mutableStateOf(listOfItems) }

    val itemHeights = remember { mutableStateMapOf<Int, Int>() }
    val baseHeight = 530.dp
    val density = LocalDensity.current

    val maxHeight = remember(itemHeights.toMap()) {
        if (itemHeights.keys.toSet() != listOfItems.indices.toSet()) {
            return@remember baseHeight
        }
        val baseHeightInt = with(density) { baseHeight.toPx().toInt() }

        var sum = with(density) { DropdownMenuVerticalPadding.toPx().toInt() } * 2
        for ((_, itemSize) in itemHeights.toSortedMap()) {
            sum += itemSize
            if (sum >= baseHeightInt) {
                return@remember with(density) { (sum - itemSize / 2).toDp() }
            }
        }
        baseHeight
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = modifier,
            colors = colors,
            value = selectedOptions.joinToString(", ") { it.stageName },
            readOnly = readOnly,
            enabled = enable,
            onValueChange = {},
            label = {
                Text(text = placeholder)
            },
            trailingIcon = {
                IconToggleButton(
                    checked = expanded,
                    onCheckedChange = {
                        expanded = it
                    }
                ) {
                    if (expanded) Icon(
                        imageVector = openedIcon,
                        contentDescription = null,
                        tint = PrimaryText
                    ) else Icon(
                        imageVector = closedIcon,
                        contentDescription = null,
                        tint = PrimaryText
                    )
                }
            },
            shape = RoundedCornerShape(parentTextFieldCornerRadius),
            isError = !isError,
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                expanded = !expanded
                            }
                        }
                    }
                }
        )
        if (expanded) {
            DropdownMenu(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .requiredSizeIn(maxHeight = maxHeight)
                    .background(Color.White),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchedOption = ""
                }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        value = searchedOption,
                        onValueChange = { searchText ->
                            searchedOption = searchText
                            filteredItems = listOfItems.filter {
                                it.toString().contains(
                                    searchedOption,
                                    ignoreCase = true
                                )
                            }.toMutableList()
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                        },
                        placeholder = {
                            Text(text = "Search")
                        }
                    )

                    filteredItems.forEach { selectedItem ->
                        val isSelected = selectedOptions.contains(selectedItem)
                        DropdownMenuItem(
                            onClick = {
                                if (isSelected) {
                                    selectedOptions = selectedOptions - selectedItem
                                    onArtistsChanged(selectedOptions)
                                } else {
                                    selectedOptions = selectedOptions + selectedItem
                                    onArtistsChanged(selectedOptions)
                                }
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    dropdownItem(selectedItem)

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        modifier = Modifier.weight(0.1f),
                                        colors = CheckboxDefaults.colors(checkedColor = Color.Gray)
                                    )
                                }
                            },
                            colors = MenuDefaults.itemColors()
                        )
                    }
                }
            }
        }
    }
}

private val DropdownMenuVerticalPadding = 8.dp
