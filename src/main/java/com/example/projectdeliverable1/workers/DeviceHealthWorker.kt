package com.example.projectdeliverable1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectdeliverable1.data.DeviceRepository
import com.example.projectdeliverable1.notifications.NotificationHelper

class DeviceHealthWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            DeviceRepository.initializeIfNeeded(applicationContext)
            val (total, active) = DeviceRepository.getCounts(applicationContext)
            NotificationHelper.showNotification(
                applicationContext,
                "Smart Home Health Check",
                "Devices tracked: $total. Active devices: $active.",
                6001
            )
            Result.success()
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}
