package com.example.photonest.feature.auth.onboarding

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.photonest.app.navigation.AppDestinations
import com.example.photonest.app.theme.PhotoNestTheme
import com.example.photonest.core.ui.components.ButtonOnboarding
import com.example.photonest.core.ui.components.Heading1
import com.example.photonest.core.ui.components.NormalText
import com.example.photonest.core.ui.components.OnBoardingTextField
import com.example.photonest.core.ui.components.camera.CameraScreen
import com.example.photonest.core.utils.PermissionUtils
import com.example.photonest.feature.addpost.addpost.ImagePickerSection
import com.example.photonest.feature.auth.onboarding.model.OnboardingEvent
import com.example.photonest.feature.auth.onboarding.model.OnboardingUiState

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(OnboardingEvent.ProfilePicSelected(uri))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.CAMERA] == true) {
            showCamera = true
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            navController.navigate(AppDestinations.HOME_ROUTE) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { uri ->
                viewModel.onEvent(OnboardingEvent.ProfilePicSelected(uri))
                showCamera = false
            },
            onImageSelectedFromGallery = { uri ->
                viewModel.onEvent(OnboardingEvent.ProfilePicSelected(uri))
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Scaffold { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)) {
                // Progress Bar (3 steps)
                LinearProgressIndicator(
                    progress = uiState.currentStep / 3f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (uiState.currentStep) {
                    1 -> IdentityStep(uiState, viewModel::onEvent)
                    2 -> ProfileStep(
                        uiState,
                        onEvent = viewModel::onEvent,
                        onLaunchCamera = {
                            PermissionUtils.checkAndRequestPermissions(
                                context = context,
                                launcher = permissionLauncher,
                                onAlreadyGranted = { showCamera = true }
                            )
                        },
                        onLaunchGallery = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}
                    )
                    3 -> DemographicStep(uiState, viewModel::onEvent)
                }
            }
        }
    }
}

@Composable
fun IdentityStep(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    Column {
        Heading1(text = "Identity")
        NormalText(text = "Choose your name and @handle.")
        Spacer(modifier = Modifier.height(24.dp))
        OnBoardingTextField(value = state.name, onValueChange = { onEvent(OnboardingEvent.NameChanged(it)) }, label = "Full Name")
        Spacer(modifier = Modifier.height(16.dp))
        OnBoardingTextField(value = state.username, onValueChange = { onEvent(OnboardingEvent.UsernameChanged(it)) }, label = "Username")
        Spacer(modifier = Modifier.height(32.dp))
        ButtonOnboarding(
            buttonText = "Continue",
            onClick = { onEvent(OnboardingEvent.NextStep) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.name.isNotBlank() && state.username.isNotBlank(),
            shape = RoundedCornerShape(40.dp)
        )
    }
}

@Composable
fun ProfileStep(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Heading1(text = "Profile", modifier = Modifier.align(Alignment.Start))
        NormalText(text = "Add a photo and a short bio.", modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(24.dp))

        ImagePickerSection(
            selectedImageUri = state.profilePictureUri,
            reset = { onEvent(OnboardingEvent.ProfilePicSelected(null)) }, // Pass null to clear
            onPickImage = { onLaunchGallery() },
            onCameraClick = { onLaunchCamera() }
        )

        Spacer(modifier = Modifier.height(24.dp))
        OnBoardingTextField(value = state.bio, onValueChange = { onEvent(OnboardingEvent.BioChanged(it)) }, label = "Short Bio (Optional)")
        Spacer(modifier = Modifier.height(32.dp))
        ButtonOnboarding(
            buttonText = "Continue",
            onClick = { onEvent(OnboardingEvent.NextStep) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.profilePictureUri != null,
            shape = RoundedCornerShape(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ButtonOnboarding(
            buttonText = "Back",
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            onClick = { onEvent(OnboardingEvent.PreviousStep) },
            buttonColors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.surfaceContainer
            ),
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DemographicStep(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = convertMillisToDate(millis)
                        onEvent(OnboardingEvent.BirthdayChanged(formattedDate))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column {
        Heading1(text = "About You")
        NormalText(text = "Help us personalize your experience.")
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OnBoardingTextField(
                value = state.birthday,
                onValueChange = { },
                label = "Birthday"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OnBoardingTextField(
            value = state.location,
            onValueChange = { onEvent(OnboardingEvent.LocationChanged(it)) },
            label = "Location"
        )
        Spacer(modifier = Modifier.height(24.dp))
        ButtonOnboarding(
            buttonText = "Continue",
            onClick = {  onEvent(OnboardingEvent.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isLoading = state.isLoading,
            enabled = state.birthday.isNotBlank() && !state.isLoading,
            shape = RoundedCornerShape(40.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonOnboarding(
            buttonText = "Back",
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            onClick = { onEvent(OnboardingEvent.PreviousStep) },
            buttonColors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.surfaceContainer
            ),
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Preview(showBackground = true, name = "1. Identity Step")
@Composable
fun IdentityStepPreview() {
    PhotoNestTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            IdentityStep(
                state = OnboardingUiState(
                    name = "Jane Doe",
                    username = "janedoe123"
                ),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Profile Step (Empty)")
@Composable
fun ProfileStepEmptyPreview() {
    PhotoNestTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            ProfileStep(
                state = OnboardingUiState(
                    bio = ""
                ),
                onEvent = {},
                onLaunchCamera = {},
                onLaunchGallery = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Profile Step (Filled)")
@Composable
fun ProfileStepFilledPreview() {
    PhotoNestTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            ProfileStep(
                state = OnboardingUiState(
                    bio = "I love photography and capturing moments!"
                ),
                onEvent = {},
                onLaunchCamera = {},
                onLaunchGallery = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. Demographic Step (Idle)")
@Composable
fun DemographicStepPreview() {
    PhotoNestTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            DemographicStep(
                state = OnboardingUiState(
                    birthday = "01/01/1990",
                    location = "New York, USA",
                    isLoading = false
                ),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. Demographic Step (Loading)")
@Composable
fun DemographicStepLoadingPreview() {
    PhotoNestTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            DemographicStep(
                state = OnboardingUiState(
                    birthday = "01/01/1990",
                    location = "New York, USA",
                    isLoading = true
                ),
                onEvent = {}
            )
        }
    }
}