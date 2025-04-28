package com.atomcamp.iot_project.Screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.atomcamp.iot_project.R

@Composable
fun AboutScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onNavigateToNames: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with enhanced contrast
        Image(
            painter = painterResource(id = R.drawable.app_background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f  // Full opacity for maximum clarity
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(
                    selected = "About",
                    onHomeClick = onNavigateToHome,
                    onDevicesClick = onNavigateToDevices,
                    onNamesClick = onNavigateToNames,
                    onAboutClick = {}
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            // State to track if the link is being pressed
            var isWhatsAppLinkPressed by remember { mutableStateOf(false) }

            // State to control showing the WhatsApp type selection dialog
            var showWhatsAppDialog by remember { mutableStateOf(false) }

            // If dialog is shown, display it
            if (showWhatsAppDialog) {
                AlertDialog(
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "Select WhatsApp Type",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Would you like to contact us via Business or Personal WhatsApp?",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onDismissRequest = {
                        showWhatsAppDialog = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showWhatsAppDialog = false
                                // Business WhatsApp URL (using the provided link)
                                val businessWhatsAppUrl = "https://wa.me/message/LMNKIANS47YRL1"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(businessWhatsAppUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366) // WhatsApp green color
                            ),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Business WhatsApp")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showWhatsAppDialog = false
                                // Personal WhatsApp URL (example number format)
                                val personalWhatsAppUrl = "https://wa.me/923487045240" // Using the number from your app
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(personalWhatsAppUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366) // Darker WhatsApp green
                            ),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Standard WhatsApp")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
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

                Text(
                    text = "About the App",
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

                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(2.dp))
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)
                ) {
                    val bullet = "•"
                    val bulletGap = "  "
                    val whatsappUrl = "https://wa.me/message/LMNKIANS47YRL1"

                    val annotatedString = buildAnnotatedString {
                        val bodyStyle = SpanStyle(fontSize = 21.5.sp)
                        val contactStyle = SpanStyle(fontSize = 15.5.sp)
                        val linkTag = "URL"

                        withStyle(style = bodyStyle + SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("HomeClick")
                        }
                        withStyle(style = bodyStyle) {
                            append(" is a user-friendly mobile app that enables wireless control of home appliances. Key features include:\n\n")
                        }

                        val bulletItems = listOf(
                            "Customizable appliances names",
                            "Touchscreen control of appliances",
                            "Manual or automatic Bluetooth connection",
                            "Background control for continuous operation\n"
                        )

                        bulletItems.forEach { item ->
                            withStyle(style = bodyStyle) {
                                append("$bullet$bulletGap$item\n")
                            }
                        }

                        withStyle(style = bodyStyle) {
                            append("Experience convenient and efficient home automation with ")
                        }

                        withStyle(style = bodyStyle + SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("HomeClick ")
                        }

                        withStyle(style = bodyStyle) {
                            append("App\n\n")
                        }

                        withStyle(style = contactStyle + SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("Contact us:\n")
                        }

                        withStyle(style = contactStyle) {
                            append("+92-348-7045240\n")
                        }

                        pushStringAnnotation(tag = linkTag, annotation = whatsappUrl)

                        // Apply different styling based on whether the link is being pressed
                        val linkStyle = if (isWhatsAppLinkPressed) {
                            contactStyle + SpanStyle(
                                color = Color.Blue,
                                fontWeight = FontWeight.ExtraBold,  // Make it extra bold when pressed
                                textDecoration = TextDecoration.Underline,
                                background = Color.LightGray  // Add background highlight when pressed
                            )
                        } else {
                            contactStyle + SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        }

                        withStyle(style = linkStyle) {
                            append("https://wa.me/message/LMNKIANS47YRL1")  // Changed text to be more descriptive
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedString,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState()),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    // When clicked, set the pressed state to true
                                    isWhatsAppLinkPressed = true

                                    // Launch a coroutine to handle the delay and dialog showing
                                    coroutineScope.launch {
                                        // Small delay to show the visual feedback (300ms is usually good for feedback)
                                        delay(300)

                                        // Reset the pressed state
                                        isWhatsAppLinkPressed = false

                                        // Show the WhatsApp type selection dialog instead of directly launching the URL
                                        showWhatsAppDialog = true
                                    }
                                }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen()
}