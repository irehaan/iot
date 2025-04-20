package com.atomcamp.iot_project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(private val context: Context) {
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting Bluetooth adapter: ${e.message}")
            null
        }
    }

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var connectionJob: Job? = null
    private var reconnectionJob: Job? = null
    private var initialized = false

    private val prefManager = PreferenceManager(context)
    private val TAG = "BluetoothManager"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentDeviceName = MutableStateFlow<String?>(null)
    val currentDeviceName: StateFlow<String?> = _currentDeviceName.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothDevice>> = _availableDevices.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
    }


    fun initialize() {
        logPermissionStatus()
        Log.d(TAG, "Explicitly initializing BluetoothManager")

        // Clear error message at start
        _errorMessage.value = null

        try {
            // Check permission first
            if (!hasBluetoothPermission()) {
                _errorMessage.value = "Bluetooth permissions required"
                Log.e(TAG, "Cannot initialize - Bluetooth permission not granted")
                return
            }

            // Then check if Bluetooth is enabled
            if (!isBluetoothEnabled()) {
                _errorMessage.value = "Bluetooth is not enabled"
                Log.e(TAG, "Cannot initialize - Bluetooth not enabled")
                return
            }

            val trustedDevice = getTrustedDevice()
            if (trustedDevice != null) {
                _currentDeviceName.value = try {
                    trustedDevice.name ?: "Unknown Device"
                } catch (e: SecurityException) {
                    "Device ${trustedDevice.address.takeLast(5)}"
                }
            }

            updatePairedDevicesList()
            initialized = true
            _errorMessage.value = null
        } catch (e: SecurityException) {
            _errorMessage.value = "Permission error: ${e.message}"
            Log.e(TAG, "Security exception in initialize: ${e.message}")
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            Log.e(TAG, "Exception in initialize: ${e.message}")
        }
    }

    fun updatePairedDevicesList() {
        _errorMessage.value = null
        try {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = manager?.adapter

            if (adapter != null && adapter.isEnabled) {
                val pairedDevices = adapter.bondedDevices?.toList() ?: emptyList()
                Log.d(TAG, "Found ${pairedDevices.size} paired devices")
                _availableDevices.value = pairedDevices
            } else if (adapter == null) {
                _availableDevices.value = emptyList()
                _errorMessage.value = "Bluetooth not available on this device"
                Log.e(TAG, "Bluetooth adapter is null")
            } else if (!adapter.isEnabled) {
                _availableDevices.value = emptyList()
                _errorMessage.value = "Bluetooth is not enabled"
                Log.e(TAG, "Bluetooth is not enabled")
            }
        } catch (e: SecurityException) {
            _availableDevices.value = emptyList()
            _errorMessage.value = "Bluetooth permission not granted"
            Log.e(TAG, "Security exception accessing paired devices: ${e.message}")
        } catch (e: Exception) {
            _availableDevices.value = emptyList()
            _errorMessage.value = "Error: ${e.message}"
            Log.e(TAG, "Exception getting paired devices: ${e.message}")
        }
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val scanGranted = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            connectGranted && scanGranted
        } else {
            true
        }
    }

    fun logPermissionStatus() {
        val permissionsToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        for (permission in permissionsToCheck) {
            val isGranted = ActivityCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Permission $permission granted: $isGranted")
        }
    }

    fun safeInitialize() {
        if (!hasBluetoothPermission()) {
            Log.e(TAG, "Cannot initialize - permission not granted")
            _errorMessage.value = "Bluetooth permissions required"
            return
        }
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "Cannot initialize - Bluetooth not enabled")
            _errorMessage.value = "Bluetooth is not enabled"
            return
        }
        Log.d(TAG, "Safe initialization starting...")
        try {
            updatePairedDevicesList()
            val trustedDevice = getTrustedDevice()
            if (trustedDevice != null) {
                _currentDeviceName.value = try {
                    trustedDevice.name ?: "Unknown Device"
                } catch (e: SecurityException) {
                    "Device ${trustedDevice.address.takeLast(5)}"
                }
                Log.d(TAG, "Found trusted device during initialization")
            }
            initialized = true
            Log.d(TAG, "Safe initialization completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during safe initialization: ${e.message}")
            _errorMessage.value = "Initialization error: ${e.message}"
        }
    }

    fun connectToDevice(device: BluetoothDevice, scope: CoroutineScope) {
        connectionJob?.cancel()
        _errorMessage.value = null // Clear previous errors
        connectionJob = scope.launch {
            try {
                if (!hasBluetoothPermission()) {
                    _errorMessage.value = "Cannot connect - Bluetooth permission not granted"
                    Log.e(TAG, "Cannot connect - Bluetooth permission not granted")
                    return@launch
                }

                _isConnected.value = false
                closeConnection()

                Log.d(TAG, "Attempting to connect to device: ${device.address}")

                withContext(Dispatchers.IO) {
                    try {
                        bluetoothAdapter?.cancelDiscovery() // Required
                        socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                        Log.d(TAG, "Attempting socket connection...")

                        // Add connection timeout
                        try {
                            withTimeout(5000) { // 5 second timeout
                                socket?.connect()
                            }
                        } catch (e: TimeoutCancellationException) {
                            throw IOException("Connection timed out")
                        }

                        Log.d(TAG, "Socket connected: ${socket?.isConnected}")

                        if (socket?.isConnected == true) {
                            inputStream = socket?.inputStream
                            outputStream = socket?.outputStream
                            _isConnected.value = true
                            _currentDeviceName.value = device.name ?: "Device ${device.address.takeLast(5)}"
                            Log.d(TAG, "Connected to device: $_currentDeviceName")

                            // Start data listener right away
                            startDataListener(scope)
                            _errorMessage.value = null
                        } else {
                            _errorMessage.value = "Failed to connect to device"
                            Log.e(TAG, "Socket not connected after connect() call")
                        }
                    } catch (e: SecurityException) {
                        _errorMessage.value = "Security exception: ${e.message}"
                        Log.e(TAG, "Security exception connecting to device: ${e.message}")
                    } catch (e: IOException) {
                        _errorMessage.value = "Connection failed: ${e.message}"
                        Log.e(TAG, "IOException connecting to device: ${e.message}")

                        // Additional diagnostics for IO errors
                        if (e.message?.contains("socket might closed") == true ||
                            e.message?.contains("read ret: -1") == true) {
                            _errorMessage.value = "Device may be powered off or out of range"
                        } else {

                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e(TAG, "Failed to connect to device: ${e.message}")
                closeConnection()
            }
        }
    }

    // Mark a device as trusted (to be auto-connected)
    fun setTrustedDevice(device: BluetoothDevice?) {
        if (!hasBluetoothPermission() && device != null) {
            _errorMessage.value = "Cannot set trusted device - permission not granted"
            Log.e(TAG, "Cannot set trusted device - permission not granted")
            return
        }

        device?.let {
            try {
                val address = it.address
                prefManager.setTrustedDeviceAddress(address)

                try {
                    Log.d(TAG, "Set ${it.name} as trusted device")
                } catch (e: SecurityException) {
                    Log.d(TAG, "Set device ${address} as trusted device")
                }
            } catch (e: SecurityException) {
                _errorMessage.value = "Security exception: ${e.message}"
                Log.e(TAG, "Security exception accessing device address: ${e.message}")
            }
        } ?: run {
            // If null, clear trusted device
            prefManager.setTrustedDeviceAddress("")
            Log.d(TAG, "Cleared trusted device")
        }
    }

    // Get the current trusted device
    fun getTrustedDevice(): BluetoothDevice? {
        val trustedAddress = prefManager.getTrustedDeviceAddress()
        Log.d(TAG, "Getting trusted device with address: $trustedAddress")

        if (trustedAddress.isBlank()) {
            Log.d(TAG, "No trusted device address saved")
            return null
        }

        if (!hasBluetoothPermission()) {
            Log.e(TAG, "Cannot get trusted device - permission not granted")
            return null
        }

        try {
            val trustedDevice = bluetoothAdapter?.bondedDevices?.find {
                try {
                    it.address == trustedAddress
                } catch (e: SecurityException) {
                    false
                }
            }

            Log.d(TAG, "Found trusted device: ${trustedDevice != null}")
            return trustedDevice
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception accessing bonded devices: ${e.message}")
            return null
        }
    }

    // Start auto-reconnect mechanism
    fun startAutoReconnect(scope: CoroutineScope) {
        // Cancel existing job first
        reconnectionJob?.cancel()
        reconnectionJob = null

        Log.d(TAG, "Starting auto-reconnect mechanism")

        // Keep track of consecutive failed attempts
        var failedAttempts = 0
        val maxFailedAttempts = 5

        reconnectionJob = scope.launch {
            while (true) {
                val currentlyConnected = _isConnected.value
                Log.d(TAG, "Auto-reconnect check - Currently connected: $currentlyConnected")

                if (!currentlyConnected) {
                    // Only try if we have permission and Bluetooth is enabled
                    if (hasBluetoothPermission() && isBluetoothEnabled()) {
                        // Try to connect to trusted device
                        val trustedDevice = getTrustedDevice()
                        if (trustedDevice != null) {
                            Log.d(TAG, "Auto-reconnecting to trusted device: ${trustedDevice.address}")

                            // Don't reuse the same scope to avoid nesting
                            // Create a new separate scope for this connection attempt
                            val connectionScope = CoroutineScope(Dispatchers.Main)

                            try {
                                // Try connection with timeout
                                withTimeout(5000) { // 5 second timeout
                                    connectToDevice(trustedDevice, connectionScope)
                                    // Wait a bit to see if connection succeeded
                                    delay(1000)

                                    // Reset failed counter on success
                                    if (_isConnected.value) {
                                        failedAttempts = 0
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Auto-reconnect attempt failed: ${e.message}")
                                failedAttempts++
                            }

                            // If we've failed multiple times in a row, back off
                            if (failedAttempts >= maxFailedAttempts) {
                                Log.d(TAG, "Reached max failed attempts ($maxFailedAttempts). Backing off...")
                                _errorMessage.value = "Device may be out of range. Reducing reconnection attempts."
                                // Wait longer between attempts after several failures
                                delay(10000) // 10 second delay after repeated failures
                                continue
                            }
                        }
                    } else {
                        Log.d(TAG, "Auto-reconnect skipped - no permissions or Bluetooth disabled")
                    }
                } else {
                    // Reset failed counter when connected
                    failedAttempts = 0
                }

                // Base delay is 2 seconds as per requirements
                // But add exponential backoff based on consecutive failures
                val delayTime = if (failedAttempts > 0) {
                    minOf(2000L * (1 shl failedAttempts), 30000L) // Cap at 30 seconds
                } else {
                    2000L // Default 2 second interval
                }

                delay(delayTime)
            }
        }
    }

    // Stop auto-reconnect mechanism
    fun stopAutoReconnect() {
        Log.d(TAG, "Stopping auto-reconnect mechanism")
        reconnectionJob?.cancel()
        reconnectionJob = null
    }

    // Send commands to the device
    fun sendCommand(command: Char) {
        try {
            outputStream?.write(command.code)
            outputStream?.flush()
            Log.d(TAG, "Sent command: $command")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command: ${e.message}")
            closeConnection()
        }
    }

    // Turn ON a specific device (1-8)
    fun turnDeviceOn(deviceNumber: Int) {
        if (deviceNumber < 1 || deviceNumber > 8) return
        // Small letters (a-h) turn ON devices
        val command = ('a'.code + (deviceNumber - 1)).toChar()
        Log.d(TAG, "Turning device $deviceNumber ON with command: $command")
        sendCommand(command)
    }

    // Turn OFF a specific device (1-8)
    fun turnDeviceOff(deviceNumber: Int) {
        if (deviceNumber < 1 || deviceNumber > 8) return
        // Capital letters (A-H) turn OFF devices
        val command = ('A'.code + (deviceNumber - 1)).toChar()
        Log.d(TAG, "Turning device $deviceNumber OFF with command: $command")
        sendCommand(command)
    }

    // Listen for incoming data from the Bluetooth device
    private fun startDataListener(scope: CoroutineScope) {
        Log.d(TAG, "Starting data listener")
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)

            try {
                while (_isConnected.value) {
                    val bytesRead = inputStream?.read(buffer)

                    if (bytesRead != null && bytesRead > 0) {
                        val data = String(buffer, 0, bytesRead)
                        Log.d(TAG, "Received raw data: $data")
                        processReceivedData(data)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading data: ${e.message}")
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Connection lost: ${e.message}"
                    closeConnection()
                }
            }
        }
    }

    // Process data received from the Bluetooth device
    private fun processReceivedData(data: String) {
        Log.d(TAG, "Processing received data: $data")

        // Process each character in the received data
        for (char in data) {
            when (char) {
                // Capital letters (A-H) mean device is OFF
                in 'A'..'H' -> {
                    val deviceNumber = char - 'A' + 1
                    updateDeviceState(deviceNumber, false)
                }
                // Small letters (a-h) mean device is ON
                in 'a'..'h' -> {
                    val deviceNumber = char - 'a' + 1
                    updateDeviceState(deviceNumber, true)
                }
            }
        }
    }

    // Update device state in preferences
    private fun updateDeviceState(deviceNumber: Int, isOn: Boolean) {
        Log.d(TAG, "Updating device $deviceNumber state to ${if (isOn) "ON" else "OFF"}")
        prefManager.setDeviceState(deviceNumber, isOn)

        // Reset timer if turning on the device
        if (isOn) {
            val newStartTime = System.currentTimeMillis()
            prefManager.setDeviceStartTime(deviceNumber, newStartTime)
            prefManager.setDeviceTime(deviceNumber, "00:00:00")
        } else {
            prefManager.setDeviceStartTime(deviceNumber, 0L)
        }
    }

    // Close the current Bluetooth connection
    fun closeConnection() {
        Log.d(TAG, "Closing Bluetooth connection")
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        } finally {
            socket = null
            inputStream = null
            outputStream = null
            _isConnected.value = false
            _currentDeviceName.value = null
        }
    }

    // Check if Bluetooth is enabled
    fun isBluetoothEnabled(): Boolean {
        // Try more direct approach first
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

        try {
            val adapter = manager?.adapter
            val enabled = adapter?.isEnabled == true
            Log.d(TAG, "Direct Bluetooth status check: adapter=${adapter != null}, enabled=$enabled")

            if (adapter != null) {
                return enabled
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in direct Bluetooth status check: ${e.message}")
        }

        // Fallback to the original method
        try {
            val enabled = bluetoothAdapter?.isEnabled == true
            Log.d(TAG, "Fallback Bluetooth enabled check: $enabled")
            return enabled
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking if Bluetooth is enabled: ${e.message}")
            return false
        }
    }
}