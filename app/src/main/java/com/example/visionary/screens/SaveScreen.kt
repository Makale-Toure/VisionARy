package com.example.visionary.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visionary.models.PhotoViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


@Composable
fun SaveScreen(
    modifier: Modifier = Modifier,
    onDeleteImage: (String) -> Unit,
    navController: NavController
) {
    val viewModel: PhotoViewModel = viewModel()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val navbarHeight = screenHeight * 0.1f
    val context = LocalContext.current

    LaunchedEffect(navController) { viewModel.refreshImages() }

    val savedImages by viewModel.savedImages.collectAsState()


    suspend fun saveBitmapToCache(bitmap: Bitmap, id: String): Uri? {
        return withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "saved_image_$id.jpg")

            try {
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
                }
                Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    if (savedImages.isEmpty()) {
        Box(
            modifier = modifier.offset(y = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("There are no photos yet", modifier = Modifier.padding(16.dp))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Conteneur de texte et des images
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(1f)
            ) {
                // Titre avec zIndex pour le placer au-dessus des images
                Text(
                    "Saves",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                )

                Divider(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 3.dp
                )
            }

            // Liste d'images avec un espace supérieur pour le texte
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                userScrollEnabled = true, // Assure que le scroll est bien actif
                contentPadding = PaddingValues(
                    top = (64 + 24).dp, // Espacement supérieur pour le texte
                    bottom = 16.dp + navbarHeight,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = modifier
            ) {
                items(savedImages) { savedImage ->
                    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
                    var isLoading by remember { mutableStateOf(true) }

                    // Charger l'image en arrière-plan
                    LaunchedEffect(savedImage.id) {
                        imageUri = saveBitmapToCache(savedImage.bitmap, savedImage.id)
                        isLoading = false
                    }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigate("information/${savedImage.id}") }
                    ) {
                        if (isLoading) {
                            // Placeholder de chargement
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            imageUri?.let { uri ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(uri)
                                        .crossfade(true)
                                        .size(200) // Charge une image optimisée
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}