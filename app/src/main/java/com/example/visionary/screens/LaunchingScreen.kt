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

// Lauching screen with animations for the app launch
@Composable
fun LaunchingScreen(
    onLaunchingComplete: () -> Unit
) {
    VisionARyTheme {
        var visible by remember { mutableStateOf(false) }
        var scale by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(100f) }
        var isClosing by remember { mutableStateOf(false) }

        // Animation de sortie
        val exitAnimation by animateFloatAsState(
            targetValue = if (isClosing) 0f else 1f,
            animationSpec = tween(durationMillis = 500),
            label = "exitAnimation"
        )

        // Fade animation
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "alphaAnimation"
        )

        // Zoom animation
        val scaleAnimated by animateFloatAsState(
            targetValue = scale,
            animationSpec = tween(durationMillis = 1000),
            label = "scaleAnimation"
        )

        // Vertical animation
        val offsetYAnimated by animateFloatAsState(
            targetValue = offsetY,
            animationSpec = tween(durationMillis = 1000),
            label = "offsetYAnimation"
        )

        // Function to close the screen properly
        LaunchedEffect(isClosing) {
            if (isClosing) {
                delay(500)
                onLaunchingComplete() // Navigate to the next screen
            }
        }

        // Function to close the screen
        fun closeScreen() {
            isClosing = true
        }

        // Start the animations
        LaunchedEffect(Unit) {
            delay(500)
            visible = true
            scale = 1f
            offsetY = 0f
            delay(3000) // Wait for 3 seconds
            closeScreen()
        }

        // Content of the launching screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .graphicsLayer(
                    alpha = exitAnimation,
                    scaleX = exitAnimation,
                    scaleY = exitAnimation
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Name of the app
                Text(
                    text = "VisionARy",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.background.copy(alpha = alpha),
                    modifier = Modifier.padding(16.dp)
                )

                // Logo with animations
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Logo de l'application",
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer(scaleX = scaleAnimated, scaleY = scaleAnimated)
                )

                // Description of the app
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