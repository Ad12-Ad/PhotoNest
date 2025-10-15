package com.example.photonest.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.photonest.ui.components.*
import com.example.photonest.ui.theme.bodyFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    // Success handler
    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            onNavigateBack()
        }
    }


    // Error dialog
    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = viewModel::dismissError,
        title = "Update Failed",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = viewModel::dismissError
    )

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    BackCircleButton(
                        onClick = onNavigateBack
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = { viewModel.setEditing(true) },
                    ) {
                        Text(
                            text = "Edit",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = bodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 25.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture Section
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.profilePictureUri ?: uiState.currentUser?.profilePicture)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") }
                    )

                    if (uiState.isEditing){
                        FloatingActionButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.size(32.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Change photo",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Name Field
                OnBoardingTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    readOnly = !uiState.isEditing,
                    label = "Name",
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    showClearIcon = uiState.isEditing,
                    onClearSearch = { viewModel.updateName("") }
                )

                // Username Field
                OnBoardingTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    readOnly = !uiState.isEditing,
                    label = "Username",
                    isError = uiState.usernameError != null,
                    errorMessage = uiState.usernameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    showClearIcon = uiState.isEditing,
                    onClearSearch = { viewModel.updateUsername("") }
                )

                // Bio Field
                OnBoardingTextField(
                    value = uiState.bio,
                    onValueChange = viewModel::updateBio,
                    readOnly = !uiState.isEditing,
                    label = "Bio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 4,
                    isError = uiState.bioError != null,
                    errorMessage = uiState.bioError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    showClearIcon = uiState.isEditing,
                    onClearSearch = { viewModel.updateBio("") }
                )

                // Website Field
                OnBoardingTextField(
                    value = uiState.website,
                    onValueChange = viewModel::updateWebsite,
                    readOnly = !uiState.isEditing,
                    label = "Website",
                    isError = uiState.websiteError != null,
                    errorMessage = uiState.websiteError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    showClearIcon = uiState.isEditing,
                    onClearSearch = { viewModel.updateWebsite("") }
                )

                // Location Field
                OnBoardingTextField(
                    value = uiState.location,
                    onValueChange = viewModel::updateLocation,
                    readOnly = !uiState.isEditing,
                    label = "Location",
                    isError = uiState.locationError != null,
                    errorMessage = uiState.locationError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    showClearIcon = uiState.isEditing,
                    onClearSearch = { viewModel.updateLocation("") }
                )

                ButtonOnboarding(
                    buttonText = "Update Profile",
                    textSize = 20.sp,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    onClick = viewModel::saveProfile,
                    enabled = uiState.isInputValid && !uiState.isLoading
                )
            }
        }
    }
}
