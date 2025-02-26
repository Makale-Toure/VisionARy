package com.example.visionary.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.visionary.R
import com.example.visionary.components.NavItem
import com.example.visionary.models.PhotoViewModel

// Navigation screen to navigate between different screens
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavScreen2(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: PhotoViewModel = viewModel()
    val context = LocalContext.current

    // Main scaffold containing navigation bar and content
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        bottomBar = { BottomNavbar(navController) },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        // Navigation host to navigate between screens
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAR = {
                        navController.navigate("ar")
                    },
                    navController = navController
                )
            }

            composable("ar") {
                ARScreen2(
                    onBack = { navController.popBackStack() },
                )
            }

            composable("save") {
                SaveScreen(
                    modifier = Modifier.fillMaxWidth(),
                    onDeleteImage = { imageId ->
                        viewModel.deleteImage(context, imageId)
                    },
                    navController = navController
                )
            }

            composable("information/{savedImageId}") { backStackEntry ->
                val savedImageId = backStackEntry.arguments?.getString("savedImageId")
                InformationScreen(
                    navController = navController,
                    savedImageId = savedImageId
                )
            }
        }
    }
}

@Composable
fun BottomNavbar(navController: NavController) {

    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    // List of navigation items
    val navItems = listOf(
        NavItem(label = "Home", icon = R.drawable.ic_photo, route = "home"),
        NavItem(label = "Save", icon = R.drawable.ic_save, route = "save")
    )

    val currentRoute = currentRoute(navController)

    // Bottom navigation bar layout
    NavigationBar (
        modifier = Modifier
            .fillMaxHeight(0.1f) // Set navbar height
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f), // Start line at top left
                    end = Offset(size.width, 0f), // End line at top right
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 1.dp),
        containerColor = MaterialTheme.colorScheme.background, // Background color
        contentColor = MaterialTheme.colorScheme.onBackground // Default content color
    ) {
        navItems.forEach {navItem ->
            val isSelected = when (navItem.route) {
                "home" -> currentRoute == "home" || currentRoute == "ar" // Highlight home icon when on home or AR screen
                "save" -> currentRoute == "save"
                else -> false
            }
            // Add a separator between navigation items
            if (navItem.route != navItems.first().route) {
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(64.dp)
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != navItem.route) {
                        navController.navigate(navItem.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = navItem.label,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                },
                label = {
                    Text(
                        text = navItem.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (currentRoute == navItem.route)
                                FontWeight.ExtraBold
                            else
                                FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondary // Color of the current indicator
                )
            )
        }
    }
}

// Function to get the current route
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
