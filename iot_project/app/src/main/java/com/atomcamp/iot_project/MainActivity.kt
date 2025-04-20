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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Iot_projectTheme {
                val navController = rememberNavController()

                // Override the back button behavior to close the app
                // Adding this at the activity level ensures it applies to all screens
                val backPressedCallback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // This will finish the app when the back button is pressed from any screen
                        finish()
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
                    startDestination = "splash" // Change start destination to splash
                ) {
                    composable(
                        "splash",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        SplashScreen()

                        // Automatically navigate to home screen after delay
                        LaunchedEffect(key1 = true) {
                            delay(2000) // 2 seconds delay, adjust as needed
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }

                    composable(
                        "home",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        HomeScreen(
                            onNavigateToDevices = {
                                // Navigate without saving state
                                navController.navigate("devices") {
                                    // Clear the entire back stack when navigating
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToNames = {
                                navController.navigate("names") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToAbout = {
                                navController.navigate("about") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }
                    composable(
                        "devices",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        DevicesScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToNames = {
                                navController.navigate("names") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToAbout = {
                                navController.navigate("about") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }
                    composable(
                        "names",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        NamesScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToDevices = {
                                navController.navigate("devices") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToAbout = {
                                navController.navigate("about") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }
                    composable(
                        "about",
                        enterTransition = fadeEnterTransition,
                        exitTransition = fadeExitTransition
                    ) {
                        AboutScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToDevices = {
                                navController.navigate("devices") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onNavigateToNames = {
                                navController.navigate("names") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}