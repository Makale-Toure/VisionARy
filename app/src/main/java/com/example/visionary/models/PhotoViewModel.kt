package com.example.visionary.models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                onTextExtracted(extractedText)
            }
            // On failure
            .addOnFailureListener { e ->
                Log.e("text", "Text recognition failed: $e")
                onTextExtracted("")
            }
    }
}