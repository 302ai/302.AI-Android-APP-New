package com.newAi302.app.infa

import com.newAi302.app.room.ChatItemRoom

interface OnSettingDialogClickListener {
    fun onModelTypeClick(modelType: String,mServiceProvider:String)
}