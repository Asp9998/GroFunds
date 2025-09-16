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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.data.model.AuthState
import com.aryanspatel.grofunds.data.model.OnboardingPage
import com.aryanspatel.grofunds.navigation.Destinations
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
            navController = navController,
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
            viewModel = viewModel,
            uiState = uiState,
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
 * - Indicators
 * - Bottom action buttons
 */
@Composable
fun OnboardingScreen(
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        OnboardingPage(
            title = "Set Financial Goals",
            description = "Create personalized savings goals and track your progress. Whether it's an emergency fund or vacation, we'll help you get there.",
            image = painterResource(R.drawable.onboarding_img_1)
        ),
        OnboardingPage(
            title = "Smart Guidance",
            description = "Receive AI-powered recommendations and tips to optimize your spending and accelerate your savings journey.",
            image = painterResource(R.drawable.onboarding_img_2)
        ),
        OnboardingPage(
            title = "Track Your Expenses",
            description = "Monitor your daily spending and categorize expenses to understand where your money goes. Get detailed insights into your financial habits.",
            image = painterResource(R.drawable.onboarding_img_3)
        )
    )

    // Auto-scroll pager every few seconds
    LaunchedEffect(pagerState) {
        if (pages.size <= 1) return@LaunchedEffect
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

            // Page indicators above the buttons
            OnboardingIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
    //                .padding(vertical = 16.dp)
            )

            // Bottom action buttons (stick to bottom)
            AuthActionButtons(
                onSignUpClick = onSignUpClick,
                onLoginClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
    //                .padding(horizontal = 16.dp, vertical = 8.dp)
            )

    //        Spacer(modifier = Modifier.height(16.dp))
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
        // Sign up (filled button)
        Button(
            text = "Sign Up",
            onClick = onSignUpClick,
            cornerRadius = 50.dp
        )

        // Log in (outlined button)
        Button(
            text = "Log In",
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
//                color = MaterialTheme.colorScheme.onPrimary
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

//        Spacer(modifier = Modifier.height(25.dp))

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

