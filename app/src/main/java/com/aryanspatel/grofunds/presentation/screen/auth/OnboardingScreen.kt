package com.aryanspatel.grofunds.presentation.screen.auth

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.model.OnboardingPage
import com.aryanspatel.grofunds.presentation.common.navigation.Destinations
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue

/**
 * Root auth screen:
 * - Shows onboarding pager by default
 * - Opens SignUpScreen / LoginScreen as overlays
 */
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val user by viewModel.user.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSignUpScreen by remember { mutableStateOf(false) }
    var showLoginScreen by remember { mutableStateOf(false) }

    // Navigate to Home once a user object appears
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Destinations.HomeScreen.name) {
                popUpTo(Destinations.AuthScreen.name) { inclusive = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    OnboardingScreen(
        onSignUpClick = { showSignUpScreen = true },
        onLoginClick = { showLoginScreen = true }
    )

    if (showSignUpScreen) {
        SignUpScreen(
            viewModel = viewModel,
            uiState = uiState,
            onSighUpClick = {email, password, preferredName ->
                viewModel.signUp(email, password, preferredName)
            }) {
            showSignUpScreen = false
        }
    }

    if (showLoginScreen) {
        LoginScreen(
            uiState = uiState,
            onResetState = { viewModel.resetState() },
            onResetClick = {viewModel.resetPassword(it)},
            onLoginClick = {email, password ->
                viewModel.signIn(email, password)
            }) {
            showLoginScreen = false
        }
    }
}

/**
 * Onboarding screen with:
 * - Auto-scrolling pager
 * - Page indicators
 * - Bottom action buttons { sign up / login }
 */
@Composable
fun OnboardingScreen(
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_page_1_title),
            description = stringResource(R.string.onboarding_page_1_description),
            image = painterResource(R.drawable.onboarding_img_1)
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_page_2_title),
            description = stringResource(R.string.onboarding_page_2_description),
            image = painterResource(R.drawable.onboarding_img_2)
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_page_3_title),
            description = stringResource(R.string.onboarding_page_3_description),
            image = painterResource(R.drawable.onboarding_img_3)
        )
    )

    /**
     * Auto-scroll pager every 4 seconds
     */
    LaunchedEffect(pagerState) {
        var direction = 1
        while (isActive) {
            delay(4000)
            val current = pagerState.currentPage
            val candidate = current + direction
            when {
                candidate > pages.lastIndex -> {
                    direction = -1
                    pagerState.animateScrollToPage(current + direction, animationSpec = tween(400))
                }
                candidate < 0 -> {
                    direction = 1
                    pagerState.animateScrollToPage(current + direction, animationSpec = tween(400))
                }
                else -> {
                    pagerState.animateScrollToPage(candidate, animationSpec = tween(400))
                }
            }
        }
    }

    /**
     *     Main UI
     */
    Surface(modifier = Modifier
        .background(Color.Transparent)
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.Transparent
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(vertical = 10.dp)
        ) {

            // Pager takes up remaining space
            OnboardingPager(
                pages = pages,
                pagerState = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            OnboardingIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            AuthActionButtons(
                onSignUpClick = onSignUpClick,
                onLoginClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Pager with card-flip + scale animation for each onboarding page
 */
@Composable
private fun OnboardingPager(
    modifier: Modifier,
    pages: List<OnboardingPage>,
    pagerState: androidx.compose.foundation.pager.PagerState
) {
    val density = LocalDensity.current.density

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
        ,
    ) { page ->
        val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue

        OnboardingPageContent(
            page = pages[page],
            modifier = Modifier
                .graphicsLayer {
                    rotationY = pageOffset * 20f
                    val scale = 1f - (0.1f * pageOffset)
                    scaleX = scale
                    scaleY = scale
                    cameraDistance = 8 * density
                }
        )
    }
}

/**
 * Page indicators below pager
 */
@Composable
private fun OnboardingIndicators(
    modifier: Modifier,
    pageCount: Int,
    currentPage: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            val isSelected = currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

/**
 * Buttons at bottom of onboarding screen
 */
@Composable
private fun AuthActionButtons(
    modifier: Modifier,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            text = stringResource(R.string.onboarding_sign_up_button_text),
            onClick = onSignUpClick,
            cornerRadius = 50.dp
        )

        Button(
            text = stringResource(R.string.onboarding_login_button_text),
            onClick = onLoginClick,
            isOutlined = true,
            cornerRadius = 50.dp
        )
    }
}

/**
 * Single onboarding page content (title, description, image)
 */
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = page.image,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen()
    }
}

