package com.newAi302.app.adapter

import androidx.recyclerview.widget.DiffUtil
import com.newAi302.app.data.ChatMessage

/**
 * author :
 * e-mail :
 * time   : 2025/9/25
 * desc   :
 * version: 1.0
 */
// 1. 定义 DiffUtil 回调
class ChatMessageDiffCallback(
    private val oldList: List<ChatMessage>,
    private val newList: List<ChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    // 判断 Item 是否为同一对象（用 message 内容或唯一 ID 区分）
    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].message == newList[newPos].message
    }

    // 判断 Item 内容是否变化
    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
}