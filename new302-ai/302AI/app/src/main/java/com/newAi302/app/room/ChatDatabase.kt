package com.newAi302.app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
@Database(entities = [ChatItemRoom::class,
    ModelDataRoom::class,
    UserConfigurationRoom::class// 添加这行，关联新实体
            ], version = 5, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                )// 添加这行：版本不匹配时允许破坏性重建
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}