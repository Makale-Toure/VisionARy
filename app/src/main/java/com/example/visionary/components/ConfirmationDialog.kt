package com.example.visionary.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.visionary.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ConfirmationDialog(
    bitmap: Bitmap,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .height(580.dp)
                .fillMaxWidth()
                .padding(4.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.extraLarge
                )
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    MaterialTheme.shapes.extraLarge
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.cancel),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.shapes.extraLarge
                            )
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.retake_photo),
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp, start = 24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Photo Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(8.dp)
                        //.clip(RoundedCornerShape(8))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Cancel Button
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .border(
                                4.dp,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.extraLarge
                            )
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .border(
                                4.dp,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.extraLarge
                            )
                    ) {
                        Text(
                            text = stringResource(id = R.string.choosing_photo),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                }
            }
        }
    }
}

