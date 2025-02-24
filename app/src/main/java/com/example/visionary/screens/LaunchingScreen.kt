package com.example.visionary.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionary.R
import com.example.visionary.ui.theme.VisionARyTheme
import kotlinx.coroutines.delay

@Composable
fun LaunchingScreen(
    onLaunchingComplete: () -> Unit // Callback standard (non composable)
) {
    VisionARyTheme { // Appliquer le thème VisionARy
        var visible by remember { mutableStateOf(false) }
        var scale by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(100f) }

        // Animation de fondu
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 1000), label = ""
        )

        // Animation de zoom
        val scaleAnimated by animateFloatAsState(
            targetValue = scale,
            animationSpec = tween(durationMillis = 1000), label = ""
        )

        // Animation de déplacement vertical
        val offsetYAnimated by animateFloatAsState(
            targetValue = offsetY,
            animationSpec = tween(durationMillis = 1000), label = ""
        )

        // Démarrer l'animation après un court délai
        LaunchedEffect(Unit) {
            delay(500) // Délai avant de démarrer l'animation
            visible = true
            scale = 1f
            offsetY = 0f
            delay(3000) // Délai avant de naviguer vers l'écran principal
            onLaunchingComplete() // Appel du callback standard
        }

        // Contenu de la launching page
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary), // Utiliser la couleur primaire du thème
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Nom de l'application en haut (animation de fondu)
                Text(
                    text = "VisionARy",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.background.copy(alpha = alpha), // Utiliser la couleur onPrimary du thème
                    modifier = Modifier.padding(16.dp)
                )

                // Logo au centre (animation de zoom)
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Logo de l'application",
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer(scaleX = scaleAnimated, scaleY = scaleAnimated)
                )

                // Phrase descriptive en bas (animation de déplacement vertical)
                Text(
                    text = "Votre application de vision par ordinateur",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.background.copy(alpha = alpha), // Utiliser la couleur onPrimary du thème
                    modifier = Modifier
                        .graphicsLayer(translationY = offsetYAnimated)
                        .padding(16.dp)
                )
            }
        }
    }
}