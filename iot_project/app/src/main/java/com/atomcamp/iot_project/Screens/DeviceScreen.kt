package com.atomcamp.iot_project.Screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.atomcamp.iot_project.BluetoothManager
import com.atomcamp.iot_project.PreferenceManager
import kotlinx.coroutines.launch
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.atomcamp.iot_project.R
import kotlinx.coroutines.delay

@Composable
fun DevicesScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onNavigateToNames: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val bluetoothManager = remember { BluetoothManager(context) }
    val prefManager = remember { PreferenceManager(context) }

    val availableDevices by bluetoothManager.availableDevices.collectAsState()
    val isConnected by bluetoothManager.isConnected.collectAsState()
    val currentDeviceName by bluetoothManager.currentDeviceName.collectAsState()
    val errorMessage by bluetoothManager.errorMessage.collectAsState()

    var permissionsGranted by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showBluetoothEnableDialog by remember { mutableStateOf(false) }

    val autoConnectState = remember { mutableStateMapOf<String, Boolean>() }

    val displayName = remember(currentDeviceName) {
        currentDeviceName ?: "Unknown Device"
    }

    DisposableEffect(Unit) {
        // Clear any stale error messages when screen is shown
        bluetoothManager.clearErrorMessage()
        onDispose {
            // Clean up connections when leaving the screen
            bluetoothManager.stopAutoReconnect()
        }
    }

    LaunchedEffect(Unit) {
        // Initialize autoConnect state from preferences
        val trustedDevice = bluetoothManager.getTrustedDevice()
        trustedDevice?.let { device ->
            // Set the auto-connect checkbox state for the trusted device
            autoConnectState[device.address] = true
            Log.d("DevicesScreen", "Restored auto-connect state for trusted device: ${device.address}")
        }
    }

    fun checkPermissionsAndBluetooth() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

        permissionsGranted = permissionsToRequest.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        bluetoothEnabled = bluetoothManager.isBluetoothEnabled()

        if (permissionsGranted && bluetoothEnabled) {
            bluetoothManager.safeInitialize()
            bluetoothManager.updatePairedDevicesList()
        }
    }

    val requestPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (perm, granted) ->
            Log.d("DevicesScreen", "Permission $perm granted: $granted")
        }

        bluetoothManager.logPermissionStatus()

        scope.launch {
            delay(500)
            permissionsGranted = bluetoothManager.hasBluetoothPermission()
            bluetoothEnabled = bluetoothManager.isBluetoothEnabled()

            if (permissionsGranted && bluetoothEnabled) {
                bluetoothManager.initialize()
                bluetoothManager.updatePairedDevicesList()
            } else if (!bluetoothEnabled) {
                showBluetoothEnableDialog = true
            } else if (!permissionsGranted) {
                showPermissionDialog = true
            }
        }
    }


    LaunchedEffect(Unit) {
        permissionsGranted = bluetoothManager.hasBluetoothPermission()

        if (!permissionsGranted) {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
            requestPermissions.launch(permissionsToRequest)
        } else {
            bluetoothEnabled = bluetoothManager.isBluetoothEnabled()

            if (!bluetoothEnabled) {
                showBluetoothEnableDialog = true
            } else {
                delay(300)
                bluetoothManager.initialize()
                bluetoothManager.updatePairedDevicesList()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsGranted = bluetoothManager.hasBluetoothPermission()
                bluetoothEnabled = bluetoothManager.isBluetoothEnabled()

                if (permissionsGranted && bluetoothEnabled) {
                    bluetoothManager.updatePairedDevicesList()
                    val trustedDevice = bluetoothManager.getTrustedDevice()
                    if (trustedDevice != null && !bluetoothManager.isConnected.value) {
                        bluetoothManager.startAutoReconnect(scope)
                    }
                } else if (!bluetoothEnabled) {
                    showBluetoothEnableDialog = true
                }
            }
            if (event == Lifecycle.Event.ON_STOP) {
                bluetoothManager.stopAutoReconnect()
            } else if (event == Lifecycle.Event.ON_START) {
                val trustedDevice = bluetoothManager.getTrustedDevice()
                if (trustedDevice != null) {
                    bluetoothManager.startAutoReconnect(scope)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with enhanced contrast
        Image(
            painter = painterResource(id = R.drawable.app_background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            // Adding alpha modifier to make the image more vibrant and clear
            alpha = 1f  // Full opacity for maximum clarity
        )

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selected = "Devices",
                    onHomeClick = onNavigateToHome,
                    onDevicesClick = onNavigateToDevices,
                    onNamesClick = onNavigateToNames,
                    onAboutClick = onNavigateToAbout
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                // Header section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "HomeClick",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(5f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Select a Device to",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(5f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                        Text(
                            text = "Connect",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(5f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }

                    // Logo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF333333), shape = CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HC",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status indicator for current connection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isConnected) "Connected to: $displayName" else "Not connected",
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let { message ->
                    if (message.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFCCCC))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = message,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }


                // Refresh button
                Button(
                    onClick = {
                        scope.launch {
                            // Initialize first to ensure proper setup
                            bluetoothManager.initialize()
                            bluetoothManager.updatePairedDevicesList()
                            Toast.makeText(context, "Refreshing devices list", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8BC34A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("Refresh Devices List", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add in DevicesScreen.kt, somewhere in the UI
                Button(
                    onClick = {
                        scope.launch {
                            // Reset bluetooth manager state
                            permissionsGranted = bluetoothManager.hasBluetoothPermission()
                            bluetoothEnabled = bluetoothManager.isBluetoothEnabled()

                            Toast.makeText(
                                context,
                                "Permission status: $permissionsGranted, Bluetooth enabled: $bluetoothEnabled",
                                Toast.LENGTH_LONG
                            ).show()

                            if (permissionsGranted && bluetoothEnabled) {
                                bluetoothManager.initialize()
                                bluetoothManager.updatePairedDevicesList()
                                Toast.makeText(
                                    context,
                                    "Bluetooth manager reset successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("Force Reset Bluetooth", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Devices list with headers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE0E0E0))
                ) {
                    Column {
                        // Headers for the table
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Devices\nAvailable",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )

                            Text(
                                text = "Connect\nAutomatically",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        // List of devices
                        if (availableDevices.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No paired devices found.\nPlease pair devices in Bluetooth settings.",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn {
                                items(availableDevices) { device ->
                                    val hasPermission = remember {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            ActivityCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            ) == PackageManager.PERMISSION_GRANTED
                                        } else {
                                            true // Permission not needed for older Android versions
                                        }
                                    }

                                    if (hasPermission) {
                                        DeviceItem(
                                            device = device,
                                            isConnected = isConnected && currentDeviceName == device.name,
                                            isAutoConnect = autoConnectState[device.address]
                                                ?: false,
                                            onDeviceClick = {
                                                // Connect to the device
                                                scope.launch {
                                                    bluetoothManager.connectToDevice(device, scope)
                                                    Toast.makeText(
                                                        context,
                                                        "Connecting to ${
                                                            try {
                                                                device.name ?: "device"
                                                            } catch (e: SecurityException) {
                                                                "device"
                                                            }
                                                        }...",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            onAutoConnectChange = { isChecked ->
                                                // Update auto-connect state
                                                autoConnectState[device.address] = isChecked

                                                // Set or clear trusted device
                                                if (isChecked) {
                                                    bluetoothManager.setTrustedDevice(device)

                                                    // Uncheck all other devices
                                                    autoConnectState.keys.filter { it != device.address }
                                                        .forEach { autoConnectState[it] = false }

                                                    // Start auto-reconnect
                                                    bluetoothManager.startAutoReconnect(scope)

                                                    Toast.makeText(
                                                        context,
                                                        "${
                                                            try {
                                                                device.name ?: "Device"
                                                            } catch (e: SecurityException) {
                                                                "Device"
                                                            }
                                                        } set as trusted device",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    // Clear trusted device if this was the trusted one
                                                    if (bluetoothManager.getTrustedDevice()?.address == device.address) {
                                                        bluetoothManager.setTrustedDevice(null)
                                                        bluetoothManager.stopAutoReconnect()

                                                        Toast.makeText(
                                                            context,
                                                            "Trusted device cleared",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        )
                                    } else {
                                        // Show placeholder or message when permission is not granted
                                        Text(
                                            text = "Bluetooth permissions required",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Show permission dialog if needed
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = {
                        Text(
                            text = "Bluetooth Permissions Required",
                            style = TextStyle(color = Color.White)
                        )
                    },
                    text = {
                        Text(
                            text = "HomeClick needs Bluetooth permissions to connect to your home automation system. Please grant these permissions in Settings.",
                            style = TextStyle(color = Color.White)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                try {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Could not open settings. Please open settings manually.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        ) {
                            Text("Open Settings", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showPermissionDialog = false }) {
                            Text("Cancel", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF001F4D) // Optional dark background
                )
            }


            // Show Bluetooth enable dialog if needed
            if (showBluetoothEnableDialog) {
                AlertDialog(
                    onDismissRequest = { showBluetoothEnableDialog = false },
                    title = {
                        Text(
                            text = "Bluetooth is Disabled",
                            style = TextStyle(color = Color.White)
                        )
                    },
                    text = {
                        Text(
                            text = "HomeClick needs Bluetooth to be enabled. Please enable Bluetooth in Settings.",
                            style = TextStyle(color = Color.White)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showBluetoothEnableDialog = false
                                try {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Could not open Bluetooth settings. Please enable Bluetooth manually.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        ) {
                            Text("Open Bluetooth Settings", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showBluetoothEnableDialog = false }) {
                            Text("Cancel", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF001F4D) // Optional: dark background for better contrast
                )
            }

        }
    }
}


@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    isAutoConnect: Boolean,
    onDeviceClick: () -> Unit,
    onAutoConnectChange: (Boolean) -> Unit
) {
    // Safe way to get device name with permissions handling
    val deviceName = remember {
        try {
            device.name ?: "Unknown device"
        } catch (e: SecurityException) {
            // If we get a security exception, fall back to a partial MAC address
            "Device ${device.address.takeLast(5)}"
        }
    }

    val backgroundColor = if (isConnected) Color(0xFFD2F5D2) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onDeviceClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device name
        Column {
            Text(
                text = deviceName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = device.address,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Auto-connect checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                .padding(2.dp) // Padding to avoid clipping the checkbox
        ) {
            Checkbox(
                checked = isAutoConnect,
                onCheckedChange = { newValue ->
                    onAutoConnectChange(newValue)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6699CC),
                    uncheckedColor = Color.White,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDevicesScreen() {
    DevicesScreen()
}