package com.aryanspatel.grofunds.data.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aryanspatel.grofunds.data.repository.SyncRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerDepsEntryPoint {
    fun repo(): SyncRepository
    fun currentUser(): CurrentUserProvider
}


@HiltWorker
class PushDirtyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {

    val deps = EntryPointAccessors.fromApplication(
        applicationContext, WorkerDepsEntryPoint::class.java
    )
    val repo = deps.repo()
    val currentUser = deps.currentUser()

        val uid = currentUser.userIdOrNull()

        if (uid == null) {
            Result.success()
        } else {
            repo.pushDirtyBatch(uid)
            Result.success()
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (io: IOException) {
        Result.retry()
    } catch (t: Throwable) {
        Result.failure(workDataOf("error" to (t.message ?: t::class.java.simpleName)))
    }
}