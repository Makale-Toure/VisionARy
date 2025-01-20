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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visionary.R
import com.example.visionary.components.NavItem
import com.example.visionary.models.PhotoViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun NavScreen(modifier: Modifier = Modifier) {

    val viewModel = viewModel<PhotoViewModel>()

    // List of navigation items
    val navItemList = listOf(
        NavItem(label = "Home", icon = R.drawable.ic_photo, route = "home"),
        NavItem(label = "Save", icon = R.drawable.ic_save, route = "save")
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                    )
                    .padding(top = 1.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    if (index > 0) {
                        // On ajoute du séparateur entre les icônes
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 16.dp)
                                .height(70.dp)
                                .width(1.dp)
                        )
                    }
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(
                                // On charge l'icône via painterResource
                                painter = painterResource(id = navItem.icon),
                                contentDescription = "Icon",
                                tint = if (selectedIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                        },
                        label = {
                            Text(
                                text = navItem.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selectedIndex == index) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (selectedIndex == index)
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
                            indicatorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex, viewModel)
    }
}