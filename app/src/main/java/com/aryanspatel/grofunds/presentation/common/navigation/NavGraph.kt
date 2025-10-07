package com.aryanspatel.grofunds.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aryanspatel.grofunds.presentation.screen.auth.AuthScreen
import com.aryanspatel.grofunds.presentation.screen.showTransaction.ExpenseScreen
import com.aryanspatel.grofunds.presentation.screen.home.HomeScreen
import com.aryanspatel.grofunds.presentation.screen.showTransaction.IncomeScreen
import com.aryanspatel.grofunds.presentation.screen.profile.ProfileScreen
import com.aryanspatel.grofunds.presentation.screen.savings.SavingScreen
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.HomeScreenViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.ShowTransactionViewModel

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
            val vm = hiltViewModel<HomeScreenViewModel>()
            HomeScreen(
                navController = navController,
                viewModel = vm,
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
        composable(
            route = Destinations.ExpenseScreen.name,
            arguments = listOf(
                navArgument("kind"){
                    type = NavType.StringType
                    defaultValue = "expense"
                }
            )
        ) { backStackEntry ->
            val vm = hiltViewModel<ShowTransactionViewModel>(backStackEntry)
            ExpenseScreen(viewModel = vm)
        }

        // Income Screen
        composable(route = Destinations.IncomeScreen.name,
            arguments = listOf(
                navArgument("kind"){
                    type = NavType.StringType
                    defaultValue = "income"
                }
            )
        ) { backStackEntry ->
            val vm = hiltViewModel<ShowTransactionViewModel>(backStackEntry)
            IncomeScreen(viewModel = vm)
        }

        // Saving Screen
        composable(route = Destinations.SavingScreen.name) {
            SavingScreen()
        }
    }
}
