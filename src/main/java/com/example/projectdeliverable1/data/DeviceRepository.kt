package com.example.projectdeliverable1.data

import android.content.Context
import com.example.projectdeliverable1.models.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DeviceRepository {
    private fun helper(context: Context) = SmartHomeDatabaseHelper(context.applicationContext)

    suspend fun initializeIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        helper(context).seedDefaultDataIfEmpty(getDefaultDevices())
    }

    suspend fun getDevices(context: Context, query: String = "", sortedByName: Boolean = false): List<Device> =
        withContext(Dispatchers.IO) { helper(context).getDevices(query, sortedByName) }

    suspend fun addDevice(context: Context, device: Device): Long =
        withContext(Dispatchers.IO) { helper(context).insertDevice(device) }

    suspend fun updateDevice(context: Context, device: Device): Int =
        withContext(Dispatchers.IO) { helper(context).updateDevice(device) }

    suspend fun deleteDevice(context: Context, deviceId: Int): Int =
        withContext(Dispatchers.IO) { helper(context).deleteDevice(deviceId) }

    suspend fun getCounts(context: Context): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val db = helper(context)
        db.getDeviceCount() to db.getActiveDeviceCount()
    }

    fun getDefaultDevices(): ArrayList<Device> = arrayListOf(
        Device(1, "Living Room Lights", "Living Room", "ON", "Lights", "08:30 AM", "Main lights are currently on and working normally."),
        Device(2, "Bedroom AC", "Bedroom", "OFF", "AC", "07:45 AM", "Air conditioner is turned off to save power."),
        Device(3, "Main Door Lock", "Main Door", "Locked", "Security", "09:00 AM", "Door lock is active and the main entrance is secure."),
        Device(4, "Hallway Thermostat", "Hallway", "22 C", "Thermostat", "08:10 AM", "Temperature is set to a comfortable value for the whole house."),
        Device(5, "Garage Camera", "Garage", "Active", "Camera", "08:50 AM", "Motion tracking camera is active and monitoring the garage."),
        Device(6, "Kitchen Sensor", "Kitchen", "Online", "Sensor", "08:15 AM", "Gas and smoke sensor is online and sending live data."),
        Device(7, "Garden Sprinkler", "Backyard", "Scheduled", "Water", "06:30 AM", "Sprinkler will start automatically based on the saved schedule."),
        Device(8, "Study Room Lamp", "Study Room", "OFF", "Lights", "07:20 AM", "Lamp is currently off and can be controlled from the app.")
    )
}
