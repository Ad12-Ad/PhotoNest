package com.example.photonest.core.di

import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.data.repository.AuthRepositoryImpl
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.data.repository.PostRepositoryImpl
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.data.repository.UserRepositoryImpl
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.data.repository.CommentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): IPostRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): ICommentRepository
}
