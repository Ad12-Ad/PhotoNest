package com.example.photonest.ui.screens.addpost

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.photonest.R
import com.example.photonest.ui.components.OnBoardingTextField

@Composable
fun CategorySection(
    caption: String,
    onCaptionChange: (String) -> Unit,
    searchQuery: String,
    selectedCategories: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onCategoryToggled: (String) -> Unit,
    onClearCategories: () -> Unit,
    categories: List<String>,
    maxCategories: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnBoardingTextField(
            label = "Search Category ...",
            showLabel = false,
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            prefix = {
                Icon(
                    painter = painterResource(id = R.drawable.icon_search_outlined),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        if (selectedCategories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedCategories.toList()) { category ->
                    FilterChip(
                        selected = true,
                        onClick = { onCategoryToggled(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            iconColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove category",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories.filter { it !in selectedCategories }) { category ->
                FilterChip(
                    selected = false,
                    onClick = { onCategoryToggled(category) },
                    label = { Text(category) },
                    enabled = selectedCategories.size < maxCategories
                )
            }
        }

        if (selectedCategories.size >= maxCategories) {
            Text(
                text = "Maximum $maxCategories categories allowed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        OnBoardingTextField(
            label = "Caption ...",
            showLabel = false,
            value = caption,
            onValueChange = onCaptionChange,
        )
    }
}

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
