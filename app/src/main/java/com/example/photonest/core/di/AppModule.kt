package com.example.photonest.core.di

import android.content.Context
import androidx.room.Room
import com.example.photonest.core.utils.Constants
import com.example.photonest.data.local.database.PhotoNestDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotNestDatabase(
        @ApplicationContext context: Context
    ): PhotoNestDatabase {
        return Room.databaseBuilder(
            context,
            PhotoNestDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(database: PhotoNestDatabase) = database.userDao()

    @Provides
    fun providePostDao(database: PhotoNestDatabase) = database.postDao()

    @Provides
    fun provideCategoryDao(database: PhotoNestDatabase) = database.categoryDao()

    @Provides
    fun provideCommentDao(database: PhotoNestDatabase) = database.commentDao()

    @Provides
    fun provideFollowDao(database: PhotoNestDatabase) = database.followDao()

    @Provides
    fun provideNotificationDao(database: PhotoNestDatabase) = database.notificationDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
