package com.aryanspatel.grofunds.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aryanspatel.grofunds.presentation.screen.auth.AuthScreen
import com.aryanspatel.grofunds.presentation.screen.expense.ExpenseScreen
import com.aryanspatel.grofunds.presentation.screen.home.HomeScreen
import com.aryanspatel.grofunds.presentation.screen.income.IncomeScreen
import com.aryanspatel.grofunds.presentation.screen.profile.ProfileScreen
import com.aryanspatel.grofunds.presentation.screen.savings.SavingScreen
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun NavGraph(
    authViewModel: AuthViewModel = hiltViewModel() // ViewModel to manage Firebase Auth state
) {

    val navController = rememberNavController()

    // Observe current user state from Firebase (null = logged out, non-null = logged in)
    val user by authViewModel.user.collectAsStateWithLifecycle()

    // Set the start destination based on login status
    val startDestination = if (user != null) {
        Destinations.HomeScreen.name
    } else {
        Destinations.AuthScreen.name
    }

    // NavHost holds all the navigation routes in the app
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Screen
        composable(route = Destinations.AuthScreen.name) {
            // Pass navController so AuthScreen can navigate on success
            // Pass authViewModel so it can call signIn/signUp
            AuthScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // Home Screen (after login)
        composable(route = Destinations.HomeScreen.name) {
            // Pass navController for navigation within Home
            // Add onLogout callback to sign out user and return to AuthScreen
            HomeScreen(
                navController = navController,
                onLogout = {
                    authViewModel.signOut() // Clear Firebase session
                    navController.navigate(Destinations.AuthScreen.name) {
                        // Remove HomeScreen from backstack to prevent going back after logout
                        popUpTo(Destinations.HomeScreen.name) { inclusive = true }
                    }
                }
            )
        }

        //Profile Screen
        composable(route = Destinations.ProfileScreen.name) {
            ProfileScreen(authViewModel = authViewModel)
        }

        // Expense Screen
        composable(route = Destinations.ExpenseScreen.name) {
            ExpenseScreen()
        }

        // Income Screen
        composable(route = Destinations.IncomeScreen.name) {
            IncomeScreen()
        }

        // Saving Screen
        composable(route = Destinations.SavingScreen.name) {
            SavingScreen()
        }
    }
}
