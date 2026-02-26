package com.example.photonest.feature.explore

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.R
import com.example.photonest.feature.explore.components.CategoryGrid
import com.example.photonest.feature.explore.components.EmptySearchResults
import com.example.photonest.feature.explore.components.PostGridItem
import com.example.photonest.feature.explore.components.SuggestedUsersList
import com.example.photonest.feature.explore.components.TrendingPostsGrid
import com.example.photonest.feature.explore.components.UserSearchItem
import com.example.photonest.core.ui.components.NormalText
import com.example.photonest.core.ui.components.OnBoardingTextField
import com.example.photonest.ui.components.states.LoadingState
import com.example.photonest.core.utils.ObserveAsEvents
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExploreScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is ExploreEffect.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }

            is ExploreEffect.NavigateToProfile ->
                onNavigateToProfile(effect.userId)

            is ExploreEffect.NavigateToPost ->
                onNavigateToPostDetail(effect.postId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) {

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = {
                viewModel.onEvent(ExploreEvent.Refresh)
            },
            modifier = Modifier
                .fillMaxSize()
        ){
            LazyColumn(
                modifier = modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    OnBoardingTextField(
                        label = "Search here ...",
                        showLabel = false,
                        value = state.searchQuery,
                        onValueChange = {
                            viewModel.onEvent(
                                ExploreEvent.UpdateQuery(it)
                            )
                        },
                        prefix = {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search_outlined),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClearSearch = {
                            viewModel.onEvent(
                                ExploreEvent.ClearSearch
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        onSearch = {
                            viewModel.onEvent(
                                ExploreEvent.SubmitSearch
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (state.isLoading) {
                    item {
                        LoadingState(
                            message = "Discovering amazing content...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                if (state.isSearchActive && !state.isLoading) {

                    val results = state.searchResults

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        NormalText(
                            text = "Found ${results.totalResults} results for \"${results.query}\"",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    if (results.users.isNotEmpty()) {
                        item {
                            Text(
                                text = "Users",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        items(results.users.take(5)) { user ->
                            UserSearchItem(
                                user = user,
                                onClick = {
                                    viewModel.onEvent(
                                        ExploreEvent.OpenProfile(user.id)
                                    )
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (results.posts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Posts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        val posts = results.posts.take(12)

                        items(posts.chunked(3)) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { post ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        PostGridItem(
                                            post = post,
                                            onClick = {
                                                viewModel.onEvent(
                                                    ExploreEvent.OpenPost(post.id)
                                                )
                                            }
                                        )
                                    }
                                }
                                repeat(3 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (results.totalResults == 0) {
                        item { EmptySearchResults() }
                    }
                }

                if (!state.isSearchActive && !state.isLoading) {

                    if (state.trendingCategories.isNotEmpty()) {
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
                            CategoryGrid(
                                categories = state.trendingCategories,
                                onCategoryClick = {
                                    viewModel.onEvent(
                                        ExploreEvent.SearchByCategory(it.name)
                                    )
                                },
                                modifier = Modifier.heightIn(max = 120.dp)
                            )
                        }
                    }

                    if (state.trendingPosts.isNotEmpty()) {
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
                            TrendingPostsGrid(
                                posts = state.trendingPosts,
                                onPostClick = {
                                    viewModel.onEvent(
                                        ExploreEvent.OpenPost(it)
                                    )
                                },
                                modifier = Modifier.heightIn(max = 200.dp)
                            )
                        }
                    }

                    if (state.suggestedUsers.isNotEmpty()) {
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
                            SuggestedUsersList(
                                users = state.suggestedUsers,
                                onUserClick = {
                                    viewModel.onEvent(
                                        ExploreEvent.OpenProfile(it)
                                    )
                                },
                                onFollowClick = {
                                    viewModel.onEvent(
                                        ExploreEvent.FollowUser(it)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}