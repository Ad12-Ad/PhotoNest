package com.example.photonest.ui.screens.addpost

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search TextField with Category Counter
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OnBoardingTextField(
                label = "Search or create category...",
                showLabel = false,
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onClearSearch = { onSearchQueryChange("") },
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search_outlined),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Category counter
            Text(
                text = "${selectedCategories.size}/$maxCategories categories selected",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedCategories.size >= maxCategories)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Selected Categories Row
        if (selectedCategories.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Selected Categories",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedCategories.toList()) { category ->
                        FilterChip(
                            selected = true,
                            onClick = { onCategoryToggled(category) },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
//                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                iconColor = MaterialTheme.colorScheme.onPrimary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $category",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Check if search query creates a new custom category
        val trimmedQuery = searchQuery.trim()
        val isCustomCategory = trimmedQuery.isNotEmpty() &&
                !categories.any { it.equals(trimmedQuery, ignoreCase = true) } &&
                !selectedCategories.any { it.equals(trimmedQuery, ignoreCase = true) }

        // Show "Create Custom Category" chip if applicable
        if (isCustomCategory && selectedCategories.size < maxCategories) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Create New",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        // Capitalize first letter of each word
                        val formattedCategory = trimmedQuery
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { it.uppercase() }
                            }
                        onCategoryToggled(formattedCategory)
                        onSearchQueryChange("") // Clear search after adding
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Create \"${trimmedQuery}\"",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        // Available Categories from Predefined List
        val availableCategories = categories.filter { it !in selectedCategories }

        if (availableCategories.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (searchQuery.isEmpty()) "Suggested Categories" else "Matching Categories",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategories) { category ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategoryToggled(category) },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            enabled = selectedCategories.size < maxCategories,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // Show message when no categories match and query is too short
        if (availableCategories.isEmpty() && !isCustomCategory && searchQuery.isNotEmpty()) {
            Text(
                text = "No matching categories found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Maximum categories warning
        if (selectedCategories.size >= maxCategories) {
            Text(
                text = "Maximum $maxCategories categories reached",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
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
