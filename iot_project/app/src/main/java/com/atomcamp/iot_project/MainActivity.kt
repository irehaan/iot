package com.atomcamp.iot_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.atomcamp.iot_project.ui.theme.Iot_projectTheme
import com.atomcamp.iot_project.Screens.AboutScreen
import com.atomcamp.iot_project.Screens.DevicesScreen
import com.atomcamp.iot_project.Screens.HomeScreen
import com.atomcamp.iot_project.Screens.NamesScreen
import com.atomcamp.iot_project.Screens.SplashScreen
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Iot_projectTheme {
                val navController = rememberNavController()

                // Override the back button behavior to navigate to home screen or close the app
                val backPressedCallback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (navController.currentDestination?.route != "home" &&
                            navController.currentDestination?.route != "splash") {
                            // Navigate to home and clear the entire back stack
                            navController.navigate("home") {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // If on home or splash, finish the app
                            finish()
                        }
                    }
                }
                onBackPressedDispatcher.addCallback(this, backPressedCallback)

                // Define fade transitions
                val fadeEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
                    fadeIn(animationSpec = tween(500))
                }

                val fadeExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
                    fadeOut(animationSpec = tween(500))
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable(
                        "splash",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        SplashScreen()

                        // Automatically navigate to home screen after delay
                        LaunchedEffect(key1 = true) {
                            delay(2000) // 2 seconds delay
                            navController.navigate("home") {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }

                    composable(
                        "home",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        // Using key forces recomposition
                        key(Unit) {
                            HomeScreen(
                                onNavigateToDevices = {
                                    navController.navigate("devices") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToNames = {
                                    navController.navigate("names") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    composable(
                        "devices",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        key(Unit) {
                            DevicesScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToNames = {
                                    navController.navigate("names") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    composable(
                        "names",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        key(Unit) {
                            NamesScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToDevices = {
                                    navController.navigate("devices") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    composable(
                        "about",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        key(Unit) {
                            AboutScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToDevices = {
                                    navController.navigate("devices") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToNames = {
                                    navController.navigate("names") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}