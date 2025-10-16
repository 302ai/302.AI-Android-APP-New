package com.newAi302.app.infa

import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.room.ChatItemChat
import com.newAi302.app.room.ChatItemRoom

interface OnItemClickListener {
    fun onItemClick(chatItem: ChatItemRoom)

    fun onDeleteClick(selectList: MutableList<Int>)

    fun onBackFunctionClick(chatFunction: ChatBackMessage)
}