package com.example.visionary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.visionary.ui.theme.VisionARyTheme
import com.example.visionary.screens.LaunchingScreen
import com.example.visionary.screens.NavScreen2


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable the edge-to-edge display for Compose
        enableEdgeToEdge()

        // Hide the status and navigation bars and display them when the user slides
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }


        // Check if the required camera permissions are granted
        if (!hasRequiredPermissions()) {
            Log.i("CameraX", "Requesting camera permissions")
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }

        // Check if the required storage permissions are granted
        if (!hasStoragePermissions()) {
            Log.i("CameraX", "Requesting storage permissions")
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 1)
        }

        // Display the content of the app
        setContent {
            VisionARyTheme {
                var showMainScreen by remember { mutableStateOf(false) }

                if (showMainScreen) {
                    NavScreen2() // Écran principal
                } else {
                    LaunchingScreen {
                        showMainScreen = true // Déclenche la navigation après l'animation
                    }
                }
            }
        }
    }

    // Check if the required permissions are granted
    // Camera permission
    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return STORAGE_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request the required permissions
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

}
