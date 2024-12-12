package com.example.visionary.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SaveScreen(
    bitmaps: List<Bitmap>,
    modifier: Modifier = Modifier,
) {

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp // Screen total height
    val navbarHeight = screenHeight * 0.1f

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    if(bitmaps.isEmpty()) {
        Box(
            modifier = modifier
                .offset(y = (100).dp),
            contentAlignment = Alignment.Center
        ) {
            Text("There are no photos yet")
        }
    } else {
        Box (modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(1f)
            ) {
                Text(
                    "Saves",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Divider(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 3.dp
                )
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp,
                userScrollEnabled = true,
                contentPadding = PaddingValues(
                    top = (16+64+16+16+16).dp,
                    bottom = 16.dp + navbarHeight,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = modifier //.size(width = 800.dp, height = 1200.dp)
            ) {
                items(bitmaps) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { selectedBitmap = bitmap },
                        contentScale = ContentScale.Crop
                    )

                }
            }

            // Display a card preview
            if (selectedBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(top = (0.025f * LocalConfiguration.current.screenHeightDp).dp)
                        .clickable { selectedBitmap = null }
                        .zIndex(2f),
                    contentAlignment = Alignment.TopCenter
                ){
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp)
                            .width(700.dp)
                            .height(1100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        // Close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ){
                            IconButton(onClick = { selectedBitmap = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Image Title",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }


                        Spacer(modifier = Modifier.height(50.dp))

                        // Display the image preview
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(600.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    bitmaps.drop(bitmaps.indexOf(selectedBitmap))
                                },
                                colors =  ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,

                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 3.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                )
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                Text("Delete from favorites", style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ))
                            }
//                            Button(
//                                onClick = { },
//                                colors =  ButtonDefaults.buttonColors(
//                                    containerColor = MaterialTheme.colorScheme.secondary,
//                                    contentColor = MaterialTheme.colorScheme.onSecondary,
//
//                                    ),
//                                border = ButtonDefaults.outlinedButtonBorder.copy(
//                                    width = 2.dp,
//                                    brush = Brush.verticalGradient(
//                                        colors = listOf(
//                                            MaterialTheme.colorScheme.primary,
//                                            MaterialTheme.colorScheme.primary
//                                        )
//                                    )
//                                )
//                            ) {
//                                Icon(Icons.Filled.Face, contentDescription = "VR")
//                                Text("View in AR", style = MaterialTheme.typography.labelSmall.copy(
//                                    fontWeight = FontWeight.Bold
//                                ))
//                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Information section
                        Text("Informations", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(15.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "\uD83D\uDCDE Phone number: _ _ _",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "\uD83D\uDCCD Place: _ _ _",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "✉️ Mail: _ _ _",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "🔗 Link: _ _ _",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "📅 Date: _ _ _",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

