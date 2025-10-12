package com.example.photonest.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.photonest.core.utils.Constants
import com.example.photonest.data.local.dao.*
import com.example.photonest.data.local.entities.*


@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CategoryEntity::class,
        CommentEntity::class,
        FollowEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhotoNestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun categoryDao(): CategoryDao
    abstract fun commentDao(): CommentDao
    abstract fun followDao(): FollowDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoNestDatabase? = null

        fun getDatabase(context: Context): PhotoNestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoNestDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
