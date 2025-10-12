package com.example.photonest.core.di

import android.content.Context
import androidx.room.Room
import com.example.photonest.core.utils.Constants
import com.example.photonest.data.local.dao.*
import com.example.photonest.data.local.database.PhotoNestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): PhotoNestDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PhotoNestDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development only
            .build()
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

}
