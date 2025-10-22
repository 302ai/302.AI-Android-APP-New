package com.newAi302.app.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.newAi302.app.data.ChatMessage
import java.io.Serializable

/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
@Entity(tableName = "chat_items",
    // 为 title 字段添加唯一索引（全局唯一）
    indices = [Index(value = ["title"], unique = true),
            Index(value = ["userId"]) // 新增userId索引，优化查询性能
    ])
@TypeConverters(MessagesConverter::class)
data class ChatItemRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val messages: MutableList<ChatMessage>,
    val time: String,
    val modelType: String,
    val isDeepThink: Boolean,
    val isNetWorkThink: Boolean,
    val userId: String,
    val isMe: Boolean,
    val isCollected: Boolean,
    val isR1Fusion: Boolean
): Serializable