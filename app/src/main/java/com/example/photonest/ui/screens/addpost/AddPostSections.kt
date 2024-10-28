package com.example.photonest.ui.screens.addpost

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.photonest.R


@Composable
fun ImagePickerSection(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onPickImage,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 350.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add_image_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(40.dp)
                        .fillMaxWidth(),
                    alpha = 0.6f
                )
            }
        }
    }
}
