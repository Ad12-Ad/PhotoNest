package com.example.photonest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhotoNestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}