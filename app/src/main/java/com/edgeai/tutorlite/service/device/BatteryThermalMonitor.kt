package com.edgeai.tutorlite.service.device

import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryThermalMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class DeviceState(
        val batteryPct: Int,
        val thermalLevel: Int,
        val shouldThrottle: Boolean
    )

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<DeviceState> = _state.asStateFlow()

    fun refresh() {
        _state.value = readState()
    }

    private fun readState(): DeviceState {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val thermal = pm.currentThermalStatus
        val shouldThrottle =
            batteryPct < 15 || thermal >= PowerManager.THERMAL_STATUS_SEVERE
        return DeviceState(batteryPct, thermal, shouldThrottle)
    }
}
