package com.absapp.control.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.absapp.control.viewmodel.AppViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Main : Screen("main", "Укладка", Icons.Filled.Home)
    object History : Screen("history", "Журнал", Icons.Filled.List)
    object Objects : Screen("objects", "Объекты", Icons.Filled.LocationOn)
    object Materials : Screen("materials", "Справочник", Icons.Filled.Info)
}

private val bottomItems = listOf(Screen.Main, Screen.History, Screen.Objects, Screen.Materials)

@Composable
fun AppScaffold(
    navController: NavHostController,
    vm: AppViewModel,
    locationPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    vm = vm,
                    locationPermissionGranted = locationPermissionGranted,
                    onRequestPermission = onRequestPermission,
                    onGoToObjects = {
                        navController.navigate(Screen.Objects.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.History.route) { HistoryScreen(vm = vm) }
            composable(Screen.Objects.route) { ObjectsScreen(vm = vm) }
            composable(Screen.Materials.route) { MaterialsScreen(vm = vm) }
        }
    }
}
