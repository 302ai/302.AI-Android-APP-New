package com.newAi302.app.data

import java.io.Serializable

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/9/26
 * desc   :
 * version: 1.0
 */
data class MainMessage(
    val welcomeMsg: String,
    val senMsg: String,
    val bottomMsg:String,
    val imageUrl:String
): Serializable