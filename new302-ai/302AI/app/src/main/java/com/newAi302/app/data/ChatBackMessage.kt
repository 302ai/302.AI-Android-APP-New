package com.newAi302.app.data

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/7/30
 * desc   :
 * version: 1.0
 */
data class ChatBackMessage(
    var message:String,
    val isMe :Boolean,
    var doType :String,
    var position:Int,
    var name:String,
    var fileName: MutableList<String> = mutableListOf(), // 默认值：空列表
    var fileSize: MutableList<String> = mutableListOf()
)