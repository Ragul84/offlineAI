package com.edgeai.tutorlite.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.edgeai.tutorlite.service.device.BatteryThermalMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ModelHealthWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val batteryThermalMonitor: BatteryThermalMonitor
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        batteryThermalMonitor.refresh()
        return Result.success()
    }
}
