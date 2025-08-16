package ar.com.nexofiscal.nexofiscalposv2.services

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BackupScheduler {
    private const val UNIQUE_PERIODIC_NAME = "db_backup_periodic"
    private const val UNIQUE_ON_DEMAND_NAME = "db_backup_on_demand"

    fun scheduleHourly(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupDatabaseWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<BackupDatabaseWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_ON_DEMAND_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
    }
}

