package com.newAi302.app.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
//import com.newAi302.app.ChatItem


/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
@Dao
interface ChatDao {
    // 插入时若 title 冲突，删除旧记录并插入新记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: ChatItemRoom)

    @Query("SELECT * FROM chat_items")
    fun getAllChats(): List<ChatItemRoom>

    @Query("SELECT EXISTS(SELECT 1 FROM chat_items WHERE title = :title)")
    fun checkTitleExists(title: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM chat_items WHERE time = :time)")
    fun checkTimeExists(time: String): Boolean

    @Query("SELECT * FROM chat_items WHERE title = :title LIMIT 1")
    fun getChatByTitle(title: String): ChatItemRoom?

//    @Query("SELECT * FROM chat_items WHERE messages LIKE '%' || :searchString || '%'")
//    fun getChatsWithMessageContaining(searchString: String): List<ChatItemRoom>
    /**
     * 查询所有ChatItemRoom中，messages列表里有ChatMessage的message包含searchString的记录
     * 原理：利用存储的JSON字符串中包含目标文本进行匹配
     */
    @Query("SELECT * FROM chat_items WHERE messages LIKE '%\"message\":\"%' || :searchString || '%\"%'")
    fun getChatsWithMessageContaining(searchString: String): List<ChatItemRoom>

    @Query("SELECT * FROM chat_items WHERE title LIKE '%' || :searchString || '%'")
    fun getChatsWithTitleContaining(searchString: String): List<ChatItemRoom>

    @Query("DELETE FROM chat_items WHERE title = :title")
    fun deleteChatByTitle(title: String)

    @Query("DELETE FROM chat_items WHERE time = :time")
    fun deleteChatByTime(time: String)

    // 返回 LiveData，自动监听数据变化
    @Query("SELECT * FROM chat_items")
    fun getAllChatsLiveData(): LiveData<List<ChatItemRoom>>

    // 查询最后一条记录（按主键倒序排列，取第一条）
    @Query("SELECT * FROM chat_items ORDER BY id DESC LIMIT 1")
    fun getLastChatItem(): ChatItemRoom?

    // 查询第一条记录（按主键升序排列，取第一条）
    @Query("SELECT * FROM chat_items ORDER BY id ASC LIMIT 1")
    fun getFirstChatItem(): ChatItemRoom?

    @Update
    fun updateChat(chat: ChatItemRoom)


    //有关模型的一些方法
    // 1. 通过 modelId 查询对应的 ModelDataRoom
    @Query("SELECT * FROM model_items WHERE modelId = :modelId LIMIT 1")
    fun getModelById(modelId: String): ModelDataRoom?

    // 2. 插入 ModelDataRoom（若 modelId 冲突则替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertModel(model: ModelDataRoom)

    // 3. 通过 modelId 删除 ModelDataRoom
    @Query("DELETE FROM model_items WHERE modelId = :modelId")
    fun deleteModelById(modelId: String)

    // 4. 查询所有 ModelDataRoom
    @Query("SELECT * FROM model_items")
    fun getAllModels(): List<ModelDataRoom>

    /***
     * 账号区分
     */
    // 基于账号的查询操作
    @Query("SELECT * FROM chat_items WHERE userId = :userId")
    fun getChatsByUserId(userId: String): List<ChatItemRoom>

    @Query("SELECT * FROM chat_items WHERE userId = :userId")
    fun getChatsByUserIdLiveData(userId: String): LiveData<List<ChatItemRoom>>

    @Query("SELECT * FROM chat_items WHERE userId = :userId AND title = :title LIMIT 1")
    fun getChatByUserIdAndTitle(userId: String, title: String): ChatItemRoom?

    // 基于账号的删除操作
    @Query("DELETE FROM chat_items WHERE userId = :userId")
    fun deleteAllChatsByUserId(userId: String)

    @Query("DELETE FROM chat_items WHERE userId = :userId AND title = :title")
    fun deleteChatByUserIdAndTitle(userId: String, title: String)

    // 基于账号的更新操作
    @Transaction
    fun updateChatForUser(userId: String, updatedChat: ChatItemRoom) {
        // 确保更新的是当前用户的聊天记录
        if (updatedChat.userId == userId) {
            updateChat(updatedChat)
        }
    }

    // 基于账号的批量操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatsForUser(chats: List<ChatItemRoom>)

    // 检查用户是否有聊天记录
    @Query("SELECT EXISTS(SELECT 1 FROM chat_items WHERE userId = :userId)")
    fun hasChatsForUser(userId: String): Boolean

    // 获取用户聊天记录数量
    @Query("SELECT COUNT(*) FROM chat_items WHERE userId = :userId")
    fun getChatCountForUser(userId: String): Int


    // ======================== 新增：UserConfigurationRoom 相关方法 ========================
    /**
     * 插入/更新用户配置：若 userId 已存在（触发唯一约束），则替换旧配置
     * 适用场景：保存或更新当前用户的系统设置（语言、主题等）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserConfig(config: UserConfigurationRoom)

    /**
     * 根据 userId 查询对应的用户配置
     * @return 若存在则返回配置，不存在则返回 null（因 userId 是唯一索引，最多返回 1 条）
     */
    @Query("SELECT * FROM user_items WHERE userId = :userId LIMIT 1")
    fun getUserConfigByUserId(userId: String): UserConfigurationRoom?

    /**
     * （可选）根据 userId 删除用户配置
     * 适用场景：用户注销时清理配置数据
     */
    @Query("DELETE FROM user_items WHERE userId = :userId")
    fun deleteUserConfigByUserId(userId: String)

    /**
     * （可选）查询所有用户配置（一般用于管理员场景，非必要）
     */
    @Query("SELECT * FROM user_items")
    fun getAllUserConfigs(): List<UserConfigurationRoom>


}