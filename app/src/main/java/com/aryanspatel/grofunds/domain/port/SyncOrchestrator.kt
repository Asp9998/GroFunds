package com.aryanspatel.grofunds.domain.port

interface SyncOrchestrator {
    fun startPeriodic()                                                   // daily push and pull
    fun stopPeriodic()                                                    // cancel periodic
    fun syncNow()                                                         // chain : push -> pull (fire and forget)
    fun pullNow()                                                         // one-time pull
    suspend fun pushAllDirtyAndWait(timeoutMs: Long = 15_000): Boolean    // one-time push before sign out
}