package com.aryanspatel.grofunds.presentation.screen.showTransaction

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Autorenew
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.domain.model.CategorySeed
import com.aryanspatel.grofunds.presentation.common.model.Transaction
import com.aryanspatel.grofunds.presentation.components.DeleteConfirmationDialog
import com.aryanspatel.grofunds.presentation.components.ModernDropDownMenuItem
import com.aryanspatel.grofunds.presentation.components.MonthYearPickerDialog
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
internal fun TopAppBarSection(
    headerText: String,
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthChange: (YearMonth) -> Unit = {},
    isSummaryMode: Boolean,
    onExportDataClick: () -> Unit,
    onShowSummaryClick: () -> Unit,
    onRecurringTransactionClick: () -> Unit,
    onInsightsClick: () -> Unit
) {
    var showMenu by remember {  mutableStateOf(false) }
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
    ){
        /** Header(Screen title) Text */
        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        /** Month & year selector */
        MonthSelectorBar(
            modifier = Modifier.align(Alignment.Center),
            month = selectedMonth,
            onMonthChange = {onMonthChange(it)},)

        /** More Menu Icon button */
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            MoreMenu(
                isVisible = showMenu,
                onDismiss = { showMenu = false },
                isSummaryMode = isSummaryMode,
                onShowSummaryClick = onShowSummaryClick,
                onInsightsClick = onInsightsClick,
                onRecurringTransactionClick = onRecurringTransactionClick,
                onExportDataClick = onExportDataClick
            )
        }
    }
}

@Composable
private fun MonthSelectorBar(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("LLL yyyy") }
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable { showPicker = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month.format(formatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
private fun MoreMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isSummaryMode: Boolean,
    onShowSummaryClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onRecurringTransactionClick: () -> Unit,
    onExportDataClick: () -> Unit,
) {

    DropdownMenu(
        expanded = isVisible,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        /** 1) Export data */
        ModernDropDownMenuItem(
            text = "Export data",
            icon = Icons.Default.FileDownload,
            onClick = {
                onDismiss()
                onExportDataClick() })

        /** 2) Summary toggle */
        ModernDropDownMenuItem(
            text = if (isSummaryMode) "Show list view" else "Show summary",
            icon = if (isSummaryMode) Icons.AutoMirrored.Filled.List else Icons.Default.PieChart,
            onClick = {
                onDismiss()
                onShowSummaryClick() })

         /** 3) Recurring Expenses */
        ModernDropDownMenuItem(
            text = "Recurring Expense",
            icon = Icons.Outlined.Autorenew,
            onClick = {
                onDismiss()
                onRecurringTransactionClick()})

        /** 4) Insights */
        ModernDropDownMenuItem(
            text = "Insights",
            icon = Icons.Default.Insights,
            onClick = {
                onDismiss()
                onInsightsClick() })
    }
}

@Composable
internal fun InsightHeaderSection(
    isExpenseOverlay: Boolean = true,
    totalSpentOrSaved: Double,
    dailyAvg: Double? = 0.0,
    budget: Double? = 0.0,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp), // For Elevation visibility
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation( 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(totalSpentOrSaved),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
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
        val mainColor = MaterialTheme.colorScheme.onSecondary

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
    categories: List<CategorySeed>,
    selectedCategories: List<String>,
    onCategoryChanged: (String) -> Unit,
    onClearCategory: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(modifier = Modifier.width(12.dp))
            }

            item{
                FilterChip(
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderWidth = 0.dp,
                        borderColor = Color.Transparent,
                        enabled = true,
                        selected = false,
                    ),
                    selected = selectedCategories.isEmpty(),
                    onClick = onClearCategory,
                    label = { Text("All") }
                )
            }

            items(categories) { category ->
                FilterChip(
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderWidth = 0.dp,
                        borderColor = Color.Transparent,
                        enabled = true,
                        selected = false,
                    ),
                    selected = selectedCategories.contains(category.id),
                    onClick = {  onCategoryChanged(category.id)},
                    label = { Text(category.name) }
                )
            }

            item {
                Spacer(modifier = Modifier.width(12.dp))
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
                .padding(start = 16.dp, top = 8.dp, bottom = 2.dp, end = 16.dp),
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
    amount: Double,
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


    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

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
                    fontWeight = FontWeight.Bold
                )
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        DeleteConfirmationDialog(
            title = "Delete transaction?",
            text = "This action cannot be undone.",
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
fun EmptyState(onAddTransactionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start tracking your spending by adding your first expense.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddTransactionClick) {
            Text("Add your first expense")
        }
    }
}