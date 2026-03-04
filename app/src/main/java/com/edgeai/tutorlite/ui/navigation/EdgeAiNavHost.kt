package com.edgeai.tutorlite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edgeai.tutorlite.R
import com.edgeai.tutorlite.ui.screens.camera.CameraScreen
import com.edgeai.tutorlite.ui.screens.chat.ChatScreen
import com.edgeai.tutorlite.ui.screens.dashboard.DashboardScreen
import com.edgeai.tutorlite.ui.screens.quiz.QuizScreen
import com.edgeai.tutorlite.ui.screens.scanner.ScannerScreen
import com.edgeai.tutorlite.ui.screens.settings.SettingsScreen
import com.edgeai.tutorlite.ui.screens.share.ShareScreen
import com.edgeai.tutorlite.ui.screens.privacy.PrivacyProofScreen

@Composable
fun EdgeAiNavHost() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val tabs = listOf(
        Route.Camera,
        Route.Scanner,
        Route.Chat,
        Route.Quiz,
        Route.Dashboard,
        Route.Share,
        Route.Settings,
        Route.Privacy
    )

    Scaffold(
        floatingActionButton = {
            if (currentRoute == Route.Chat.value) {
                FloatingActionButton(onClick = { }) {
                    Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.voice_input))
                }
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { route ->
                    NavigationBarItem(
                        selected = currentRoute == route.value,
                        onClick = { navController.navigate(route.value) },
                        icon = {
                            when (route) {
                                Route.Camera -> Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Route.Scanner -> Icon(Icons.Default.AutoStories, contentDescription = null)
                                Route.Chat -> Icon(Icons.Default.Chat, contentDescription = null)
                                Route.Quiz -> Icon(Icons.Default.AutoStories, contentDescription = null)
                                Route.Dashboard -> Icon(Icons.Default.Dashboard, contentDescription = null)
                                Route.Share -> Icon(Icons.Default.IosShare, contentDescription = null)
                                Route.Settings -> Icon(Icons.Default.Settings, contentDescription = null)
                                Route.Privacy -> Icon(Icons.Default.Security, contentDescription = null)
                            }
                        },
                        label = {
                            Text(
                                when (route) {
                                    Route.Camera -> stringResource(R.string.nav_camera)
                                    Route.Scanner -> stringResource(R.string.nav_scan)
                                    Route.Chat -> stringResource(R.string.nav_chat)
                                    Route.Quiz -> stringResource(R.string.nav_quiz)
                                    Route.Dashboard -> stringResource(R.string.nav_dashboard)
                                    Route.Share -> stringResource(R.string.nav_share)
                                    Route.Settings -> stringResource(R.string.nav_settings)
                                    Route.Privacy -> stringResource(R.string.nav_privacy)
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Chat.value
        ) {
            composable(Route.Camera.value) { CameraScreen(paddingValues) }
            composable(Route.Scanner.value) { ScannerScreen(paddingValues) }
            composable(Route.Chat.value) { ChatScreen(paddingValues) }
            composable(Route.Quiz.value) { QuizScreen(paddingValues) }
            composable(Route.Dashboard.value) { DashboardScreen(paddingValues) }
            composable(Route.Share.value) { ShareScreen(paddingValues) }
            composable(Route.Settings.value) { SettingsScreen(paddingValues) }
            composable(Route.Privacy.value) { PrivacyProofScreen(paddingValues) }
        }
    }
}
