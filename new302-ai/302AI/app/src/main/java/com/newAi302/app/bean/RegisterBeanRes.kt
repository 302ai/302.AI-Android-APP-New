package com.newAi302.app.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   : 注册实体
 * version: 1.0
 */
@Parcelize
data class RegisterBeanRes(
    val token: String = ""
) : Parcelable

