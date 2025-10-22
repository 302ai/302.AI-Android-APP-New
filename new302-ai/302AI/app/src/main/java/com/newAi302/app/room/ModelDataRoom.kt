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
@Entity(tableName = "model_items",
    // 为 title 字段添加唯一索引（全局唯一）
    indices = [Index(value = ["modelId"], unique = true)])
@TypeConverters(MessagesConverter::class)
data class ModelDataRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modelId: String,
    val remark: String,
    val reasoning: Boolean,
    val imageUnderstanding: Boolean,
    val baseUrl: String,
    val apiKey: String,
    val isCustomize: Boolean = false
): Serializable