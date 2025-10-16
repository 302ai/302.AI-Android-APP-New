package com.newAi302.app.infa

import com.newAi302.app.data.ChatTitle


fun interface OnChatTitleSelectedListener {
    fun onChatTitleSelected(selectedChatTitle: ChatTitle)
}