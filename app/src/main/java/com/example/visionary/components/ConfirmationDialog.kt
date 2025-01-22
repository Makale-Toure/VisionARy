package com.example.visionary.components

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.visionary.R

@Composable
fun ConfirmationDialog(
    bitmap: Bitmap,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {

    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .width(400.dp) // Largeur réduite pour une meilleure adaptation
                .height(550.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(16.dp) // Coins arrondis
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp) // Espacement interne
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Titre du dialogue
                Text(
                    text = stringResource(id = R.string.retake_photo),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp) // Espacement sous le titre
                )

                // Aperçu de la photo
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Photo Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Hauteur ajustée
                        .clip(RoundedCornerShape(8.dp)) // Coins arrondis pour l'image
                        .background(MaterialTheme.colorScheme.background) // Fond légèrement transparent
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espacement entre l'image et les boutons

                // Boutons de confirmation et d'annulation
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Espacement horizontal
                ) {
                    // Bouton Annuler
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .weight(1f) // Répartir l'espace entre les boutons
                            .padding(end = 8.dp) // Espacement entre les boutons
                            .border(
                                width = 4.dp, // Épaisseur de la bordure
                                color = MaterialTheme.colorScheme.error, // Couleur de la bordure
                                shape = MaterialTheme.shapes.extraLarge
                            ),
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Bouton Confirmer
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .weight(1f) // Répartir l'espace entre les boutons
                            .padding(start = 8.dp) // Espacement entre les boutons
                            .border(
                                width = 4.dp, // Épaisseur de la bordure
                                color = MaterialTheme.colorScheme.outline, // Couleur de la bordure
                                shape = MaterialTheme.shapes.extraLarge
                            ),
                        shape = MaterialTheme.shapes.extraLarge // Coins arrondis pour le bouton
                    ) {
                        Text(
                            text = stringResource(id = R.string.choosing_photo),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

