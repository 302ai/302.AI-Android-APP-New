package com.newAi302.app.room

import androidx.room.TypeConverter
import com.newAi302.app.data.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class MessagesConverter {
    @TypeConverter
    fun fromMessagesList(messages: List<ChatMessage>): String {
        return Gson().toJson(messages)
    }

    @TypeConverter
    fun toMessagesList(messagesString: String): List<ChatMessage> {
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return Gson().fromJson(messagesString, type)
    }


    // ======================== 新增：处理 List<String>（用于 modelList） ========================
    @TypeConverter
    fun fromStringList(list: MutableList<String>?): String? {
        return Gson().toJson(list) // 把 List<String> 转成 JSON 字符串（如 ["gpt-4", "claude-3"]）
    }

    @TypeConverter
    fun toStringList(json: String?): MutableList<String>? {
        if (json == null) return mutableListOf() // 空值时返回空列表，避免空指针
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(json, type) // 把 JSON 字符串转成 List<String>
    }

}