package com.atomcamp.iot_project.Screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atomcamp.iot_project.PreferenceManager
import com.atomcamp.iot_project.R
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun NamesScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    // Sample list of device names
    val defaultDeviceNames = listOf(
        "Room 1 Light",
        "Room 2 Fan",
        "Water Pump",
        "Guestroom Light",
        "Street Light",
        "Lounge LED TV",
        "Microwave Oven",
        "Lounge Fan"
    )

    // Define character limit for device names
    val nameCharacterLimit = 25

    val context = LocalContext.current
    val prefManager = remember(context) { PreferenceManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Load names from preferences when initializing
    val customizedNames = remember {
        mutableStateOf(defaultDeviceNames.withIndex().associate { (index, defaultName) ->
            defaultName to prefManager.getDeviceName(index + 1, defaultName)
        }.toMutableMap())
    }

    // Original names to revert to when canceling changes
    val originalNames = remember {
        mutableStateOf(customizedNames.value.toMap())
    }

    // State for dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedDeviceName by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(0) }
    var newName by remember { mutableStateOf("") }
    var isLimitReached by remember { mutableStateOf(false) }

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

        // Content overlay
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(
                    selected = "Names",
                    onHomeClick = onNavigateToHome,
                    onDevicesClick = onNavigateToDevices,
                    onNamesClick = {},
                    onAboutClick = onNavigateToAbout
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Top blue header - semi-transparent
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Touch a name to",
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
                            text = "rename it",
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
                }

                // Middle gray area with device names - SOLID color

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-30).dp)
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp))
                        .padding(vertical = 2.dp)
                ) {
                    LazyColumn {
                        items(defaultDeviceNames.withIndex().toList()) { (index, defaultName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDeviceName = defaultName
                                        selectedIndex = index
                                        newName = customizedNames.value[defaultName] ?: defaultName
                                        isLimitReached = false
                                        showDialog = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 17.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(30.dp)
                                )

                                Text(
                                    text = customizedNames.value[defaultName] ?: defaultName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bottom blue area with buttons - semi-transparent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(60.dp)
                    ) {

                    }
                }
            }
        }

        // Dialog for renaming
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = "Rename Device",
                        style = TextStyle(color = Color.White)
                    )
                },
                text = {
                    Column {
                        TextField(
                            value = newName,
                            onValueChange = { input ->
                                if (input.length <= nameCharacterLimit) {
                                    newName = input
                                    isLimitReached = false
                                } else {
                                    newName = input.take(nameCharacterLimit)
                                    isLimitReached = true
                                    // Show notification when character limit is reached
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Name Limit Reached")
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = "Enter new name",
                                    color = Color.Black
                                )
                            },
                            textStyle = TextStyle(color = Color.Black),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Black,
                                unfocusedIndicatorColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            ),
                            singleLine = true
                        )

                        if (isLimitReached) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Name Limit Reached",
                                color = Color(0xFFFF5722),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${newName.length}/$nameCharacterLimit characters",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updatedMap = customizedNames.value.toMutableMap()
                            updatedMap[selectedDeviceName] = newName
                            customizedNames.value = updatedMap

                            // Save the updated name to preferences
                            prefManager.setDeviceName(selectedIndex + 1, newName)

                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A))
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF001F4D)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNamesScreen() {
    NamesScreen()
}