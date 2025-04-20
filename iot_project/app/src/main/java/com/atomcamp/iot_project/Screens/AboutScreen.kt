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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
            // Adding alpha modifier to make the image more vibrant and clear
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
                    val annotatedString = buildAnnotatedString {
                        val bodyStyle = SpanStyle(fontSize = 21.5.sp)
                        val contactStyle = SpanStyle(fontSize = 15.5.sp)
                        val linkTag = "URL"
                        val whatsappUrl = "https://wa.me/message/LMNKIANS47YRL1"

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
                        withStyle(
                            style = contactStyle + SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("https://wa.me/message/LMNKIANS47YRL1")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedString,
                        modifier = Modifier
                            // Remove fillMaxSize() since we want it to wrap content naturally
                            .verticalScroll(rememberScrollState()),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                    context.startActivity(intent)
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
