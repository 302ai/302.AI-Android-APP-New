package com.newAi302.app.data

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/7/30
 * desc   :
 * version: 1.0
 */
data class ChatBackCodeMessage(
    var message:String,
    val isMe :Boolean,
    var doType :String,
    var position:Int,
    var name :String
)