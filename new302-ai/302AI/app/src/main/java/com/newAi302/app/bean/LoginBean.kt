package com.newAi302.app.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/9
 * desc   :
 * version: 1.0
 */
@Parcelize
data class LoginBean(

    val token: String = ""  //获取token

) : Parcelable