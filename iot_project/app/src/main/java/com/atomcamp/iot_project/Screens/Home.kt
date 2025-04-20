package com.atomcamp.iot_project.Screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atomcamp.iot_project.PreferenceManager
import com.atomcamp.iot_project.R
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


@Composable
fun HomeScreen(
    onNavigateToDevices: () -> Unit = {},
    onNavigateToNames: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }

    // Check if it's the first time the user is opening the app
    val isFirstTime = remember { mutableStateOf(prefManager.isFirstTimeUser()) }

    // If it's the first time, initialize device states to OFF and time to 00:00:00
    if (isFirstTime.value) {
        SideEffect {
            for (i in 1..8) {
                prefManager.setDeviceState(i, false)
                prefManager.setDeviceTime(i, "00:00:00")
                prefManager.setDeviceStartTime(i, 0L) // Initialize start times too
            }
            prefManager.setFirstTimeUserFlag(false)
        }
    }
    // If app is restarting (not first time), ensure timers for ON devices continue
    else {
        LaunchedEffect(Unit) {
            // Check each device that might be ON
            for (i in 1..8) {
                if (prefManager.getDeviceState(i)) {
                    // If device is ON but no start time, it might be from before this feature
                    // was added, so set the start time to now
                    if (prefManager.getDeviceStartTime(i) == 0L) {
                        prefManager.setDeviceStartTime(i, System.currentTimeMillis())
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selected = "Home",
                onHomeClick = onNavigateToHome,
                onDevicesClick = onNavigateToDevices,
                onNamesClick = onNavigateToNames,
                onAboutClick = onNavigateToAbout
            )
        },
        containerColor = Color(0xFF001F4D)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.app_background), // Replace with your actual image resource
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // Adding alpha modifier to make the image more vibrant and clear
                alpha = 1f  // Full opacity for maximum clarity
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
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
                        Text(
                            text = "Control Panel",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(3f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Control Appliances here",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(3f, 1f),
                                    blurRadius = 2f
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

                // Grid of devices
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DeviceCard(
                            number = 1,
                            name = "Room 1 Light",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DeviceCard(
                            number = 2,
                            name = "Room 2 Fan",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DeviceCard(
                            number = 3,
                            name = "Water Pump",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DeviceCard(
                            number = 4,
                            name = "Guestroom Light",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DeviceCard(
                            number = 5,
                            name = "Street Light",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DeviceCard(
                            number = 6,
                            name = "Lounge LED TV",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DeviceCard(
                            number = 7,
                            name = "Microwave Oven",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DeviceCard(
                            number = 8,
                            name = "Lounge Fan",
                            prefManager = prefManager,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Connection status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFDDDDDD).copy(alpha = 0.8f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Connected to: \nJDY-31-SPP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    number: Int,
    name: String,
    prefManager: PreferenceManager,
    modifier: Modifier = Modifier
) {
    // Get the saved state and name for this device
    var isOn by remember { mutableStateOf(prefManager.getDeviceState(number)) }
    val savedName = remember { mutableStateOf(prefManager.getDeviceName(number, name)) }

    // Timer display state
    var displayTime by remember { mutableStateOf(prefManager.getDeviceTime(number)) }

    // Handle timer logic
    LaunchedEffect(isOn) {
        if (isOn) {
            // Device is ON - check if we already have a start time
            val startTime = prefManager.getDeviceStartTime(number)

            // If this is the first time turning ON (no start time saved)
            if (startTime == 0L) {
                // Set new start time and reset display
                val newStartTime = System.currentTimeMillis()
                prefManager.setDeviceStartTime(number, newStartTime)
                displayTime = "00:00:00"
                prefManager.setDeviceTime(number, displayTime)
            } else {
                // Calculate existing elapsed time based on stored start time
                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                displayTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                prefManager.setDeviceTime(number, displayTime)
            }

            // Continue updating the timer
            while (isActive && isOn) {
                delay(1000) // Update every second

                // Recalculate elapsed time based on stored start time
                val currentStartTime = prefManager.getDeviceStartTime(number)
                if (currentStartTime > 0) {
                    val elapsedSeconds = (System.currentTimeMillis() - currentStartTime) / 1000
                    val hours = elapsedSeconds / 3600
                    val minutes = (elapsedSeconds % 3600) / 60
                    val seconds = elapsedSeconds % 60
                    displayTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    prefManager.setDeviceTime(number, displayTime)
                }
            }
        }
    }

    val backgroundColor = if (isOn) Color(0xFFFFFF00) else Color(0xFFDDDDDD)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isOn) "ON" else "OFF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Switch(
                    checked = isOn,
                    onCheckedChange = { newState ->
                        isOn = newState
                        prefManager.setDeviceState(number, newState)

                        if (newState) {
                            // When turning ON, reset timer to zero
                            val newStartTime = System.currentTimeMillis()
                            prefManager.setDeviceStartTime(number, newStartTime)
                            displayTime = "00:00:00"
                            prefManager.setDeviceTime(number, displayTime)
                        } else {
                            // When turning OFF, stop the timer but keep the final time value
                            prefManager.setDeviceStartTime(number, 0L)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFFF5722)
                    ),
                    modifier = Modifier.size(40.dp, 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = savedName.value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = displayTime,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

@Composable
fun BottomNavigationBar(
    selected: String,
    onHomeClick: () -> Unit = {},
    onDevicesClick: () -> Unit = {},
    onNamesClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val items = listOf("Home", "Devices", "Names", "About")
    BottomNavigation(
        backgroundColor = Color(0xFF6699CC),
        elevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                val isSelected = item == selected
                // Button for each Bottom Navigation Item
                Button(
                    onClick = {
                        when (item) {
                            "Home" -> onHomeClick()
                            "Devices" -> onDevicesClick()
                            "Names" -> onNamesClick()
                            "About" -> onAboutClick()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 1.5.dp), // Adds spacing between buttons
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isSelected) Color(0xFFD2B48D) else Color.White
                    ),
                    elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
                ) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}