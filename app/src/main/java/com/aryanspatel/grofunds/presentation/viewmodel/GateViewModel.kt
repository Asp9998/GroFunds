package com.aryanspatel.grofunds.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.data.repository.UserPreferencesRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import com.aryanspatel.grofunds.presentation.common.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GateViewModel @Inject constructor(
    private val currentUser: CurrentUserProvider,
    private val repo: UserPreferencesRepository
): ViewModel(){

    sealed interface GateState {
        data object Loading : GateState
        data class Go(val dest: Destinations) : GateState
        data class Error(val message: String) : GateState
    }

    val gateState : StateFlow<GateState> =
        flow<GateState> {
            emit(GateState.Loading)

            val uid = currentUser.userIdOrNull()
            if(uid == null){
                emit(GateState.Go(Destinations.AuthScreen))
            } else{
                emit(GateState.Loading)
                emitAll(
                    repo.observePreferences(userId = uid)
                        .map { prefs ->
                            val needSetUp = prefs == null || prefs.currencyCode.isBlank()
                            if (needSetUp) GateState.Go(Destinations.InitialPreferencesScreen)
                            else GateState.Go(Destinations.HomeScreen)
                        }
                )
            }
        }.catch { e ->

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GateState.Loading
        )
}

