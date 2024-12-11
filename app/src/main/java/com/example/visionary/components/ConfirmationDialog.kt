package com.example.visionary.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfirmationDialog(
    bitmap: Bitmap,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Photo Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                )
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(onClick = onConfirm) {
                        Text("Confirmer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onCancel) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}
