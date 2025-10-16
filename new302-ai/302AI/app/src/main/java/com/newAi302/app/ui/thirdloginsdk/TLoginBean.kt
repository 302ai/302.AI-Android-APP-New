package com.newAi302.app.ui.thirdloginsdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */
@Parcelize
data class TLoginBean(
    val token: String?,
    val id: String?,
    val typeValue: String?,
    val loginType: TLoginMgr.LoginType,
    val email: String?
) : Parcelable