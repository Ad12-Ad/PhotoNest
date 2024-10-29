package com.example.photonest.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.photonest.data.model.Post

@Composable
fun PostFeed(
    modifier: Modifier = Modifier,
    posts: List<Post>,
    onPostClick: (Post) -> Unit = {},
    onLikeClick: (Post) -> Unit = {},
    onBookmarkClick: (Post) -> Unit = {},
    onUserClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(
            items = posts,
            key = { it.id }
        ) { post ->
            PostItem(
                post = post,
                onPostClick = { onPostClick(post) },
                onLikeClick = { onLikeClick(post) },
                onBookmarkClick = { onBookmarkClick(post) },
                onUserClick = { onUserClick(post.userName) }
            )
        }
    }
}
