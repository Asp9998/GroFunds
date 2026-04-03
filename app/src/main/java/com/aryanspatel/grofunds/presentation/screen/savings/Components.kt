package com.aryanspatel.grofunds.presentation.screen.savings

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.presentation.common.model.GoalData
import com.aryanspatel.grofunds.presentation.common.model.SavingState
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.ModernDropDownMenuItem
import com.aryanspatel.grofunds.presentation.components.ModernIconButton
import com.aryanspatel.grofunds.presentation.components.ModernTextField

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSwitcherBottomSheet(
    listOfSavings: List<SavingState>,
    selectedSavingId: String,
    onSavingSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxSheetHeight = screenHeight * 0.50f // cap at 85% of screen

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Switch Savings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                ModernIconButton(
                    onClick = onDismiss,
                    icon = Icons.Default.Close
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())

            ){
                listOfSavings.forEach { saving ->

                    val selected = selectedSavingId.compareTo(saving.savingId, ignoreCase = true)

                    Card(
                        onClick = {
                            onSavingSelected(saving.savingId)
                            onDismiss()
                                  },
                        colors = CardDefaults.cardColors(
                            containerColor = if(selected == 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.inverseSurface
                        ),
                        border = BorderStroke(2.dp,
                            color = if (selected == 0) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.inverseOnSurface) ,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = saving.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "${saving.savedAmount} / ${saving.targetAmount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionBottomSheet(
    contributionAmount: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Contribution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                ModernIconButton(
                    onClick = onDismiss,
                    icon = Icons.Default.Close
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ModernTextField(
                value = contributionAmount,
                onValueChange = onAmountChange,
                placeholder = "Amount",
                leadingIcon = Icons.Rounded.Payments,
                suffix = "CAD",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModernTextField(
                value = "",
                onValueChange = {},
                placeholder = "note"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernButton(
                onClick = onConfirm,
                text = "Add Contribution",
                enabled = contributionAmount.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SavingMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
//        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        ModernDropDownMenuItem(
            text = "Delete",
            color = MaterialTheme.colorScheme.error,
            onClick = { onDelete(); onDismiss() },
            icon = Icons.Outlined.Delete)
    }
}