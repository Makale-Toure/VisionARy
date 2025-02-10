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
import androidx.compose.ui.graphics.asImageBitmap
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

    // ✅ Trouver l'image sélectionnée
    val selectedImage = savedImages.find { it.id == savedImageId } ?: savedImages.lastOrNull()

    // ✅ Ajout du scroll pour la page entière
    val scrollState = rememberScrollState()

    // ✅ État de chargement de l'image et du texte
    val isImageLoading = selectedImage?.bitmap == null
    var isTextLoading by remember { mutableStateOf(true) }
    var extractedText by remember { mutableStateOf("") }

    // ✅ Extraire le texte à chaque affichage
    LaunchedEffect(selectedImage) {
        if (selectedImage != null) {
            isTextLoading = true
            viewModel.extractTextFromImage(selectedImage.bitmap) { text ->
                extractedText = text // ✅ Mise à jour du texte extrait
                isTextLoading = false
            }
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
                Text(extractedText, style = MaterialTheme.typography.bodyMedium)
            }

            // ✅ Bouton de suppression
            if (selectedImage != null) {
                IconButton(
                    onClick = {
                        viewModel.deleteImage(navController.context, selectedImage.id)
                        navController.popBackStack("save", false) // Retourner à SaveScreen
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // ✅ Taille du fond
                        .background(MaterialTheme.colorScheme.error, shape = CircleShape), // ✅ Fond coloré arrondi
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Supprimer l'image",
                        tint = MaterialTheme.colorScheme.onBackground
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
