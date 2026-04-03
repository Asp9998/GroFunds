package com.aryanspatel.grofunds.presentation.screen.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aryanspatel.grofunds.presentation.common.model.BudgetInfo
import com.aryanspatel.grofunds.presentation.common.model.CategoryBudget
import com.aryanspatel.grofunds.presentation.common.model.CurrencyInfo
import com.aryanspatel.grofunds.presentation.common.model.NotifPrefs
import com.aryanspatel.grofunds.presentation.common.model.ProStatuses
import com.aryanspatel.grofunds.presentation.common.model.demoBudget
import com.aryanspatel.grofunds.presentation.common.model.demoCategories
import com.aryanspatel.grofunds.presentation.common.model.demoCurrency
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.ModernFilledTonalButton
import com.aryanspatel.grofunds.presentation.components.ModernIconButton
import com.aryanspatel.grofunds.presentation.components.ModernToggleButton
import com.aryanspatel.grofunds.presentation.viewmodel.InitialPreferencesViewModel
import kotlin.math.min

@Composable
fun ProfileScreen(
    viewModel: InitialPreferencesViewModel = hiltViewModel(),
) {

    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val user by viewModel.initialPrefsUiState.collectAsState()

            ProfileSettingsScreenPinnedHeader(
                name = user.displayName,
                email = user.email,
                onEditDisplayName = { /* open edit dialog */ }
            ) {

                PreferencesMerged(
                    currency = demoCurrency,
                    budget = demoBudget,
                    categories = demoCategories,
                    onChangeCurrency = { /* open currency sheet */ },
                    onChangeBudget = { /* open budget sheet */ },
                    onChangeCategoryBudget = { it ->

                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                NotificationsSection(NotifPrefs())

                Spacer(modifier = Modifier.height(24.dp))

                GrowthMerged(
                    referralCode = "GRO-8ZD3-PL",
                    onShareReferral = {},
                    onReview = {},
                )

                Spacer(modifier = Modifier.height(24.dp))

                SupportAndLegalMerged()

                Spacer(modifier = Modifier.height(24.dp))

                LogoutColumn(onLogout = {})
            }
        }
    }
}

@Composable
fun ProfileSettingsScreenPinnedHeader(
    name: String,
    email: String,
    onEditDisplayName: () -> Unit = {},
    content: @Composable () -> Unit // your sections below the header
) {
    val listState = rememberLazyListState()

    // Header sizes
    val expandedHeight = 218.dp
    val collapsedHeight = 92.dp
    val maxAvatar = 120.dp
    val minAvatar = 48.dp

    val density = LocalDensity.current
    val maxCollapsePx = with(density) { (expandedHeight - collapsedHeight).toPx() }

    // Collapsing state (0f..maxCollapsePx)
    var collapseOffsetPx by remember { mutableStateOf(0f) }

    // Nested scroll: consume UP to collapse header; when scrolling DOWN,
    // list consumes first; once at top, remaining expands header.
    val nested = remember(maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy < 0f) {
                    // scrolling up -> collapse header first
                    val consume = min(-dy, maxCollapsePx - collapseOffsetPx)
                    collapseOffsetPx += consume
                    return Offset(0f, -consume)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val dy = available.y
                if (dy > 0f) {
                    // scrolling down -> let list expand; when it can't, expand header
                    val consume = min(dy, collapseOffsetPx)
                    // Only expand when list is at top (first item fully visible)
                    if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                        collapseOffsetPx -= consume
                        return Offset(0f, consume)
                    }
                }
                return Offset.Zero
            }
        }
    }

    val t = (collapseOffsetPx / maxCollapsePx).coerceIn(0f, 1f) // ← no animate

    val headerHeight = lerp(expandedHeight, collapsedHeight, t)
    val avatarSize  = lerp(maxAvatar,      minAvatar,      t)

    Box(Modifier.fillMaxSize()) {

        // 1) Pinned transparent header
        CollapsingPinnedHeader(
            name = "Aryan Pate",
            email = email,
            headerHeight = headerHeight,
            avatarSize = avatarSize,
            t = t,
            onEditDisplayName = onEditDisplayName
        )

        // 2) Content list, padded under current header height, with nested scroll
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .zIndex(0f)
                .nestedScroll(nested)
                .padding(top = headerHeight),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { content() }
        }
    }
}

@Composable
fun CollapsingPinnedHeader(
    name: String,
    email: String,
    headerHeight: Dp,
    avatarSize: Dp,
    t: Float, // 0 = expanded, 1 = collapsed
    onEditDisplayName: () -> Unit
) {
    val density = LocalDensity.current
    var showEditSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .height(headerHeight),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Spacer(modifier = Modifier.height(25.dp))

            // Compute positions inside this box (absolute)
            BoxWithConstraints(Modifier.fillMaxSize()) {

                val wPx = constraints.maxWidth.toFloat()
                val hPx = constraints.maxHeight.toFloat()
                val avatarPx = with(density) { avatarSize.toPx() }
                val sidePadPx = with(density) { 16.dp.toPx() }
                val expandedAvatarX = (wPx - avatarPx) / 2f
                val expandedAvatarY = hPx / 2f - avatarPx * 0.9f
                val collapsedAvatarX = sidePadPx
                val collapsedAvatarY = with(density) { 8.dp.toPx() }
                val avatarX = lerp(expandedAvatarX, collapsedAvatarX, t)
                val avatarY = lerp(expandedAvatarY, collapsedAvatarY, t)

                var contentWidthPx by remember { mutableFloatStateOf(0f) }
                val baseCenterX = wPx / 2f

                val collapsedTextStartX = avatarX + avatarPx + with(density) { 96.dp.toPx() }
                val collapsedNameTopY   = avatarY

                val targetCenterXRaw = collapsedTextStartX + contentWidthPx / 2f
                val contentLeft = sidePadPx
                val contentRight = wPx - sidePadPx

                val minCenterX = contentLeft + contentWidthPx / 2f
                val maxCenterX = contentRight - contentWidthPx / 2f
                val targetCenterX = targetCenterXRaw.coerceIn(minCenterX, maxCenterX)

                val deltaX = targetCenterX - baseCenterX

                val baseExpandedY = avatarY + avatarPx + with(density) { 12.dp.toPx() }
                val deltaY = collapsedNameTopY - baseExpandedY

                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .graphicsLayer {
                            translationX = avatarX
                            translationY = avatarY
                        }
                        .background(MaterialTheme.colorScheme.onTertiaryFixed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Use initial if no avatar URL
                    Text(
                        text = (name.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                        color = MaterialTheme.colorScheme.surfaceBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { (avatarSize * 0.38f).toSp() }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = deltaX * t          // center → center delta
                            translationY = baseExpandedY + deltaY * t
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = lerp(28.sp, 22.sp, t),
                                maxLines = 1
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showEditSheet  = true},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Rounded.Edit,
                                    contentDescription = "Edit name",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = email,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = lerp(18.sp, 16.sp, t),
                            maxLines = 1
                        )
                    }
                }
            }
            HorizontalDivider(thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.onSecondary.copy(0.1f))
        }
    }
    if(showEditSheet){
        BudgetEditSheet(
            title = "Edit Display Name",
            initial = name,
            editDisplayState = true,
            onConfirm = {onEditDisplayName}
        ) { showEditSheet = false }
    }
}


@Composable
fun PreferencesMerged(
    currency: CurrencyInfo,
    budget: BudgetInfo,
    categories: List<CategoryBudget>,
    onChangeCurrency: () -> Unit,
    onChangeBudget: () -> Unit,
    onChangeCategoryBudget: (CategoryBudget) -> Unit
) {
    var expandCategories by remember { mutableStateOf(false) }
    var showBudgetSheet by remember { mutableStateOf(false) }

    var editTitle by remember { mutableStateOf("")}
    var editInitial by remember { mutableStateOf("") }
    var emojiIcon by remember { mutableStateOf<String?>(null) }

    ProfileMainPannal(
        pannalIcon = Icons.Default.Settings,
        pannalTitle = "Preferences"
    ) {
        // Currency Card
        PreferenceCard(
            title = "Currency",
            subtitle = "Default for amounts & budgets",
        ) {
            PreferenceContainer(
                textIcon = currency.symbol,
                text = currency.code,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Budget Card
        PreferenceCard(
            title = "Budget",
            subtitle = "Monthly budget",
            trailing = {

                ModernIconButton(
                    icon = Icons.Rounded.Edit,
                    onClick = {
                        editTitle = "Edit Monthly Budget"
                        editInitial = "%.2f".format(budget.monthlyBudget)
                        showBudgetSheet = true
                    }
                )
            }
        ) {
            PreferenceContainer(
                imageVectorIcon = Icons.Rounded.Savings,
                text = currency.symbol + " " + budget.monthlyBudget.toString(),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories header row
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expandCategories = !expandCategories }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "Categories",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Define budgets by category",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(
                if (expandCategories) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Categories list (expandable)
        AnimatedVisibility(
            visible = expandCategories,
            enter = expandVertically(
                animationSpec = tween(
                    240,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = shrinkVertically(
                animationSpec = tween(
                    200,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            CategoryBudgetList(
                items = categories,
                onEdit = {cat ->
                    editTitle = "Edit ${cat.label} Budget"
                    editInitial = "%.2f".format(cat.monthlyBudget)
                    emojiIcon = cat.emoji
                    showBudgetSheet = true
                }
            )
        }
    }

    if (showBudgetSheet) {
        BudgetEditSheet(
            title = editTitle,
            emojiIcon  = emojiIcon,
            currencyCode = budget.currencyCode,
            initial = editInitial,
            onConfirm = { new ->
                showBudgetSheet = false
                onChangeBudget()
            },
            onDismiss = { showBudgetSheet = false },
        )
    }
}


@Composable
fun NotificationsSection(notif: NotifPrefs) {
    ProfileMainPannal(
        pannalIcon = Icons.Rounded.NotificationsActive,
        pannalTitle = "Notifications"
    ) {

        var toggleButton = false

        Box(modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface,
                RoundedCornerShape(16.dp))
        ){
            Column(modifier = Modifier.padding(12.dp)) {
                ToggleRow("Upcoming bills & subscriptions", toggleButton) { toggleButton = !toggleButton }
                ToggleRow("Anomalies (bill changes)",toggleButton ) { toggleButton = !toggleButton  }
                ToggleRow("Possible duplicates",toggleButton ) { toggleButton = !toggleButton  }
                ToggleRow("AI insights",toggleButton ) { toggleButton = !toggleButton  }

            }
        }
    }
}

@Composable
fun GrowthMerged(
    referralCode: String?,
    onShareReferral: () -> Unit,
    onReview: () -> Unit,
) {

    ProfileMainPannal(
        pannalIcon = Icons.Rounded.RocketLaunch,
        pannalTitle = "Growth"
    ) {
        PreferenceCard(
            title = "Refer a friend",
            subtitle = "Give 1 month Pro, get 1 month Pro",
        ) {
            ReferralHeroRow(
                referralCode = referralCode,
                onShare = onShareReferral
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ratings
        PreferenceCard(
            title = "Love GroFunds?",
            subtitle = "A quick review really helps",
            trailing = {
                Text(text = "Thank you 💙",
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    style = MaterialTheme.typography.bodySmall)
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModernFilledTonalButton(
                    icon = Icons.Rounded.ThumbUp,
                    text = "Leave a review",
                    onClick = onReview
                )
            }
        }
    }
}

@Composable
fun SupportAndLegalMerged(
    onOpenHelp: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenLegalDetails: (() -> Unit)? = null, // optional: in case you show a full policy screen
) {

    ProfileMainPannal(
        pannalIcon = Icons.AutoMirrored.Rounded.Help,
        pannalTitle = "Support & Legal"
    ) {

        // Support Actions
        PreferenceCard(
            title = "Support",
            subtitle = "We’re here to help",
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernFilledTonalButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    text = "Leave a review",
                    onClick = onOpenHelp
                )
                ModernFilledTonalButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.SupportAgent,
                    text = "Leave a review",
                    onClick = onContactSupport
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Data & Legal (AI declaration + links)
        PreferenceCard(
            title = "Data & Legal",
            subtitle = "Your privacy and rights",
        ) {
            LegalNoticeCard(
                onOpenPrivacy = onOpenPrivacy,
                onOpenTerms = onOpenTerms,
                onOpenLegalDetails = onOpenLegalDetails
            )
        }
    }
}

@Composable
fun LogoutColumn(onLogout: () -> Unit) {
    ProfileMainPannal(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        ModernFilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.AutoMirrored.Rounded.Logout,
            text = "Logout",
            onClick = onLogout,
        )
    }
}


@Stable fun lerp(start: Dp, stop: Dp, f: Float): Dp = start + (stop - start) * f
@Stable fun lerp(start: TextUnit, stop: TextUnit, f: Float): TextUnit {
    require(start.type == TextUnitType.Sp && stop.type == TextUnitType.Sp)
    return (start.value + (stop.value - start.value) * f).sp
}