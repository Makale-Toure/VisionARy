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

    fun getModelForPhone(): String {
        return "armodels/phone.glb"
    }

    fun getModelForWebsite(): String {
        return "armodels/url.glb"
    }

    fun createAnchorNodePhone(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        anchor: Anchor,
    ): AnchorNode {
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        try {
            val modelPath = getModelForPhone()
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