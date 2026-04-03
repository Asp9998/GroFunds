package com.aryanspatel.grofunds.data.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aryanspatel.grofunds.data.repository.SyncRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import java.io.IOException
import kotlin.math.log

@HiltWorker
class PullRemoteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {

        val deps = EntryPointAccessors.fromApplication(
            applicationContext, WorkerDepsEntryPoint::class.java
        )
        val repo = deps.repo()
        val currentUser = deps.currentUser()

        val uid = currentUser.userIdOrNull() ?: return Result.success()
        return try{
            repo.pullRemoteUpdates(uid)
            Result.success()
        }catch (t: Throwable){
            if(t is IOException) Result.retry() else Result.failure()
        }
    }
}