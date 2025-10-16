package com.newAi302.app.room

import androidx.lifecycle.LiveData

class ChatRepository(private val chatDao: ChatDao) {
    // 基于当前用户的操作
    suspend fun getCurrentUserChats(userId: String): List<ChatItemRoom> {
        return chatDao.getChatsByUserId(userId)
    }

    fun observeCurrentUserChats(userId: String): LiveData<List<ChatItemRoom>> {
        return chatDao.getChatsByUserIdLiveData(userId)
    }

    suspend fun addChatForUser(chat: ChatItemRoom) {
        chatDao.insertChat(chat)
    }

    suspend fun updateChatForUser(userId: String, chat: ChatItemRoom) {
        if (chat.userId == userId) {
            chatDao.updateChat(chat)
        } else {
            throw SecurityException("Cannot update chat for another user")
        }
    }

    suspend fun deleteChatForUser(userId: String, title: String) {
        chatDao.deleteChatByUserIdAndTitle(userId, title)
    }

    suspend fun clearUserChats(userId: String) {
        chatDao.deleteAllChatsByUserId(userId)
    }

    suspend fun getUserChatCount(userId: String): Int {
        return chatDao.getChatCountForUser(userId)
    }

    // 原有其他操作...
}