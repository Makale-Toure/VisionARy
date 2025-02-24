package com.example.visionary.screens

import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visionary.Camera.CameraPreview
import com.example.visionary.R
import com.example.visionary.models.PhotoViewModel
import kotlinx.coroutines.delay


@Composable
fun ARScreen(
    onBack: () -> Unit, // Callback pour revenir à l'écran précédent
) {
    val context = LocalContext.current

    val viewModel = viewModel<PhotoViewModel>()

    var isAnalyzing by remember { mutableStateOf(false) }

    var countdown by remember { mutableStateOf(5) }

    // Contrôleur de la caméra avec analyse d'image en temps réel
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS
            )
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            countdown = 5
            val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
                viewModel.analyzeImage(imageProxy)
            }
            cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), imageAnalyzer)

            while (countdown > 0) {
                delay(1000)
                countdown--
            }

            // Arrêter l'analyse après le compte à rebours
            isAnalyzing = false
            cameraController.clearImageAnalysisAnalyzer()
            viewModel.clearDetectedText()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            controller = cameraController,
            modifier = Modifier.fillMaxSize()
        )

        if (isAnalyzing) {
            Text(
                text = "Display AR in $countdown ...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        val detectedText by viewModel.detectedText.collectAsState()
        if (detectedText.isNotEmpty()) {
            Text(
                detectedText,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Bouton pour prendre une photo
        FloatingActionButton(
            onClick = {
                if (!isAnalyzing) {
                    isAnalyzing = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligner le bouton en bas à droite
                .width(200.dp) // Largeur du bouton
                .height(56.dp) // Hauteur du bouton
                .padding(bottom = 16.dp)
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.extraLarge
                ), // Marge du bouton
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground, // Couleur du texte et de l'icône
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.play_circle_24px),
                    contentDescription = "Display AR",
                    tint = MaterialTheme.colorScheme.onBackground // Icône en onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Display AR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground // Texte en onBackground
                )
            }
        }

        // Bouton pour revenir en arrière
        FloatingActionButton(
            onClick = {
                cameraController.unbind()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.BottomStart) // Aligner le bouton en bas à droite
                .width(60.dp) // Largeur du bouton
                .height(60.dp) // Hauteur du bouton
                .padding(bottom = 16.dp, start = 16.dp)
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.extraLarge
                ), // Marge du bouton
            containerColor = MaterialTheme.colorScheme.background, // Fond du bouton
            contentColor = MaterialTheme.colorScheme.onBackground, // Couleur du texte et de l'icône
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24px), // Icône de retour
                contentDescription = "Back"
            )
        }

    }
}
