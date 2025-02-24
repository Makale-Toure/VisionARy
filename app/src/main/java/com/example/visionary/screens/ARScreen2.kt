package com.example.visionary.screens

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visionary.Camera.CameraPreview
import com.example.visionary.R
import com.example.visionary.models.PhotoViewModel
import com.example.visionary.utils.Utils
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import io.github.sceneview.node.Node
import kotlinx.coroutines.delay


@Composable
fun ARScreen2(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = viewModel<PhotoViewModel>()
    var countdown by remember { mutableStateOf(10) }
    var isCountingDown by remember { mutableStateOf(false) }


    // Initialisation du moteur AR
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val planeRenderer = remember {
        mutableStateOf(true)
    }
    val modelInstance = remember {
        mutableListOf<ModelInstance>()
    }
    val trackingFailureReason = remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    val frame = remember {
        mutableStateOf<Frame?>(null)
    }

    // États pour l'analyse d'image
    var isAnalyzing by remember { mutableStateOf(false) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
                viewModel.analyzeImage(imageProxy)
            }
            cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), imageAnalyzer)

            // Analyse pendant 3 secondes
            delay(3000)
            isAnalyzing = false
            cameraController.clearImageAnalysisAnalyzer()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if(isAnalyzing){
            CameraPreview(
                controller = cameraController,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ){
                Text(
                    text = "Hold the position for few seconds",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        val detectedEntity by viewModel.detectedText.collectAsState()

        if (!isAnalyzing){
            ARScene(
                modifier = Modifier.fillMaxSize(),
                childNodes = childNodes,
                engine = engine,
                view = view,
                modelLoader = modelLoader,
                collisionSystem = collisionSystem,
                planeRenderer = planeRenderer.value,
                cameraNode = cameraNode,
                materialLoader = materialLoader,
                onTrackingFailureChanged = {
                    trackingFailureReason.value = it
                },
                onSessionUpdated = { _, updatedFrame ->
                    frame.value = updatedFrame
                },
                sessionConfiguration = { session, config ->
                    config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        true -> Config.DepthMode.AUTOMATIC
                        false -> Config.DepthMode.DISABLED
                    }
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                },

                onGestureListener = rememberOnGestureListener(
                    onSingleTapConfirmed = { e: MotionEvent, node: Node? ->
                        if(node is ModelNode) {
                            if (detectedEntity.contains("Phone number")) {
                                val phoneNumberRegex = Regex("""Phone number:\s*([\d\s]+)""")
                                // Si on clique sur le téléphone, lancer l'appel
                                val phoneNumber = phoneNumberRegex.find(detectedEntity)
                                    ?.groupValues?.get(1)
                                    ?.replace(
                                        "\\s".toRegex(),
                                        ""
                                    ) // Supprimer les espaces // numéro random: "+33612345678"
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phoneNumber")
                                }
                                context.startActivity(intent)
                            }
                        } else {
                            // Création d'un ancrage si aucun modèle n'est détecté sous le clic
                            val hitTestResult = frame.value?.hitTest(e.x, e.y)
                            hitTestResult?.firstOrNull { it.isValid(depthPoint = false, point = false) }
                                ?.createAnchorOrNull()?.let {
                                    val nodeModel = Utils.createAnchorNode(
                                        engine = engine,
                                        modelLoader = modelLoader,
                                        materialLoader = materialLoader,
                                        anchor = it,
                                    )
                                    childNodes += nodeModel
                                }
                        }
                    }
                )
            )
        }

        if (detectedEntity.isNotEmpty()) {
            Text(
                detectedEntity,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Bouton pour démarrer l'analyse
        FloatingActionButton(
            onClick = {
                if (!isAnalyzing) {
                    isAnalyzing = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(200.dp)
                .height(56.dp)
                .padding(bottom = 16.dp)
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.extraLarge
                ),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground, // Couleur du texte et de l'icône
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.play_circle_24px),
                    contentDescription = "Display AR",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Display AR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground // Texte en onBackground
                )
            }
        }

        FloatingActionButton(
            onClick = {
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