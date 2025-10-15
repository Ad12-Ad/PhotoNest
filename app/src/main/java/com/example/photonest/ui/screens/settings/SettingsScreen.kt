package com.example.photonest.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Settings
            item {
                SettingsSectionTitle(title = "Appearance")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = when (uiState.themeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "Follow system"
                    },
                    onClick = { viewModel.showThemeDialog() }
                )
            }

            // Notification Settings
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionTitle(title = "Notifications")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Push notifications",
                    subtitle = "Receive notifications about likes, comments, and follows",
                    checked = uiState.pushNotificationsEnabled,
                    onCheckedChange = viewModel::togglePushNotifications
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Favorite,
                    title = "Like notifications",
                    subtitle = "Get notified when someone likes your posts",
                    checked = uiState.likeNotificationsEnabled,
                    onCheckedChange = viewModel::toggleLikeNotifications
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Comment,
                    title = "Comment notifications",
                    subtitle = "Get notified when someone comments on your posts",
                    checked = uiState.commentNotificationsEnabled,
                    onCheckedChange = viewModel::toggleCommentNotifications
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.PersonAdd,
                    title = "Follow notifications",
                    subtitle = "Get notified when someone follows you",
                    checked = uiState.followNotificationsEnabled,
                    onCheckedChange = viewModel::toggleFollowNotifications
                )
            }

            // Privacy Settings
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionTitle(title = "Privacy")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Private account",
                    subtitle = "Only approved followers can see your posts",
                    checked = uiState.privateAccount,
                    onCheckedChange = viewModel::togglePrivateAccount
                )
            }

            // Data & Storage
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionTitle(title = "Data & Storage")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Clear cache",
                    subtitle = "Free up space by clearing cached images",
                    onClick = viewModel::clearCache
                )
            }

            // About
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionTitle(title = "About")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }

    // Theme Selection Dialog
    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(
            selectedTheme = uiState.themeMode,
            onThemeSelected = viewModel::setTheme,
            onDismiss = viewModel::hideThemeDialog
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose theme") },
        text = {
            Column {
                ThemeMode.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when (theme) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "Follow system"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}