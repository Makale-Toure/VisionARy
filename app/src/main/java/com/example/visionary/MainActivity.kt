package com.example.visionary

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults.containerColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visionary.ui.theme.VisionARyTheme
import com.example.visionary.Camera.CameraPreview
import com.example.visionary.models.PhotoViewModel
import com.example.visionary.screens.NavScreen


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


        // Check if the required permissions are granted by the user
        if (!hasRequiredPermissions()) {
            // Log the info
            Log.i("CameraX", "Requesting permissions")
            // Request the permissions if they are not granted yet
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        // Display the content of the app
        setContent {
            // A surface container using the 'background' color from our theme
            VisionARyTheme {
                // Call the MainScreen composable function
                NavScreen()
            }
        }
    }

    // Function to take a photo using the camera controller
    private fun takePhoto(
        controller : LifecycleCameraController,     // The camera controller
        onPhotoTaken : (Bitmap) -> Unit             // Callback to be called when the photo is successfully taken
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {

                // Override the method to get the image and convert it to a Bitmap that can be used better in processing
                override fun onCaptureSuccess(image: ImageProxy) {
                    // Convert the image to a Bitmap
                    super.onCaptureSuccess(image)

                    // We use Matrix to rotate the image if the photo was taken with a special orientation
                    val matrix = Matrix().apply {
                        // Rotate the image by the rotation degrees
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }

                    val bitmap = Bitmap.createBitmap(
                        image.toBitmap(),       // Convert the image to a Bitmap
                        0,                      // Start x
                        0,                      // Start y
                        image.width,            // Width
                        image.height,           // Height
                        matrix,                 // Matrix
                        true                // Filter to smooth the edges of the rotated image
                    )
                    // Log the info
                    Log.i("CameraX", "Photo taken successfully")
                    // Call the callback function with the rotated bitmap
                    onPhotoTaken(bitmap)
                }

                // Override the method to log the error
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    // Log the error
                    Log.e("CameraX", "Error while taking a photo", exception)
                }
            }
        )
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

    // Request the required permissions
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

}
