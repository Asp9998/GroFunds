package com.aryanspatel.grofunds

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.work.Configuration
import com.aryanspatel.grofunds.data.sync.SyncBootstrapper
import com.google.firebase.FirebaseApp


@HiltAndroidApp
class GroFundsApplication() : Application()
    , Configuration.Provider {
    @Inject lateinit var syncBootstrapper: SyncBootstrapper

    @Inject lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()

       // Ensure Firebase is ready before anything that may touch it
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

//        // Start background sync safely (don’t let it crash startup)
        runCatching { syncBootstrapper.start() }
            .onFailure { e -> android.util.Log.e("AppInit", "Sync bootstrap failed", e) }

//        // Optional: debug strict mode
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            enableStrictMode()
        }
    }

    /**
     * Returns true if the installed app is marked as debuggable.
     * This works regardless of which module this class lives in (app or library).
     */
    private fun isAppDebuggable(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * StrictMode helps catch accidental disk/network work on the main thread,
     * leaked resources, etc. Use it in debug to surface performance issues early.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()        // Enable all thread checks: disk reads/writes, network on main, custom slow calls, etc.
                .permitDiskReads()  // TEMPORARY relaxation: allows disk *reads* on main to reduce noise (e.g., Compose previews).
                                    // Remove this once you clean up warnings, or replace with .detectDiskReads() for stricter checks.
                .penaltyLog()       // Log violations to Logcat instead of crashing the app (good for day-to-day dev).
                //.penaltyDialog()   // (Optional) Pop a dialog on violations — noisy but very visible.
                //.penaltyDeath()    // (Optional) Crash on violation — good for CI/debug hardening after cleanup.
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()         // Enable all VM checks: leaked closable, activity leaks, file URI exposure, etc.
                .penaltyLog()        // Log VM violations to Logcat.
                //.penaltyDeath()    // (Optional) Crash on VM violations.
                .build()
        )
    }
}
