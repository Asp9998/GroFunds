package com.aryanspatel.grofunds.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryanspatel.grofunds.presentation.common.model.CategoryBudget
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.ModernIconBadge
import com.aryanspatel.grofunds.presentation.components.ModernIconButton
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.ModernToggleButton


@Composable
fun ProfileMainPannal(
    modifier: Modifier = Modifier,
    pannalTitle: String? = null,
    pannalIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
){
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Column{

            HorizontalDivider(thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.surface)

            Spacer(Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if(pannalTitle != null && pannalIcon != null){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(pannalIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            pannalTitle,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
                content()
            }
        }
    }
}


@Composable
fun PreferenceCard(
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface,
                RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary)

                Spacer(Modifier.height(2.dp))

                Text(text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary)
            }
            if (trailing != null) trailing()
        }
        Spacer(Modifier.height(10.dp))

        content()
    }
}


@Composable
fun PreferenceContainer(
    textIcon: String? = null,
    imageVectorIcon: ImageVector? = null,
    text: String,
    description: String? = null,
    containerColor1: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    containerColor2: Color = MaterialTheme.colorScheme.tertiaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.onTertiary,
    iconBackground: Color = MaterialTheme.colorScheme.onTertiaryFixed,
    iconTint: Color = MaterialTheme.colorScheme.surfaceBright,
    isEditable: Boolean = false,
    onEditClick: () -> Unit = {}
){
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(containerColor1,containerColor2)))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Big symbol badge
            ModernIconBadge(
                text = textIcon,
                icon = imageVectorIcon,
                background = iconBackground,
                iconTint = iconTint
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(text = text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium)
                if(description != null){
                    Text(description,
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if(isEditable){
            ModernIconButton(
                icon = Icons.Rounded.Edit,
                onClick = onEditClick
            )
        }
    }
}

@Composable
fun CategoryBudgetList(
    items: List<CategoryBudget>,
    onEdit: (CategoryBudget) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(16.dp))
            .padding(6.dp)
    ) {
        // Use LazyColumn if long; for compact lists Column is fine.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(6.dp)
        ) {
            items(items, key = { it.id }) { cat ->
                PreferenceContainer(
                    textIcon = cat.emoji,
                    text = cat.label,
                    description = formatCurrency(cat.monthlyBudget, cat.currencyCode),
                    isEditable = true,
                    onEditClick = { onEdit(cat) },
                    containerColor1 = MaterialTheme.colorScheme.inverseSurface,
                    containerColor2 = MaterialTheme.colorScheme.inverseSurface,
                    borderColor = MaterialTheme.colorScheme.inverseOnSurface

                )
            }
        }
    }

}

@Composable
fun ToggleRow(title: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title,
            color = MaterialTheme.colorScheme.onPrimary
        )
        ModernToggleButton(
            initialValue = value,
            onCheckedChange = { onToggle(it) },
        )

    }
}

@Composable
fun ReferralHeroRow(
    referralCode: String?,
    onShare: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    ModernIconBadge(
                        icon = Icons.Rounded.Redeem,
                        background = MaterialTheme.colorScheme.onTertiaryFixed,
                        iconTint = MaterialTheme.colorScheme.surfaceBright,
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(text = "Referral code",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium)

                        Text(
                            referralCode ?: "—",
                            color = MaterialTheme.colorScheme.onPrimaryFixed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row {
                    ModernIconButton(
                        icon = Icons.Rounded.ContentCopy,
                        onClick = {
                            if (!referralCode.isNullOrBlank()) {
                                clipboard.setText(AnnotatedString(referralCode))
                            }
                        }
                    )

                    Spacer(Modifier.width(4.dp));

                    ModernIconButton(
                        icon = Icons.Rounded.Share,
                        onClick = onShare)
                }
            }
        }

    }
}

@Composable
fun LegalNoticeCard(
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenLegalDetails: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0x33F59E0B), Color(0x1AF59E0B)) // warm legal glass
                )
            )
            .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Gavel, contentDescription = null, tint = Color(0xFFF59E0B))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("AI-assisted insights", color = Color.White, fontWeight = FontWeight.Medium)
                Text(
                    "GroFunds analyzes certain user data with AI to generate insights and recommendations. " +
                            "Review how data is processed, stored, and your choices.",
                    color = Color(0xFF8EA0B6),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenPrivacy, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Icon(Icons.Rounded.Lock, contentDescription = null)
                Spacer(Modifier.width(6.dp)); Text("Privacy Policy")
            }
            OutlinedButton(onClick = onOpenTerms, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Icon(Icons.Rounded.Description, contentDescription = null)
                Spacer(Modifier.width(6.dp)); Text("Terms of Use")
            }
            if (onOpenLegalDetails != null) {
                FilledTonalButton(onClick = onOpenLegalDetails, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Rounded.Info, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("Learn more")
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Tip: You can opt out of certain AI features in Settings.",
            color = Color(0xFF9AA7B8),
            fontSize = 11.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditSheet(
    title: String,
    editDisplayState: Boolean  = false,
    emojiIcon : String? = null,
    currencyCode: String? = null,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(initial) }
    val isValid = remember(text) {
        if(editDisplayState) text.toDoubleOrNull()?.let { it >= 0 && it <= 1_000_000 } == true
        else text.isNotBlank()
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Column(Modifier.padding(16.dp).imePadding()) {

            Row {
                if(emojiIcon != null){
                    Text (text = emojiIcon,
                        fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            ModernTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { t -> text = t.filter { it.isDigit() || it == '.' }},
                leadingIcon = if (!editDisplayState) Icons.Rounded.Payments else null,
                placeholder = "Amount",
                suffix = currencyCode ?: "",
                keyboardType = if(!editDisplayState) KeyboardType.Decimal else KeyboardType.Text,
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )

            if (!isValid && !editDisplayState) {
                Spacer(Modifier.height(6.dp))
                Text(text = "Enter a valid input",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(22.dp))

            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModernButton(
                    modifier = Modifier.weight(1f),
                    text = "Cancel",
                    onClick = onDismiss,
                    isOutlined = true,
                )
                ModernButton(
                    modifier = Modifier.weight(2f),
                    text = "Save",
                    onClick = {onConfirm(text)},
                    enabled = isValid
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}


@Composable
private fun formatCurrency(amount: Double, code: String): String {
    // You likely have a better formatter—this is a simple readable default.
    val symbol = when (code.uppercase()) {
        "USD" -> "$"
        "CAD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        else -> ""
    }
    return if (symbol.isNotEmpty()) "$symbol${"%,.0f".format(amount)} $code" else "${"%,.0f".format(amount)} $code"
}
