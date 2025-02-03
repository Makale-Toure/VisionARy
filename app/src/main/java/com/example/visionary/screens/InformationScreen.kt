package com.example.visionary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visionary.models.PhotoViewModel

@Composable
fun InformationScreen(
    navController: NavController,
    savedImageId: String?,
    formattedText: String?  // Le texte formaté avec les entités extraites
) {
    val viewModel = viewModel<PhotoViewModel>()
    val savedImage = viewModel.savedImages.collectAsState().value.find { it.id == savedImageId }

    // Ajout du scroll pour la page entière
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Centrer le texte "Informations"
            Text(
                "Informations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 56.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Affichage de l'aperçu de l'image
            savedImage?.bitmap?.let { bitmap ->
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Image Preview", modifier = Modifier.fillMaxWidth())
            }

            // Ajout d'un Divider en bas de l'image (couleur primaire)
            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                thickness = 1.dp
            )

            // Spacer pour éviter que le texte ne soit trop collé au divider
            Spacer(modifier = Modifier.height(8.dp))

            // Partie défilable (le texte)
            formattedText?.let {
                // Ajouter le scroll ici uniquement pour la partie texte
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Afficher le texte formaté avec entités, avec style
                    Text(formatEntitiesWithStyle(it), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Bouton Retour
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
        }
    }
}

// Fonction pour formater le texte avec les entités et leur style
@Composable
fun formatEntitiesWithStyle(text: String): AnnotatedString {
    val entities = text.split("\n")
    val formattedText = buildAnnotatedString {
        entities.forEach { entity ->
            // Vérifier si l'entité contient un champ et une valeur, et les formater
            val parts = entity.split(": ")
            if (parts.size == 2) {
                // Appliquer un style "primary" souligné pour le champ
                pushStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline))
                append(parts[0])  // Champ de l'entité (par exemple "Address")
                pop()

                // Ajouter un espace entre le champ et la valeur
                append(": ")

                // Appliquer le style pour la valeur (blanc)
                pushStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground))
                append(parts[1])  // Valeur de l'entité
                pop()
            } else {
                // Si ce n'est pas une entité, on garde le texte tel quel
                append(entity)
            }
            append("\n")
        }
    }
    return formattedText
}