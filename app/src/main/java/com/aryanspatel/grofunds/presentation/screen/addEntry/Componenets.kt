package com.aryanspatel.grofunds.presentation.screen.addEntry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryanspatel.grofunds.domain.model.EntryKind


@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = gradient?.let { Color.Transparent } ?: MaterialTheme.colorScheme.surface
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (gradient != null) Modifier.background(gradient, RoundedCornerShape(cornerRadius)) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(pressedElevation = 12.dp)
    ) {
        Column(Modifier.padding(24.dp), content = content)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDropdownTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    droppedMenuCornerRadius: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
            shape = RoundedCornerShape(cornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
        ExposedDropdownMenu(
            shape = RoundedCornerShape(droppedMenuCornerRadius),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(droppedMenuCornerRadius))
                .background(backgroundColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onValueChange(option); expanded = false },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedToggleButton(
    modifier: Modifier = Modifier,
    selectedOption: EntryKind = EntryKind.EXPENSE,
    onOptionSelected: (EntryKind) -> Unit = {},
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    selectedTextColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSecondary,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    val options = listOf(
        EntryKind.EXPENSE to "Expense",
        EntryKind.INCOME to "Income",
        EntryKind.GOAL to "Goal"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ).border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val optionWidth = (totalWidth - 8.dp) / 3 // Account for spacing between options

        // Calculate the offset for the sliding background
        val selectedIndex = when (selectedOption) {
            EntryKind.EXPENSE -> 0
            EntryKind.INCOME -> 1
            EntryKind.GOAL -> 2
        }

        val slideOffset by animateDpAsState(
            targetValue = (optionWidth + 4.dp) * selectedIndex,
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            ),
            label = "slide_animation"
        )

        // Sliding background
        Box(
            modifier = Modifier
                .offset(x = slideOffset)
                .width(optionWidth)
                .height(48.dp)
                .background(
                    color = selectedBackgroundColor,
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // Row of options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEach { (option, label) ->
                val isSelected = selectedOption == option

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No hover/ripple effect
                        ) {
                            onOptionSelected(option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) selectedTextColor else unselectedTextColor
                    )
                }
            }
        }
    }
}




