package com.example.visionary.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visionary.Camera.CameraPreview
import com.example.visionary.MainActivity
import com.example.visionary.R
import com.example.visionary.models.PhotoViewModel

@Composable
fun HomeScreen() {
    // Composable function to display the "Main screen"
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    // Create and remember the Camera controller
    val imageCaptureController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }

    // Create the ViewModel
    val viewModel = viewModel<PhotoViewModel>()

    val bitmaps by viewModel.bitmaps.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // CAlling Composable "CameraPreview" to display the camera preview in the app
        CameraPreview(
            controller = imageCaptureController,
            modifier = Modifier.fillMaxSize() // Remplir l'écran
        )


        LazyColumn {
            items(bitmaps) { bitmap ->
                var extractedText by remember { mutableStateOf("") }
                LaunchedEffect(bitmaps) {
                    viewModel.extractTextFromImage(bitmap) { text ->
                        extractedText = text
                    }
                }
                Text(text = extractedText,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                    )
            }
        }

        FloatingActionButton(

            onClick = {
                // Code pour l'action du bouton
                takePhoto(
                    context = context,
                    imageCaptureController,
                    onPhotoTaken = viewModel::onPhotoTaken,
                )
                vibratePhoto(context)
                //vibrateCancel(context)

            },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligner le bouton en bas à droite
                .width(200.dp) // Largeur du bouton
                .height(56.dp) // Hauteur du bouton
                .offset(y = (-100).dp)
                .padding(bottom = 16.dp)
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.extraLarge
                ), // Marge du bouton
            containerColor = MaterialTheme.colorScheme.background, // Fond du bouton
            contentColor = MaterialTheme.colorScheme.onBackground, // Couleur du texte et de l'icône
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.radio_button_checked_32),
                    contentDescription = "Take a photo", // Accesibilité pour l'icône
                    tint = MaterialTheme.colorScheme.onBackground // Icône en onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Take a photo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground // Texte en onBackground
                )
            }
        }
    }
}

// Function to take a photo using the camera controller
private fun takePhoto(
    context: Context,
    controller : LifecycleCameraController,     // The camera controller
    onPhotoTaken : (Bitmap) -> Unit             // Callback to be called when the photo is successfully taken
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
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

// Function test vibrate
fun vibrate(context: Context){
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100), -1))
}

// Function to vibrate the phone (Haptic feedback)
fun vibratePhoto(context: Context){
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
}