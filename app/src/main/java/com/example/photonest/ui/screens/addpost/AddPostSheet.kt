package com.example.photonest.ui.screens.addpost

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.ui.components.ButtonOnboarding
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.screens.addpost.model.AddPostEvent
import com.example.photonest.ui.screens.addpost.model.AddPostState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostBottomSheet(
    viewModel: AddPostViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    MyAlertDialog(
        shouldShowDialog = state.showErrorDialog,
        onDismissRequest = {viewModel.handleEvent(AddPostEvent.DismissErrorDialog)},
        title = "Sign In Failed",
        text = state.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = { viewModel.handleEvent(AddPostEvent.DismissErrorDialog) }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        AddPostContent(
            viewModel= viewModel,
            state = state,
            onEvent = viewModel::handleEvent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
private fun AddPostContent(
    viewModel: AddPostViewModel,
    state: AddPostState,
    onEvent: (AddPostEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onEvent(AddPostEvent.ImageSelected(uri))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ImagePickerSection(
            selectedImageUri = state.selectedImageUri,
            onPickImage = { imagePickerLauncher.launch("image/*") }
        )

        CategorySection(
            caption = state.caption,
            searchQuery = state.searchQuery,
            selectedCategories = state.selectedCategories,
            onCaptionChange = { onEvent(AddPostEvent.CaptionChanged(it))},
            onSearchQueryChange = { onEvent(AddPostEvent.SearchQueryChanged(it)) },
            onCategoryToggled = { onEvent(AddPostEvent.CategoryToggled(it)) },
            onClearCategories = { onEvent(AddPostEvent.ClearCategories) },
            categories = viewModel.getFilteredCategories(),
            maxCategories = AddPostViewModel.MAX_CATEGORIES
        )

        ButtonOnboarding(
            buttonText = "Post",
            enabled = state.selectedImageUri != null &&
                    state.selectedCategories.isNotEmpty() &&
                    !state.isLoading,
            textSize = 20.sp,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .height(60.dp)
                .padding(0.dp)
                .fillMaxWidth(),
            buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.6f)
            ),
            onClick = { onEvent(AddPostEvent.PostClicked) }
        )
        if (state.isLoading){
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}