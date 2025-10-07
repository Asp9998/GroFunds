package com.aryanspatel.grofunds.presentation.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.DateConverters.formatUiDate
import com.aryanspatel.grofunds.domain.usecase.DateConverters.localDateToPickerMillis
import com.aryanspatel.grofunds.domain.usecase.DateConverters.pickerMillisToLocalDate
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A reusable overlay Screen with slide + fade animation.
 */
@Composable
internal fun HorizontalSlidingOverlay(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,  // give screen header horizontal padding, when parent function want to utilize the whole screen , and don't give horizontal padding from modifier.
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var visibleInternal by remember { mutableStateOf(false) }
    var triggerDismiss by remember { mutableStateOf(false) }

    // Show animation on entry
    LaunchedEffect(Unit) { visibleInternal = true }

    // Animate exit before removing from composition
    LaunchedEffect(triggerDismiss) {
        if (triggerDismiss) {
            delay(200)
            onDismiss()
            triggerDismiss = false
        }
    }

    val cardOffset by animateDpAsState(
        targetValue = if (visibleInternal) 0.dp else 1000.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "card_slide"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (visibleInternal) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "overlay_alpha"
    )

    fun animateDismiss() {
        visibleInternal = false
        triggerDismiss = true
    }

    // Handle system back button
    BackHandler { animateDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = overlayAlpha * 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = visibleInternal && !triggerDismiss
            ) { animateDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.BottomCenter)
                .offset(x = cardOffset)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Prevent dismiss on card click */ },
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Reusable header with back button + title
                HorizontalSlidingOverlayHeader(
                    isFullScreen = true,
                    title = title,
                    onBackClick = { animateDismiss() }
                )
                content()
            }
        }
    }
}

/**
 * Header for overlay Screen
 */
@Composable
internal fun HorizontalSlidingOverlayHeader(
    title: String,
    isFullScreen: Boolean = false,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = if (isFullScreen) 16.dp else 0.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Reusable Outline and Regular button in one
 */
@Composable
internal fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOutlined: Boolean = false,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    elevation: Dp = 0.dp
) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = backgroundColor
            ),
            border =  ButtonDefaults.outlinedButtonBorder(enabled).copy(1.dp),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Text(
                text = text,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Bold,

            )
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                contentColor = contentColor
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Reusable Icon Button
 */
@Composable
internal fun ModernIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector,
    enabledIconColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledIconColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
    ){
    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(imageVector = icon,
            contentDescription = "Previous year",
            tint = if (enabled) enabledIconColor
                else disabledIconColor
        )
    }
}

/**
 *  Reusable OutlineTextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModernTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    suffix: String = "",
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions {},
    cornerRadius: Dp = 16.dp,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
    isPassword: Boolean = false
) {

    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label.isNotEmpty()) ({ Text(label) }) else null,
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        leadingIcon = leadingIcon?.let { { Icon(it, null) } },
        suffix = if (suffix.isNotEmpty()) ({
            Text(
                suffix,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }) else null,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedLabelColor = focusedLabelColor
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else null
    )
}

@Composable
internal fun ModernDropDownMenuItem(
    text: String,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    icon: ImageVector,
    onClick: () -> Unit,
    isTrailingIcon: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
){
    if (!isTrailingIcon){
        DropdownMenuItem(
            text = { Text(text = text, color = color) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
            onClick = onClick)
    }else{
        DropdownMenuItem(
            modifier = Modifier.clip(shape = RoundedCornerShape(8.dp)),
            text = { Text(text = text, color = color) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
            onClick = onClick,
            trailingIcon = { if(isTrailingIcon) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )}
            }
        )
    }
}

@Composable
internal fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,

){
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = { onConfirm()}) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
internal fun ProgressIndicator(){
    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
}

@Composable
internal fun SnackBarMessage(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState) {

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
            .padding(16.dp)
            .navigationBarsPadding() // avoid system bars
    ) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerField(
    modifier: Modifier = Modifier,
    value: String,
    label: String = "Date",
    onValueChange: (String) -> Unit,
) {

    var show by remember { mutableStateOf(false) }
    val localDate = DateConverters.stringToLocalDate(value)
    val initialDateMillis = localDateToPickerMillis(localDate)

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        initialDisplayMode = DisplayMode.Picker // Force to calendar picker mode only
    )

    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { TextButton(onClick = { show = true }) { Text("Pick") } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
    }
    if (show) {
        DatePickerDialog(
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { ms ->
                        onValueChange(formatUiDate(pickerMillisToLocalDate(ms)))
                    }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } }
        ) { DatePicker(
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            state = dateState
        ) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MonthYearPickerDialog(
    initial: YearMonth = YearMonth.now(),
    onConfirm: (YearMonth) -> Unit = {},
    onDismiss: () -> Unit= {},
    yearRange: IntRange = (Year.now().value - 5)..(Year.now().value + 5)
) {
    var selectedYear by remember { mutableIntStateOf(initial.year) }
    var selectedMonth by remember { mutableIntStateOf(initial.monthValue) }

    // Months grid
    val months = remember {
        listOf(
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                /** Year Selector with arrows */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    /** Left Arrow [Access previous year]*/
                    ModernIconButton(
                        onClick = {if (selectedYear > yearRange.first) selectedYear--},
                        enabled = selectedYear > yearRange.first,
                        icon = Icons.Default.KeyboardArrowLeft
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    /** Year Text */
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    /** Right Arrow [Access next year] */
                    ModernIconButton(
                        onClick = {if (selectedYear < yearRange.last) { selectedYear++ }},
                        enabled = selectedYear < yearRange.last,
                        icon = Icons.Default.KeyboardArrowRight
                    )

                }

                /** Month Grid */
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .heightIn(min = 180.dp, max = 240.dp)
                        .fillMaxWidth()
                ) {
                    items(12) { idx ->
                        val monthIdx = idx + 1
                        val isSelected = monthIdx == selectedMonth

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else Color.Transparent
                                )
                                .clickable { selectedMonth = monthIdx }
                        ) {
                            Text(
                                text = months[idx],
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                /** Action Buttons [Cancel / Confirm] */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}