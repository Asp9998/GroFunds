package com.aryanspatel.grofunds.data.work

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.aryanspatel.grofunds.data.sync.PullRemoteWorker
import com.aryanspatel.grofunds.data.sync.PushDirtyWorker
import com.aryanspatel.grofunds.domain.port.SyncOrchestrator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerSyncOrchestrator @Inject constructor(
    private val app: Application
): SyncOrchestrator{

    private val wm get() = WorkManager.getInstance(app)

    override fun startPeriodic() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val dailyPush = PeriodicWorkRequestBuilder<PushDirtyWorker>(
            1, TimeUnit.DAYS,
            2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("push-periodic")
            .build()

        wm.enqueueUniquePeriodicWork(
            "push-periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyPush
        )

        val dailyPull = PeriodicWorkRequestBuilder<PullRemoteWorker>(
            1, TimeUnit.DAYS,
            2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("pull-periodic")
            .build()

        wm.enqueueUniquePeriodicWork(
            "pull-periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyPull
        )

    }

    override fun stopPeriodic() {
        wm.cancelUniqueWork("push-periodic")
        wm.cancelUniqueWork("pull-periodic")
    }

    override fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val push = OneTimeWorkRequestBuilder<PushDirtyWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        val pull = OneTimeWorkRequestBuilder<PullRemoteWorker>()
            .setConstraints(constraints)
            .build()

        wm.beginUniqueWork("sync-now", ExistingWorkPolicy.APPEND_OR_REPLACE, push)
            .then(pull)
            .enqueue()
    }

    override fun pullNow() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        wm.enqueueUniqueWork(
            "pull-now",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<PullRemoteWorker>().setConstraints(constraints).build()
        )
    }

    override suspend fun pushAllDirtyAndWait(timeoutMs: Long): Boolean {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val req = OneTimeWorkRequestBuilder<PushDirtyWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("sign-out-push")
            .build()

        wm.enqueueUniqueWork("push-before-sign-out", ExistingWorkPolicy.REPLACE, req)

        // Wait (bounded) till it finishes
        return withTimeoutOrNull(timeoutMs) {
            wm.getWorkInfoByIdFlow(req.id).first { it!!.state.isFinished}
        }?.let { info ->
            info.state == WorkInfo.State.SUCCEEDED
        } ?: false
    }

}