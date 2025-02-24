package com.example.visionary.models

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SavedImage (
    val id: String, // Identifiant unique
    val bitmap: Bitmap, // Image bitmap
    val extractedText: String
)

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    // List of saved images
    private val _savedImages = MutableStateFlow<List<SavedImage>>(emptyList())
    val savedImages: StateFlow<List<SavedImage>> = _savedImages.asStateFlow()

    // Detected text from an image
    private val _detectedText = MutableStateFlow("")
    val detectedText: StateFlow<String> = _detectedText.asStateFlow()

    private val _photoDateTime = MutableStateFlow<Pair<String, String>?>(null)
    val photoDateTime: StateFlow<Pair<String, String>?> = _photoDateTime.asStateFlow()


    // Initialisation
    init {
        refreshImages()
    }

    fun loadPhotoDateTime(filename: String) {
        _photoDateTime.value = extractDateTimeFromFilename(filename)
    }


    fun extractDateTimeFromFilename(filename: String): Pair<String, String>? {
        val regex = Regex("""(\d{13})""") // ✅ Détecte un timestamp UNIX à 13 chiffres
        val matchResult = regex.find(filename)

        return matchResult?.let {
            val timestamp = it.value.toLongOrNull() ?: return null
            val date = Date(timestamp)
            val formatDate = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) // ✅ Format sans année ni secondes
            val formattedDate = formatDate.format(date)

            val parts = formattedDate.split(" ")
            if (parts.size == 2) {
                return Pair(parts[0], parts[1]) // 📆 Date et ⏰ Heure séparées
            }
            null
        }
    }

    fun refreshImages() {
        viewModelScope.launch {
            _savedImages.value = loadSavedImagesFromStorage(getApplication()) // ✅ Recharge toutes les images depuis le stockage
        }
    }


    // Add an image to the saved images list
    // This function will add a new SavedImage object to the savedImages list
    fun addSavedImage(savedImage: SavedImage) {
        _savedImages.value += savedImage
    }

    // Delete a saved image by ID
    // This function will remove a saved image from the savedImages list
    fun deleteSavedImage(imageId: String) {
        _savedImages.value = _savedImages.value.filter { it.id != imageId }
    }

    // Callback for when a photo is taken
    // This function will save the photo to storage and extract text from it
    fun onPhotoTaken(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            if (bitmap.width > 0 && bitmap.height > 0) {
                val filename = "${System.currentTimeMillis()}.jpg"
                val file = saveBitmapToStorage(context, bitmap, filename)
                if (file != null) {
                    // Extraction du texte de l'image après capture
                    extractTextFromImage(bitmap) { extractedText ->
                        val savedImage = SavedImage(
                            id = file.nameWithoutExtension,
                            bitmap = bitmap,
                            extractedText = extractedText
                        )
                        addSavedImage(savedImage)
                    }

                } else {
                    Log.e("PhotoViewModel", "Failed to save image")
                }
            } else {
                Log.e("PhotoViewModel", "Invalid bitmap")
            }
        }
    }


    // Google ML Kit Text Recognition
    // Extract text from an image and return it
    fun extractTextFromImage(bitmap: Bitmap, onTextExtracted: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                onTextExtracted(extractedText)
            }
            .addOnFailureListener { e ->
                Log.e("TextRecognition", "Text recognition failed: $e")
                onTextExtracted("")
            }
    }

    // Save a bitmap to storage
    // Returns the file where the bitmap was saved
    private fun saveBitmapToStorage(context: Context, bitmap: Bitmap, filename: String): File? {
        val imageFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "VisionARyImages")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }
        val file = File(imageFolder, filename)
        return try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            file
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Failed to save image: ${e.localizedMessage}")
            null
        }
    }

    // Load saved images from storage
    // Returns a list of SavedImage objects
    fun loadSavedImagesFromStorage(context: Context): List<SavedImage> {
        val imageFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "VisionARyImages")

        if (imageFolder.exists() && imageFolder.isDirectory) {
            return imageFolder.listFiles()?.mapNotNull { file ->
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let { SavedImage(id = file.nameWithoutExtension, bitmap = it, extractedText = "") }
            } ?: emptyList()
        }

        return emptyList()
    }

    // Extraire les entités du texte
    fun extractEntitiesFromText(text: String, onEntitiesExtracted: (List<Pair<String, String>>, String) -> Unit) {
        val entityExtractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.FRENCH).build()
        )

        entityExtractor.downloadModelIfNeeded()
            .addOnSuccessListener {
                val params = EntityExtractionParams.Builder(text).build()
                entityExtractor.annotate(params)
                    .addOnSuccessListener { annotations: List<EntityAnnotation> ->
                        val formattedEntities = mutableListOf<Pair<String, String>>()
                        val entityTextRanges = mutableListOf<IntRange>()

                        annotations.forEach { annotation ->
                            val entityType = annotation.entities.firstOrNull()?.type ?: -1
                            val entityLabel = getEntityName(entityType)
                            val entityValue = annotation.annotatedText

                            if (entityLabel != "Autre") {
                                formattedEntities.add(Pair(entityLabel, entityValue))
                                entityTextRanges.add(annotation.start until annotation.end)
                            }
                        }

                        // ✅ EXTRAIRE LE TEXTE QUI NE FAIT PAS PARTIE DES ENTITÉS
                        val remainingText = buildString {
                            var currentIndex = 0
                            for (range in entityTextRanges) {
                                if (currentIndex < range.first) {
                                    append(text.substring(currentIndex, range.first) + " ")
                                }
                                currentIndex = range.last
                            }
                            if (currentIndex < text.length) {
                                append(text.substring(currentIndex))
                            }
                        }.trim()

                        onEntitiesExtracted(formattedEntities, remainingText)
                    }
                    .addOnFailureListener { e ->
                        Log.e("EntityExtraction", "Entity annotation failed: $e")
                        onEntitiesExtracted(emptyList(), text) // Retourner tout le texte en cas d'erreur
                    }
            }
            .addOnFailureListener { e ->
                Log.e("EntityExtraction", "Failed to download model: $e")
                onEntitiesExtracted(emptyList(), text) // Retourner tout le texte en cas d'erreur
            }
    }


    // Formater les entités extraites
    private fun formatEntities(annotations: List<EntityAnnotation>): String {
        return annotations.joinToString("\n") { annotation ->
            val entityName = getEntityName(annotation.entities.firstOrNull()?.type ?: -1)
            val annotatedText = annotation.annotatedText
            "$entityName: $annotatedText"
        }
    }

    // Extraire le texte restant après l'annotation des entités
    private fun extractRemainingText(text: String, annotations: List<EntityAnnotation>): String {
        val stringBuilder = StringBuilder()
        var currentIndex = 0

        annotations.forEach { annotation ->
            if (currentIndex < annotation.start) {
                stringBuilder.append(text.substring(currentIndex, annotation.start))
            }
            currentIndex = annotation.end
        }

        if (currentIndex < text.length) {
            stringBuilder.append(text.substring(currentIndex))
        }

        return stringBuilder.toString().trim()
    }

    // Obtenir le nom de l'entité en fonction de son type
    private fun getEntityName(entityType: Int): String {
        return when (entityType) {
            Entity.TYPE_ADDRESS -> "Address"
            Entity.TYPE_DATE_TIME -> "Date and time"
            Entity.TYPE_EMAIL -> "Email"
            Entity.TYPE_FLIGHT_NUMBER -> "Flight number"
            Entity.TYPE_IBAN -> "IBAN"
            Entity.TYPE_PHONE -> "Phone number"
            Entity.TYPE_URL -> "URL"
            else -> "Other"
        }
    }

    // Supprimer une image par ID
    fun deleteImage(context: Context, imageId: String) {
        viewModelScope.launch {
            val imageFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "VisionARyImages")
            val fileToDelete = imageFolder.listFiles()?.find { it.nameWithoutExtension == imageId }

            if (fileToDelete?.delete() == true) {
                // ✅ Supprime l'image de la liste immédiatement
                _savedImages.value = _savedImages.value.filter { it.id != imageId }
                Log.d("PhotoViewModel", "Image supprimée : ${fileToDelete.name}")
            } else {
                Log.e("PhotoViewModel", "Échec de la suppression de l'image")
            }
        }
    }


    // Réinitialiser le texte détecté
    fun clearDetectedText() {
        _detectedText.value = ""
    }

    // Conversion de ImageProxy en Bitmap (pour les images de la caméra)
    @OptIn(ExperimentalGetImage::class)
    fun ImageProxy.toBitmap(): Bitmap {
        val image = this.image ?: throw IllegalStateException("Image is null")
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 100, out)
        val imageBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val matrix = Matrix().apply { postRotate(this@toBitmap.imageInfo.rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Analyser une image depuis un ImageProxy (comme pour la caméra)
    fun analyzeImage(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        extractTextFromImage(bitmap) { extractedText ->
            if (extractedText.isBlank()) {
                _detectedText.value = "No text detected."
            } else {
                //extractEntitiesFromText(extractedText) { classifiedText ->
                //    _detectedText.value = classifiedText
                //}
            }
        }
        imageProxy.close()
    }
}
