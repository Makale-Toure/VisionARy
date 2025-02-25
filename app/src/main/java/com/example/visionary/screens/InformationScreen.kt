package com.example.visionary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visionary.models.PhotoViewModel

// Information Screen to display the extracted information from the image
@Composable
fun InformationScreen(
    navController: NavController,
    savedImageId: String?
) {
    // Get the view model
    val viewModel: PhotoViewModel = viewModel()
    val savedImages by viewModel.savedImages.collectAsState()
    val photoDateTime by viewModel.photoDateTime.collectAsState()

    // Get the selected image from the saved images list if the saved image ID is provided
    val selectedImage = savedImages.find { it.id == savedImageId } ?: savedImages.lastOrNull()

    // Scroll state for the column
    val scrollState = rememberScrollState()

    // States for loading
    val isImageLoading = selectedImage?.bitmap == null
    var isTextLoading by remember { mutableStateOf(true) }
    var extractedText by remember { mutableStateOf("") }
    var extractedEntities by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var remainingText by remember { mutableStateOf("") }
    var showRemainingText by remember { mutableStateOf(false) }



    // Extract text from the image when the screen is launched
    LaunchedEffect(selectedImage) {
        if (selectedImage != null) {
            isTextLoading = true
            viewModel.extractTextFromImage(selectedImage.bitmap) { text ->
                extractedText = text
                isTextLoading = false

                // Extract entities from the text
                viewModel.extractEntitiesFromText(text) { entities, nonEntityText ->
                    extractedEntities = entities
                    remainingText = nonEntityText
                }
            }
        }
        selectedImage?.let {
            viewModel.loadPhotoDateTime(it.id) // Load the photo date and time to display
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                "Informations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 56.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Loader or image preview
            if (isImageLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                selectedImage?.bitmap?.let { bitmap ->
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Image Preview", modifier = Modifier.fillMaxWidth())
                }
            }
            // Date and time of the photo
            if (selectedImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    if (photoDateTime != null) {
                        val (date, time) = photoDateTime!!

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📅 $date",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "⏰ $time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteImage(navController.context, selectedImage.id)
                            navController.popBackStack("save", false) // Navigate back to the Save screen
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.error, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Supprimer l'image",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }


            // Divider
            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Loader or extracted text
            if (isTextLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Display the extracted text
                if (extractedEntities.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        )
                    ) {

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Informations",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            extractedEntities.forEach { (label, value) ->
                                Row {
                                    Text(
                                        text = "$label : ",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
                if (remainingText.isNotEmpty()) {
                    Button(
                        onClick = { showRemainingText = !showRemainingText },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showRemainingText) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Icône Voir plus / Voir moins",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showRemainingText) "See more" else "See less",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Display the remaining text if the user wants to see more
                    if (showRemainingText) {
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

            }

        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Fermer")
        }
    }

}
