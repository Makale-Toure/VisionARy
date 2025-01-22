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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

data class SavedImage (
    val id: String, // Identifiant unique
    val bitmap: Bitmap
)

class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val _savedImages = MutableStateFlow<List<SavedImage>>(emptyList())
    val savedImages: StateFlow<List<SavedImage>> = _savedImages.asStateFlow()

    private val _detectedText = MutableStateFlow("")
    val detectedText = _detectedText.asStateFlow()

    init {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            _savedImages.value = loadSavedImages(context)
        }
    }

    fun reloadSavedImages(context: Context) {
        viewModelScope.launch {
            _savedImages.value = loadSavedImages(context)
        }
    }

    fun onPhotoTaken(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            if (bitmap.width > 0 && bitmap.height > 0) {
                val filename = "photo_${System.currentTimeMillis()}.jpg"
                val file = saveBitmapToAppStorage(context, bitmap, filename)
                if (file != null) {
                    val newImage = SavedImage(
                        id = file.nameWithoutExtension, // Générer un identifiant unique
                        bitmap = bitmap
                    )
                    _savedImages.value += newImage
                } else {
                    Log.e("PhotoViewModel", "Failed to save bitmap")
                }
            } else {
                Log.e("PhotoViewModel", "Invalid bitmap")
            }
        }
    }

    // Function to extract text from a bitmap using Google ML text recognition
    fun extractTextFromImage(bitmap: Bitmap, onTextExtracted: (String) -> Unit) {
        // Create an InputImage object from the bitmap
        val image = InputImage.fromBitmap(bitmap, 0)
        // Initialize the text recognizer
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // Process the image
        recognizer.process(image)
            // On successful text recognition
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text

                // Log the extracted text
                Log.d("text", "Extracted text: $extractedText")
                // Return the extracted text
                // onTextExtracted(extractedText)

                // Call the entity extraction function
                extractEntitiesFromText(extractedText) { entities ->
                    // Log the extracted entities
                    Log.d("entities", "Extracted entities: $entities")
                    // Return the extracted entities
                    onTextExtracted(entities)
                }


            }
            // On failure
            .addOnFailureListener { e ->
                Log.e("text", "Text recognition failed: $e")
                onTextExtracted("")
            }
    }


    // Function to extract entities from a text using Google ML entity extraction
    fun extractEntitiesFromText(text: String, onEntitiesExtracted: (String) -> Unit) {
        val entityExtractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.FRENCH).build()
        )

        entityExtractor.downloadModelIfNeeded()
            .addOnSuccessListener {
                val params = EntityExtractionParams.Builder(text).build()

                entityExtractor.annotate(params)
                    .addOnSuccessListener { annotations: List<EntityAnnotation> ->
                        if (annotations.isEmpty()) {
                            Log.d("EntityExtraction", "Aucune entité détectée.")
                            onEntitiesExtracted(text)
                            return@addOnSuccessListener
                        }

                        val formattedEntities = formatEntities(annotations)
                        val remainingText = extractRemainingText(text, annotations)

                        val finalOutput = formattedEntities + "\n\n" + remainingText
                        onEntitiesExtracted(finalOutput)
                    }
                    .addOnFailureListener { e ->
                        Log.e("EntityExtraction", "Échec de l'annotation", e)
                        onEntitiesExtracted(text)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("EntityExtraction", "Échec du téléchargement du modèle", e)
                onEntitiesExtracted(text)
            }
    }

    fun formatEntities(annotations: List<EntityAnnotation>): String {
        val stringBuilder = StringBuilder()

        for (annotation in annotations) {
            val annotatedText = annotation.annotatedText
            for (entity in annotation.entities) {
                val entityName = getEntityName(entity.type)
                stringBuilder.append("${entityName}: **$annotatedText**\n")
            }
        }

        return stringBuilder.toString().trim()
    }

    fun extractRemainingText(text: String, annotations: List<EntityAnnotation>): String {
        val stringBuilder = StringBuilder()
        var currentIndex = 0

        for (annotation in annotations) {
            val start = annotation.start
            val end = annotation.end

            if (currentIndex < start) {
                stringBuilder.append(text.substring(currentIndex, start))
            }

            currentIndex = end
        }

        if (currentIndex < text.length) {
            stringBuilder.append(text.substring(currentIndex))
        }

        return stringBuilder.toString().trim()
    }

    fun getEntityName(entityType: Int): String {
        return when (entityType) {
            Entity.TYPE_ADDRESS -> "Address"
            Entity.TYPE_DATE_TIME -> "Date and time"
            Entity.TYPE_EMAIL -> "Email"
            Entity.TYPE_FLIGHT_NUMBER -> "Flight number"
            Entity.TYPE_IBAN -> "IBAN"
            Entity.TYPE_PHONE -> "Phone number"
            Entity.TYPE_URL -> "URL"
            else -> "Other informations"
        }
    }

    private fun saveBitmapToAppStorage(context: Context, bitmap: Bitmap, filename: String): File? {
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
            e.printStackTrace()
            null
        }
    }

    private fun loadSavedImages(context: Context): List<SavedImage> {
        val imageFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "VisionARyImages")
        return if (imageFolder.exists() && imageFolder.isDirectory) {
            imageFolder.listFiles()?.mapNotNull { file ->
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    SavedImage(
                        id = file.nameWithoutExtension, // Utiliser le nom du fichier comme identifiant
                        bitmap = bitmap
                    )
                } else {
                    null
                }
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteImage(context: Context, imageId: String) {
        viewModelScope.launch {
            val imageFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "VisionARyImages")
            if (imageFolder.exists() && imageFolder.isDirectory) {
                val fileToDelete = imageFolder.listFiles()?.find { file ->
                    file.nameWithoutExtension == imageId
                }

                if (fileToDelete != null && fileToDelete.delete()) {
                    // Mettre à jour la liste des images
                    _savedImages.value = _savedImages.value.filter { it.id != imageId }
                    Log.d("PhotoViewModel", "Image deleted: ${fileToDelete.name}")
                } else {
                    Log.e("PhotoViewModel", "Failed to delete image")
                }
            }
        }
    }

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

        // Appliquer une rotation si nécessaire
        val matrix = Matrix().apply {
            postRotate(this@toBitmap.imageInfo.rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun analyzeImage(imageProxy: ImageProxy, onEntityDetected: (String?) -> Unit) {
        val bitmap = imageProxy.toBitmap() // Convertir ImageProxy en Bitmap
        extractTextFromImage(bitmap) { extractedText ->
            // Si aucun texte n'est détecté
            if (extractedText.isBlank()) {
                _detectedText.value = "No text detected."
            } else {
                // Extraire les entités du texte détecté
                extractEntitiesFromText(extractedText) { classifiedText ->
                    // Mettre à jour le texte détecté avec les entités classées
                    _detectedText.value = classifiedText
                }
            }
        }
        imageProxy.close() // Fermer l'ImageProxy après utilisation
    }

    fun clearDetectedText() {
        _detectedText.value = "" // Réinitialiser le texte
    }

}