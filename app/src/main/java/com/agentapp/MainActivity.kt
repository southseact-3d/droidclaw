package com.agentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.agentapp.ui.chat.ChatScreen
import com.agentapp.ui.scheduler.SchedulerScreen
import com.agentapp.ui.settings.SettingsScreen
import com.agentapp.ui.skills.SkillsScreen
import com.agentapp.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector) {
    object Chat      : Screen("chat",      "Chat",      Icons.Outlined.Chat,        Icons.Filled.Chat)
    object Skills    : Screen("skills",    "Skills",    Icons.Outlined.Extension,   Icons.Filled.Extension)
    object Scheduler : Screen("scheduler", "Schedule",  Icons.Outlined.Schedule,    Icons.Filled.Schedule)
    object Settings  : Screen("settings",  "Settings",  Icons.Outlined.Settings,    Icons.Filled.Settings)
}

val bottomNavItems = listOf(Screen.Chat, Screen.Skills, Screen.Scheduler, Screen.Settings)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgentAppTheme {
                AgentApp()
            }
        }
    }
}

@Composable
fun AgentApp() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Obsidian,
        bottomBar = {
            AgentBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route)      { ChatScreen() }
            composable(Screen.Skills.route)    { SkillsScreen() }
            composable(Screen.Scheduler.route) { SchedulerScreen() }
            composable(Screen.Settings.route)  { SettingsScreen() }
        }
    }
}

@Composable
private fun AgentBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Column {
        HorizontalDivider(color = Border, thickness = 0.5.dp)
        NavigationBar(
            containerColor = Surface1,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selected) screen.iconSelected else screen.icon,
                            contentDescription = screen.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(screen.label, style = MaterialTheme.typography.labelSmall)
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Amber,
                        selectedTextColor = Amber,
                        indicatorColor = AmberDim.copy(alpha = 0.3f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}
