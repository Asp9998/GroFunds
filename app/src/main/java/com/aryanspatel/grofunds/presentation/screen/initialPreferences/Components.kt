package com.aryanspatel.grofunds.presentation.screen.initialPreferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryanspatel.grofunds.domain.model.CategorySeed
import com.aryanspatel.grofunds.presentation.common.model.Country
import com.aryanspatel.grofunds.presentation.common.model.OnboardingStep
import com.aryanspatel.grofunds.presentation.components.ModernLinearProgressIndicator
import com.aryanspatel.grofunds.presentation.components.ModernTextField


@Composable
fun BoxScope.EmptyState(title: String) {
    Column(
        Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
        Spacer(Modifier.height(8.dp))
        Text(text = title,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TopProgress(
    step: OnboardingStep
) {
    val index = when (step) {
        OnboardingStep.Currency -> 0
        OnboardingStep.Budget -> 1
        OnboardingStep.CategoryBudgets -> 2
    }
    val total = 3
    Column {
        Text(text = "${index + 1} of $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primaryFixed
        )

        Spacer(Modifier.height(6.dp))

        ModernLinearProgressIndicator(
            currentValue = index + 1,
            totalValue = total,
            isGapped = true
        )
    }
}

@Composable
fun TitleBlock(
    icon: ImageVector,
    title: String,
    helper: String
) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryFixed
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(2.dp))

            Text(text = helper,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CountryRow(
    country: Country,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if(selected) MaterialTheme.colorScheme.primaryFixed
    else MaterialTheme.colorScheme.inverseOnSurface

    Row(
        Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = country.flag,
                fontSize = 24.sp,
                color = LocalContentColor.current.copy(alpha = 1f))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = country.name,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text("${country.symbol} • ${country.currencyCode}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (selected) {
            Icon(imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primaryFixed
            )
        }
    }
}

@Composable
fun CategoryBudgetRow(
    currencySymbol: String,
    draft: CategorySeed,
    monthlyBudget: String,
    onChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(text = draft.emoji ?: "🧩",
                fontSize = 20.sp,
                color = LocalContentColor.current.copy(alpha = 1f),
            )

            Spacer(Modifier.width(8.dp))

            Text(text = draft.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(8.dp))

        ModernTextField(
            value = monthlyBudget,
            onValueChange = {onChange(it)},
            modifier = Modifier.widthIn(min = 120.dp).fillMaxWidth(),
            maxLines = 1,
            placeholder = "Amount",
            suffix = currencySymbol,
            keyboardType = KeyboardType.Decimal,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        )
    }
}
