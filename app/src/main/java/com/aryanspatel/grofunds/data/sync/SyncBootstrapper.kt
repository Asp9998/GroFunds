package com.aryanspatel.grofunds.data.sync

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.aryanspatel.grofunds.data.local.GroFundsDatabase
import com.aryanspatel.grofunds.domain.port.SyncOrchestrator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncBootstrapper @Inject constructor(
    private val app: Application,
    private val session: SessionStore,
    private val orchestrator: SyncOrchestrator,
    private val db: GroFundsDatabase,     // if you clear on sign-out
    private val auth: FirebaseAuth
){
    private val scope = ProcessLifecycleOwner.get().lifecycleScope

    fun start(){
        scope.launch {
            session.userIdFlow.collect { uid ->
                if(uid != null){
                    // signed in
                    orchestrator.pullNow()
                    orchestrator.startPeriodic()
                }else{
                    // Signed out
                    orchestrator.stopPeriodic()
                    db.clearAllTables()
                }
            }
        }
    }
}