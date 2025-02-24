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
    /*val posters = mapOf(
        "Phone number" to "phone.glb"
    )

    fun getModelForPoster(poster: String): String {
        val modelName = posters[poster] ?: error("Model not found")
        return "assets/$modelName"
    }*/

    fun getModelForPoster(): String {
        return "armodels/phone.glb"
    }

    fun createAnchorNode(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        anchor: Anchor,
    ): AnchorNode {
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        try {
            val modelPath = getModelForPoster()
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

            listOf(modelNode, anchorNode).forEach {
                it.onEditingChanged = { editingTransforms ->
                    boundingBox.isVisible = editingTransforms.isNotEmpty()
                }
            }
        }catch (e: Exception){
            Log.e("AR_ERROR", "Erreur lors du chargement du modèle", e)
        }
        return anchorNode
    }
}
