package com.atomcamp.iot_project

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val PREF_NAME = "IoTPreferences"
    private val FIRST_TIME_KEY = "isFirstTimeUser"
    private val TRUSTED_DEVICE_KEY = "trustedDeviceAddress"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Check if it's the user's first time opening the app
    fun isFirstTimeUser(): Boolean {
        return prefs.getBoolean(FIRST_TIME_KEY, true)
    }

    // Mark that the user has opened the app
    fun setFirstTimeUserFlag(isFirstTime: Boolean) {
        prefs.edit().putBoolean(FIRST_TIME_KEY, isFirstTime).apply()
    }

    // Device state management
    fun getDeviceState(deviceId: Int): Boolean {
        return prefs.getBoolean("device_state_$deviceId", false)
    }

    fun setDeviceState(deviceId: Int, isOn: Boolean) {
        prefs.edit().putBoolean("device_state_$deviceId", isOn).apply()
    }

    // Device time management
    fun getDeviceTime(deviceId: Int): String {
        return prefs.getString("device_time_$deviceId", "00:00:00") ?: "00:00:00"
    }

    fun setDeviceTime(deviceId: Int, time: String) {
        prefs.edit().putString("device_time_$deviceId", time).apply()
    }

    // Device name management
    fun getDeviceName(deviceId: Int, defaultName: String): String {
        return prefs.getString("device_name_$deviceId", defaultName) ?: defaultName
    }

    fun setDeviceName(deviceId: Int, name: String) {
        prefs.edit().putString("device_name_$deviceId", name).apply()
    }

    // Device start time management (for persistent timers)
    fun setDeviceStartTime(deviceId: Int, timestamp: Long) {
        prefs.edit().putLong("device_start_time_$deviceId", timestamp).apply()
    }

    fun getDeviceStartTime(deviceId: Int): Long {
        return prefs.getLong("device_start_time_$deviceId", 0L)
    }

    // Trusted device management
    fun setTrustedDeviceAddress(address: String) {
        prefs.edit().putString(TRUSTED_DEVICE_KEY, address).apply()
    }

    fun getTrustedDeviceAddress(): String {
        return prefs.getString(TRUSTED_DEVICE_KEY, "") ?: ""
    }
}