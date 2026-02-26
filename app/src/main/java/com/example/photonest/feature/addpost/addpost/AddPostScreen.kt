package com.example.photonest.feature.addpost.addpost

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.photonest.core.ui.components.ButtonOnboarding
import com.example.photonest.core.ui.components.MyAlertDialog
import com.example.photonest.core.ui.components.OnBoardingTextField
import com.example.photonest.feature.addpost.addpost.model.AddPostEvent
import com.example.photonest.feature.addpost.addpost.model.AddPostState

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToCamera: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddPostViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val capturedImageUri by savedStateHandle
        ?.getStateFlow<String?>("captured_image", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uriString ->
            viewModel.handleEvent(AddPostEvent.ImageSelected(Uri.parse(uriString)))
            savedStateHandle?.remove<String>("captured_image")
        }
    }

    // Handle successful post creation
    LaunchedEffect(state.isPostCreated) {
        if (state.isPostCreated) {
            viewModel.resetPostCreated()
            onPostCreated()
        }
    }

    // Error Dialog
    MyAlertDialog(
        shouldShowDialog = state.showErrorDialog,
        onDismissRequest = { viewModel.handleEvent(AddPostEvent.DismissErrorDialog) },
        title = "Error",
        text = state.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = { viewModel.handleEvent(AddPostEvent.DismissErrorDialog) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Post",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AddPostContent(
                    viewModel = viewModel,
                    state = state,
                    onEvent = viewModel::handleEvent,
                    onNavigateToCamera = onNavigateToCamera                )
            }
            // Loading Indicator
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun AddPostContent(
    viewModel: AddPostViewModel,
    state: AddPostState,
    onEvent: (AddPostEvent) -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onEvent(AddPostEvent.ImageSelected(uri))
    }

    // Image Section
    ImagePickerSection(
        selectedImageUri = state.selectedImageUri,
        onPickImage = { imagePickerLauncher.launch("image/*") },
        onCameraClick = { onNavigateToCamera() },
        reset = {viewModel.resetPostCreated()}
    )

    // Caption TextField
    OnBoardingTextField(
        label = "Write a caption...",
        showLabel = true,
        value = state.caption,
        onValueChange = { onEvent(AddPostEvent.CaptionChanged(it)) },
        maxLines = 5,
        showClearIcon = true,
        onClearSearch = { onEvent(AddPostEvent.CaptionChanged("")) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )

    // Location TextField
    OnBoardingTextField(
        label = "Add location (Optional)",
        showLabel = true,
        value = state.location,
        onValueChange = { onEvent(AddPostEvent.LocationChanged(it)) },
        maxLines = 1,
        showClearIcon = true,
        onClearSearch = { onEvent(AddPostEvent.LocationChanged("")) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth()
    )

    // Category Section
    CategorySection(
        caption = state.caption,
        searchQuery = state.searchQuery,
        selectedCategories = state.selectedCategories,
        onCaptionChange = { onEvent(AddPostEvent.CaptionChanged(it)) },
        onSearchQueryChange = { onEvent(AddPostEvent.SearchQueryChanged(it)) },
        onCategoryToggled = { onEvent(AddPostEvent.CategoryToggled(it)) },
        onClearCategories = { onEvent(AddPostEvent.ClearCategories) },
        categories = viewModel.getFilteredCategories(),
        maxCategories = AddPostViewModel.MAX_CATEGORIES
    )

    // Post Button
    ButtonOnboarding(
        buttonText = if (state.isLoading) "Posting..." else "Post",
        enabled = state.selectedImageUri != null &&
                state.selectedCategories.isNotEmpty() &&
                !state.isLoading,
        textSize = 18.sp,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        buttonColors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.5f)
        ),
        onClick = { onEvent(AddPostEvent.PostClicked) }
    )
}