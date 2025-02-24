package com.example.visionary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
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

@Composable
fun InformationScreen(
    navController: NavController,
    savedImageId: String?
) {
    val viewModel: PhotoViewModel = viewModel()
    val savedImages by viewModel.savedImages.collectAsState()
    val photoDateTime by viewModel.photoDateTime.collectAsState()

    // ✅ Trouver l'image sélectionnée
    val selectedImage = savedImages.find { it.id == savedImageId } ?: savedImages.lastOrNull()

    // ✅ Ajout du scroll pour la page entière
    val scrollState = rememberScrollState()

    // ✅ État de chargement de l'image et du texte
    val isImageLoading = selectedImage?.bitmap == null
    var isTextLoading by remember { mutableStateOf(true) }
    var extractedText by remember { mutableStateOf("") }
    var extractedEntities by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var remainingText by remember { mutableStateOf("") }
    var showRemainingText by remember { mutableStateOf(false) }



    // Extraire le texte à chaque affichage
    LaunchedEffect(selectedImage) {
        if (selectedImage != null) {
            isTextLoading = true
            viewModel.extractTextFromImage(selectedImage.bitmap) { text ->
                extractedText = text
                isTextLoading = false

                // ✅ EXTRACTION DES ENTITÉS ET DU TEXTE NON CLASSÉ
                viewModel.extractEntitiesFromText(text) { entities, nonEntityText ->
                    extractedEntities = entities
                    remainingText = nonEntityText
                }
            }
        }
        selectedImage?.let {
            viewModel.loadPhotoDateTime(it.id) // 📥 Extraire la date et l’heure
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

            // ✅ Loader pour l'image
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
            // Bouton de suppression
            if (selectedImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // ✅ Remplit toute la largeur
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd // ✅ Alignement à droite
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
                            navController.popBackStack("save", false) // Retourner à SaveScreen
                        },
                        modifier = Modifier
                            .size(48.dp) // ✅ Assurer une taille fixe
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp) // ✅ Taille du fond
                                .background(MaterialTheme.colorScheme.error, shape = CircleShape), // ✅ Fond coloré arrondi
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


            // ✅ Divider
            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Affichage du texte ou d'un loader
            if (isTextLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // ✅ Affichage des entités avec style
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
                            containerColor = Color.Transparent, // ✅ Fond transparent
                            contentColor = MaterialTheme.colorScheme.primary // ✅ Couleur du texte
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp) // ✅ Supprime l'ombre du bouton
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showRemainingText) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Icône Voir plus / Voir moins",
                                tint = MaterialTheme.colorScheme.primary // ✅ Assure la bonne visibilité
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // ✅ Espace entre l'icône et le texte
                            Text(
                                text = if (showRemainingText) "Voir moins" else "Voir plus",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // ✅ Afficher le texte brut seulement si l'utilisateur a cliqué sur "Voir plus"
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

        // ✅ Bouton Retour
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
