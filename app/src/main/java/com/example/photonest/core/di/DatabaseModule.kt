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
        ).build()
    }

    @Provides
    fun provideUserDao(database: PhotoNestDatabase): UserDao = database.userDao()

    @Provides
    fun providePostDao(database: PhotoNestDatabase): PostDao = database.postDao()

    @Provides
    fun provideCommentDao(database: PhotoNestDatabase): CommentDao = database.commentDao()

    @Provides
    fun provideCategoryDao(database: PhotoNestDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideFollowDao(database: PhotoNestDatabase): FollowDao = database.followDao()
}
