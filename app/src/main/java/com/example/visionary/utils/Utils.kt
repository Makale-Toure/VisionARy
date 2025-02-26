package com.example.visionary.utils

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode

object Utils {

    // Function to get the model for the phone
    fun getModelForPhone(): String {
        return "armodels/phone.glb"
    }

    // Function to get the model for the website
    fun getModelForWebsite(): String {
        return "armodels/url.glb"
    }

    // Function to create the AnchorNode for the phone
    fun createAnchorNodePhone(
        engine: Engine, // Filament engine for rendering
        modelLoader: ModelLoader, // Model loader to load the model
        materialLoader: MaterialLoader, // Material loader to load the material
        anchor: Anchor, // Anchor to attach the model
    ): AnchorNode {
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        try {
            // Path to the model
            val modelPath = getModelForPhone()
            Log.d("AR_DEBUG", "Chargement du modèle: $modelPath")

            // Create the model instance with 10 instances for optimization
            val modelInstance = modelLoader.createInstancedModel(modelPath, 10)

            // Create the ModelNode with the model instance and scale it to 0.2f
            val modelNode = ModelNode(
                modelInstance = modelInstance.first(),
                scaleToUnits = 0.2f
            ).apply {
                isEditable = true // Make the model editable
            }

            // Create the bounding box around the model for interaction
            val boundingBox = CubeNode(
                engine = engine,
                size = modelNode.extents,
                center = modelNode.center,
                materialInstance = materialLoader.createColorInstance(Color.White)
            ).apply {
                isVisible = false
            }

            // Attach the bounding box to the model node
            modelNode.addChildNode(boundingBox)
            // Attach the model node to the anchor node
            anchorNode.addChildNode(modelNode)

        }catch (e: Exception){
            Log.e("AR_ERROR", "Erreur lors du chargement du modèle", e)
        }
        return anchorNode
    }

    // Function to create the AnchorNode for the website - same as the phone but with a different model
    fun createAnchorNodeWebsite(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        anchor: Anchor,
    ): AnchorNode {
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        try {
            val modelPath = getModelForWebsite()
            Log.d("AR_DEBUG", "Chargement du modèle: $modelPath")

            val modelInstance = modelLoader.createInstancedModel(modelPath, 10)

            val modelNode = ModelNode(
                modelInstance = modelInstance.first(),
                scaleToUnits = 0.2f
            ).apply {
                isEditable = true
            }
            val boundingBox = CubeNode(
                engine = engine,
                size = modelNode.extents,
                center = modelNode.center,
                materialInstance = materialLoader.createColorInstance(Color.White)
            ).apply {
                isVisible = false
            }

            modelNode.addChildNode(boundingBox)
            anchorNode.addChildNode(modelNode)

        }catch (e: Exception){
            Log.e("AR_ERROR", "Erreur lors du chargement du modèle", e)
        }
        return anchorNode
    }
}