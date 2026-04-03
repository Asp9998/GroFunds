package com.aryanspatel.grofunds.presentation.common.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.Text
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.screen.auth.AuthScreen
import com.aryanspatel.grofunds.presentation.screen.showTransaction.ExpenseScreen
import com.aryanspatel.grofunds.presentation.screen.home.HomeScreen
import com.aryanspatel.grofunds.presentation.screen.initialPreferences.InitialPreferencesScreen
import com.aryanspatel.grofunds.presentation.screen.showTransaction.IncomeScreen
import com.aryanspatel.grofunds.presentation.screen.profile.ProfileScreen
import com.aryanspatel.grofunds.presentation.screen.savings.SavingScreen
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.GateViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.HomeScreenViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.InitialPreferencesViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.SavingsViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.ShowTransactionViewModel

@Composable
fun NavGraph(
) {
    val navController = rememberNavController()

    val vm: GateViewModel = hiltViewModel()
    val state by vm.gateState.collectAsStateWithLifecycle()

    val startDestination: String? = when (val s = state) {
        GateViewModel.GateState.Loading -> null
        is GateViewModel.GateState.Go -> s.dest.name
        is GateViewModel.GateState.Error -> "Error" // or a safe fallback
    }

    if (startDestination == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            ProgressIndicator()
        }
    } else {

    // NavHost holds all the navigation routes in the app
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = {fadeOut(animationSpec = tween(0))},
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {

        composable("Gate"){
            val gateViewModel = hiltViewModel<GateViewModel>()
            val state by gateViewModel.gateState.collectAsStateWithLifecycle()

            when(val s = state){
                GateViewModel.GateState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ProgressIndicator()}
                }
                is GateViewModel.GateState.Error -> {
                    Log.d("InitialError", ": ${s.message}")
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Error: ${s.message}")
                    }

                }
                is GateViewModel.GateState.Go -> {
                    navController.navigate(s.dest.name){
                        popUpTo("Gate"){inclusive = true}
                        launchSingleTop = true
                    }
                }
            }
        }

        /** 1) Authentication Screen */
        composable(route = Destinations.AuthScreen.name) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            AuthScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        /** 2) Initial Preference screen for account set up (Currency & Budget)
         *     - run only once.
         */
        composable(route = Destinations.InitialPreferencesScreen.name){
            val initialPreferencesViewModel = hiltViewModel<InitialPreferencesViewModel>()
            InitialPreferencesScreen(
                navController = navController,
                viewModel = initialPreferencesViewModel
            )
        }

        /** 3) Home Screen (Main Dashboard) */
        composable(route = Destinations.HomeScreen.name) {
            val homeScreenViewModel = hiltViewModel<HomeScreenViewModel>()
            HomeScreen(
                navController = navController,
                viewModel = homeScreenViewModel,
            )
        }

        /** 4) Profile Screen */
        composable(route = Destinations.ProfileScreen.name) {
            val initialPreferencesViewModel = hiltViewModel<InitialPreferencesViewModel>()
            ProfileScreen(
                viewModel = initialPreferencesViewModel
            )
        }

        /** 5) Show Transactions
         *     i) Expense Screen
         */
        composable(
            route = Destinations.ExpenseScreen.name,
            arguments = listOf(
                navArgument("kind"){
                    type = NavType.StringType
                    defaultValue = "expense"
                }
            )
        ) { backStackEntry ->
            val showTransactionViewModel = hiltViewModel<ShowTransactionViewModel>(backStackEntry)
            ExpenseScreen(viewModel = showTransactionViewModel)
        }

        /** 5) Show Transactions
         *     ii) Income Screen
         */
        composable(route = Destinations.IncomeScreen.name,
            arguments = listOf(
                navArgument("kind"){
                    type = NavType.StringType
                    defaultValue = "income"
                }
            )
        ) { backStackEntry ->
            val showTransactionViewModel = hiltViewModel<ShowTransactionViewModel>(backStackEntry)
            IncomeScreen(viewModel = showTransactionViewModel)
        }

        /**
         *  6) Saving Screen
         */
        composable(route = Destinations.SavingScreen.name) {
            val savingsViewModel = hiltViewModel<SavingsViewModel>()
            SavingScreen(
                viewModel = savingsViewModel
            )
        }
    }
    }
}
