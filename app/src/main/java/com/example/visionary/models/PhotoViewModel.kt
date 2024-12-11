package com.example.visionary.models

import android.graphics.Bitmap
import android.util.Log
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoViewModel : ViewModel() {
    // Variable to save a list of captured bitmaps
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()
    // Function to add the bitmap to the list of bitmaps each time a photo is taken
    // To-Do (only when the user confirm the photo taken)
    fun onPhotoTaken(bitmap: Bitmap) {
        viewModelScope.launch {
            // Check if the bitmap has a valid width and height (Valid photo)
            if (bitmap.width > 0 && bitmap.height > 0) {
                _bitmaps.value += bitmap
            } else {
                // Log the error
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






}