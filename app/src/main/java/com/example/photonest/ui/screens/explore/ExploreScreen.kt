package com.example.photonest.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.R
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.OnBoardingTextField
import com.example.photonest.ui.components.states.LoadingState
import com.example.photonest.ui.screens.explore.components.*

@Composable
fun ExploreScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Search Bar
        item {
            OnBoardingTextField(
                label = "Search here ...",
                showLabel = false,
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search_outlined),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClearSearch = viewModel::clearSearch,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                onSearch = viewModel::performSearch,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when {
            uiState.isLoading -> {
                item {
                    LoadingState(
                        message = "Discovering amazing content...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            uiState.error != null -> {
                item {
                    MyAlertDialog(
                        shouldShowDialog = uiState.showErrorDialog,
                        onDismissRequest = { viewModel.dismissError() },
                        title = "Can't Load",
                        text = uiState.error ?: "An unknown error occurred",
                        confirmButtonText = "Refresh",
                        onConfirmClick = { viewModel.refreshContent() }
                    )
                }
            }

            uiState.isSearchActive && uiState.searchQuery.isNotEmpty() -> {
                // Search Results - Add as items, not nested scrollables
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search summary
                item {
                    NormalText(
                        text = "Found ${uiState.searchResults.totalResults} results for \"${uiState.searchResults.query}\"",
                        fontWeight = FontWeight.SemiBold,
                        fontColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Categories
                if (uiState.searchResults.categories.isNotEmpty()) {
                    item {
                        NormalText(
                            text = "Categories",
                            fontWeight = FontWeight.Bold,
                            fontColor = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        CategoryChipRow(
                            categories = uiState.searchResults.categories,
                            onCategoryClick = viewModel::searchByCategory,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Users
                if (uiState.searchResults.users.isNotEmpty()) {
                    item {
                        Text(
                            text = "Users",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.searchResults.users.take(5)) { user ->
                        UserSearchItem(
                            user = user,
                            onClick = { onNavigateToProfile(user.id) },
                            modifier = Modifier.padding( vertical = 4.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Posts Grid - Using chunked rows
                if (uiState.searchResults.posts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Grid as chunked rows
                    val posts = uiState.searchResults.posts.take(12)
                    items(posts.chunked(3)) { rowPosts ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowPosts.forEach { post ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PostGridItem(
                                        post = post,
                                        onClick = { onNavigateToPostDetail(post.id) }
                                    )
                                }
                            }
                            repeat(3 - rowPosts.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Empty results
                if (uiState.searchResults.totalResults == 0 && uiState.searchResults.query.isNotEmpty()) {
                    item {
                        EmptySearchResults()
                    }
                }
            }

            else -> {
                // Trending Categories
                if (uiState.trendingCategories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Text(
                            text = "Trending Categories",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        CategoryGrid(
                            categories = uiState.trendingCategories,
                            onCategoryClick = { category ->
                                viewModel.searchByCategory(category.name)
                            },
                            modifier = Modifier.heightIn(max = 120.dp)
                        )
                    }
                }

                // Trending Posts
                if (uiState.trendingPosts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Text(
                            text = "Trending Posts",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        TrendingPostsGrid(
                            posts = uiState.trendingPosts,
                            onPostClick = onNavigateToPostDetail,
                            modifier = Modifier.heightIn(max = 200.dp)
                        )
                    }
                }

                // Suggested Users
                if (uiState.suggestedUsers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Text(
                            text = "Suggested for You",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        SuggestedUsersList(
                            users = uiState.suggestedUsers,
                            onUserClick = onNavigateToProfile,
                            onFollowClick = viewModel::followUser
                        )
                    }
                }
            }
        }
    }

    // Error dialog
    if (uiState.showErrorDialog && uiState.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("OK")
                }
            }
        )
    }
}
