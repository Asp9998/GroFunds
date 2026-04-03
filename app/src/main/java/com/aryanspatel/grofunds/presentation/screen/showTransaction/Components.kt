package com.aryanspatel.grofunds.presentation.screen.showTransaction

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.domain.model.CategorySeed
import com.aryanspatel.grofunds.presentation.common.model.Kind
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.ModernConfirmationDialog
import com.aryanspatel.grofunds.presentation.components.ModernDropDownMenuItem
import com.aryanspatel.grofunds.presentation.components.ModernIconBadge
import com.aryanspatel.grofunds.presentation.components.MonthYearPickerDialog
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
internal fun TopAppBarSection(
    kind: String,
    headerText: String,
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthChange: (YearMonth) -> Unit = {},
    onExportDataClick: () -> Unit,
    onShowSummaryClick: () -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    ){
        /** Header(Screen title) Text */
        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        /** Month & year selector */
        MonthSelectorBar(
            kind = kind,
            modifier = Modifier.align(Alignment.Center),
            month = selectedMonth,
            onMonthChange = {onMonthChange(it)},)

        /** More Menu Icon button */
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Row {
                IconButton(onClick = onExportDataClick) {
                    Icon(Icons.Default.FileDownload,
                        contentDescription = "Export Data",
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onShowSummaryClick) {
                    Icon(Icons.Default.PieChart,
                        contentDescription = "Summary",
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun MonthSelectorBar(
    kind: String,
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("LLL yyyy") }
    var showPicker by remember { mutableStateOf(false) }

    val backgroundColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.surfaceContainer
    val borderColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { showPicker = true },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month.format(formatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (showPicker) {
        MonthYearPickerDialog(
            initial = month,
            onDismiss = { showPicker = false },
            onConfirm = {
                onMonthChange(it)
                showPicker = false
            }
        )
    }
}

@Composable
internal fun InsightHeaderSection(
    kind: String,
    isExpenseOverlay: Boolean = true,
    totalSpentOrSaved: Double,
    dailyAvg: Double? = 0.0,
    budget: Double? = 0.0,
) {

    val backgroundColor =
        if(kind == Kind.EXPENSE.name)
            Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer))
        else Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer))

    val borderColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp), // For Elevation visibility
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .background(backgroundColor)
                    .border(1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(totalSpentOrSaved),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if(isExpenseOverlay){
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily avg: ${NumberFormat.getCurrencyInstance().format(dailyAvg)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Limit: ${NumberFormat.getCurrencyInstance().format(budget)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                if(isExpenseOverlay){
                    BudgetProgressRing(
                        kind = kind,
                        progress = (totalSpentOrSaved / (budget ?: 0.0)).toFloat(),
                        size = 54.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressRing(
    kind: String,
    progress: Float,
    size: Dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val backgroundColor = MaterialTheme.colorScheme.background
        val mainColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.surfaceDim
                            else MaterialTheme.colorScheme.surfaceTint

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.toPx()) / 2

            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(strokeWidth)
            )

            // Progress arc
            drawArc(
                color = mainColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        if (size > 32.dp) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
internal fun FiltersSection(
    kind: String,
    categories: List<CategorySeed>,
    selectedCategories: List<String>,
    onCategoryChanged: (String) -> Unit,
    onClearCategory: () -> Unit
) {

    val backgroundColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.surfaceContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(modifier = Modifier.width(8.dp))
            }

            item{
                FilterChip(
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = backgroundColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSecondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderWidth = 0.dp,
                        borderColor = Color.Transparent,
                        enabled = true,
                        selected = false,
                    ),
                    selected = selectedCategories.isEmpty(),
                    onClick = onClearCategory,
                    label = { Text(text = "All") }
                )
            }

            items(categories) { category ->
                FilterChip(
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = backgroundColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSecondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderWidth = 0.dp,
                        borderColor = Color.Transparent,
                        enabled = true,
                        selected = false,
                    ),
                    selected = selectedCategories.contains(category.id),
                    onClick = {  onCategoryChanged(category.id)},
                    label = { Text(text = category.name) }
                )
            }

            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
internal fun DateHeader(
    date: String,
    total: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(start = 16.dp, top = 8.dp, bottom = 2.dp, end = 16.dp),
                .padding(start = 0.dp, top = 8.dp, bottom = 2.dp, end = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Text(
                text = NumberFormat.getCurrencyInstance().format(total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    kind: String,
    amount: Double,
    categoryIcon: String,
    categoryOrType: String,
    isExpenseOverlay: Boolean,
    subcategory: String? = null,
    merchant: String? = null,
    note: String? = null,
    isExcluded: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onExcludeFromReport: () -> Unit,
    onDeleteTransaction: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val backgroundColor =
        if(kind == Kind.EXPENSE.name)
            Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.onSecondaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer))
        else Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.primaryContainer))

    val borderColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onBackground

    val iconBackground = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.surfaceContainer
    val iconTint = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.surfaceDim
                            else MaterialTheme.colorScheme.surfaceTint

    Column(
//        modifier = modifier.fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .background(backgroundColor)
//            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .border(1.dp, Color.Red),
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /** Category Icon */
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(iconBackground),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    painter = painterResource(categoryIcon),
//                    contentDescription = "Category Icon",
//                    modifier = Modifier.size(20.dp),
//                    tint = iconTint)
//            }

            ModernIconBadge(
                text = categoryIcon,
//                background = iconBackground,
                background = Color.Transparent,
                iconTint = iconTint
            )


            Spacer(modifier = Modifier.width(10.dp))

            /** Category / Type, Subcategory, Merchant and Note */
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if(isExpenseOverlay && subcategory != null) subcategory else categoryOrType,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if(isExpenseOverlay){
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryOrType ,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        if(merchant != null){
                            Text(
                                text = " | $merchant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if(note != null){
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount and Menu
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = (if(isExpenseOverlay)"-" else "")
                            + NumberFormat.getCurrencyInstance().format(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    TransactionMenu(
                        expanded = menuOpen,
                        isExcluded = isExcluded,
                        onDismiss = { menuOpen = false },
                        onEdit = onEdit,
                        onDuplicate = onDuplicate,
                        onExcludeFromReport = onExcludeFromReport,
                        onDeleteTransaction = { confirmDelete = true }
                    )
                }
            }
        }
    }
    if (confirmDelete) {
        ModernConfirmationDialog(
            title = "Delete transaction?",
            text = "This action cannot be undone.",
            confirmButtonLabel = "Delete",
            confirmButtonColor = MaterialTheme.colorScheme.error,
            onConfirm = {
                onDeleteTransaction()
                confirmDelete = false
            },
            onDismiss = { confirmDelete = false}
        )
    }
}

@Composable
private fun TransactionMenu(
    expanded: Boolean,
    isExcluded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onExcludeFromReport: () -> Unit,
    onDeleteTransaction: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ModernDropDownMenuItem(
            text = "Edit",
            onClick = { onEdit(); onDismiss() },
            icon = Icons.Outlined.Edit)

        ModernDropDownMenuItem(
            text = "Duplicate",
            onClick = { onDuplicate(); onDismiss() },
            icon = Icons.Outlined.ContentCopy)

        ModernDropDownMenuItem(
            text = "Exclude from report",
            onClick = { onExcludeFromReport(); onDismiss() },
            icon = Icons.Outlined.VisibilityOff,
            isTrailingIcon = true,
            checked = isExcluded,
            onCheckedChange = { onExcludeFromReport() ;onDismiss()},
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        ModernDropDownMenuItem(
            text = "Delete",
            color = MaterialTheme.colorScheme.error,
            onClick = { onDeleteTransaction(); onDismiss() },
            icon = Icons.Outlined.Delete)
    }
}


@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    onAddTransactionClick: () -> Unit
) {

    Column(modifier = modifier
        .fillMaxSize()
        .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(96.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No ${title}s yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center

        )

        Spacer(modifier = Modifier.height(24.dp))

        ModernButton(
            modifier = Modifier.wrapContentWidth(),
            onClick = onAddTransactionClick,
            text = "Add your first $title"
        )
    }
}