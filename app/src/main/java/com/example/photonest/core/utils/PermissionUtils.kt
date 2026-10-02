package com.example.photonest.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun checkAndRequestPermissions(
        context: Context,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
        onAlreadyGranted: () -> Unit
    ) {
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onAlreadyGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}