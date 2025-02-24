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
    onLaunchingComplete: () -> Unit // Callback pour signaler la fin de l'écran de lancement
) {
    VisionARyTheme {
        var visible by remember { mutableStateOf(false) }
        var scale by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(100f) }
        var isClosing by remember { mutableStateOf(false) } // Suivi de l'état de fermeture

        // Animation fluide pour la fermeture
        val exitAnimation by animateFloatAsState(
            targetValue = if (isClosing) 0f else 1f,
            animationSpec = tween(durationMillis = 500), // Transition animée sur 500ms
            label = "exitAnimation"
        )

        // Animation de fondu
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "alphaAnimation"
        )

        // Animation de zoom
        val scaleAnimated by animateFloatAsState(
            targetValue = scale,
            animationSpec = tween(durationMillis = 1000),
            label = "scaleAnimation"
        )

        // Animation de déplacement vertical
        val offsetYAnimated by animateFloatAsState(
            targetValue = offsetY,
            animationSpec = tween(durationMillis = 1000),
            label = "offsetYAnimation"
        )

        // Déclenche l'animation de fermeture avec un délai avant navigation
        LaunchedEffect(isClosing) {
            if (isClosing) {
                delay(500) // Attente que l'animation se termine
                onLaunchingComplete() // Naviguer après l'animation
            }
        }

        // Fonction pour fermer l'écran proprement
        fun closeScreen() {
            isClosing = true // Déclenche l'animation de sortie
        }

        //  Démarrer l'animation d'entrée et lancer la fermeture après 3s
        LaunchedEffect(Unit) {
            delay(500) // Petit délai avant l'animation
            visible = true
            scale = 1f
            offsetY = 0f
            delay(3000) // Attente avant de quitter automatiquement
            closeScreen()
        }

        // Contenu de la page avec animation d'entrée et de sortie
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .graphicsLayer(
                    alpha = exitAnimation, // Animation fluide de fermeture
                    scaleX = exitAnimation, // Zoom-out progressif
                    scaleY = exitAnimation
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Nom de l'application avec animation de fondu
                Text(
                    text = "VisionARy",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.background.copy(alpha = alpha),
                    modifier = Modifier.padding(16.dp)
                )

                // Logo avec animation de zoom
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Logo de l'application",
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer(scaleX = scaleAnimated, scaleY = scaleAnimated)
                )

                // Slogan animé
                Text(
                    text = "Know better the world around you and make it easier",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.background.copy(alpha = alpha),
                    modifier = Modifier
                        .graphicsLayer(translationY = offsetYAnimated)
                        .padding(16.dp)
                )
            }
        }
    }
}