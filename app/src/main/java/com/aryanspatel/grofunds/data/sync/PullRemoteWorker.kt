package com.aryanspatel.grofunds.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.data.repository.SyncRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class PullRemoteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: SyncRepository,
    private val currentUser: CurrentUserProvider
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val uid = currentUser.userIdOrNull() ?: return Result.success()
        return try{
            repo.pullRemoteUpdates(uid)
            Result.success()
        }catch (t: Throwable){
            if(t is IOException) Result.retry() else Result.failure()
        }
    }
}