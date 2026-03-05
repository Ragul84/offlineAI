package com.edgeai.tutorlite.ui.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edgeai.tutorlite.R
import com.edgeai.tutorlite.ui.screens.camera.CameraScreen
import com.edgeai.tutorlite.ui.screens.chat.ChatScreen
import com.edgeai.tutorlite.ui.screens.dashboard.DashboardScreen
import com.edgeai.tutorlite.ui.screens.learn.LearnHubScreen
import com.edgeai.tutorlite.ui.screens.onboarding.OnboardingScreen
import com.edgeai.tutorlite.ui.screens.privacy.PrivacyProofScreen
import com.edgeai.tutorlite.ui.screens.quiz.QuizScreen
import com.edgeai.tutorlite.ui.screens.scanner.ScannerScreen
import com.edgeai.tutorlite.ui.screens.settings.SettingsScreen
import com.edgeai.tutorlite.ui.screens.share.ShareScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeAiNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var onboardingDone by remember { mutableStateOf(readOnboardingDone(context)) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(Route.Learn, Route.Chat, Route.Dashboard)
    val showTopBar = currentRoute != Route.Onboarding.value
    val showBottomBar = currentRoute in tabs.map { it.value }
    val canOpenSettings = currentRoute in tabs.map { it.value }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    actions = {
                        if (canOpenSettings) {
                            IconButton(onClick = { navController.navigate(Route.Settings.value) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.nav_settings)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { route ->
                        NavigationBarItem(
                            selected = currentRoute == route.value,
                            onClick = { navController.navigate(route.value) },
                            icon = {
                                when (route) {
                                    Route.Learn -> Icon(Icons.Default.AutoStories, contentDescription = null)
                                    Route.Chat -> Icon(Icons.Default.Chat, contentDescription = null)
                                    Route.Dashboard -> Icon(Icons.Default.Dashboard, contentDescription = null)
                                    else -> Unit
                                }
                            },
                            label = {
                                Text(
                                    when (route) {
                                        Route.Learn -> stringResource(R.string.nav_learn)
                                        Route.Chat -> stringResource(R.string.nav_chat)
                                        Route.Dashboard -> stringResource(R.string.nav_dashboard)
                                        else -> ""
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Route.Chat.value else Route.Onboarding.value
        ) {
            composable(Route.Onboarding.value) {
                OnboardingScreen(
                    onContinue = {
                        setOnboardingDone(context)
                        onboardingDone = true
                        navController.navigate(Route.Chat.value) {
                            popUpTo(Route.Onboarding.value) { inclusive = true }
                        }
                    }
                )
            }
            composable(Route.Learn.value) {
                LearnHubScreen(paddingValues = paddingValues, onNavigate = { navController.navigate(it.value) })
            }
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

private fun readOnboardingDone(context: Context): Boolean =
    context.getSharedPreferences("edgeai_prefs", Context.MODE_PRIVATE)
        .getBoolean("onboarding_done", false)

private fun setOnboardingDone(context: Context) {
    context.getSharedPreferences("edgeai_prefs", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("onboarding_done", true)
        .apply()
}
