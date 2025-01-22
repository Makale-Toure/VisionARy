package com.example.visionary.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavScreen2(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel = viewModel<PhotoViewModel>()
    val context = LocalContext.current
    

    Scaffold (
        modifier = modifier.fillMaxSize(),
        bottomBar = {BottomNavbar(navController)}
    ){ innerPadding ->
        NavHost(navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("home") {
                HomeScreen(
                    onNavigateToAR = {
                        navController.navigate("ar")
                    }
                )
            }

            composable("ar") {
                ARScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable("save") {
                SaveScreen(
                    savedImages = viewModel.savedImages.value,
                    modifier = Modifier.fillMaxWidth(),
                    onDeleteImage = { imageId ->
                        viewModel.deleteImage(context, imageId)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavbar(navController: NavController) {
    // List of navigation items
    val navItems = listOf(
        NavItem(label = "Home", icon = R.drawable.ic_photo, route = "home"),
        NavItem(label = "Save", icon = R.drawable.ic_save, route = "save")
    )

    val currentRoute = currentRoute(navController)
    NavigationBar (
        modifier = Modifier
            .fillMaxHeight(0.1f) // Hauteur de la barre
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            )
            .padding(top = 1.dp),
        containerColor = MaterialTheme.colorScheme.background, // Couleur de fond de la barre
        contentColor = MaterialTheme.colorScheme.onBackground // Couleur par défaut du contenu
    ) {
        navItems.forEach {navItem ->
            val isSelected = when (navItem.route) {
                "home" -> currentRoute == "home" || currentRoute == "ar" // Home est actif sur "ar" également
                "save" -> currentRoute == "save"
                else -> false
            }
            // On ajoute du séparateur entre les icônes
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .height(70.dp)
                    .width(1.dp)
            )
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
                    indicatorColor = MaterialTheme.colorScheme.secondary // Couleur de l'indicateur actif
                )
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
