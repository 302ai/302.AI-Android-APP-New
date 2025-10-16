package com.newAi302.app.ui.thirdloginsdk

import android.app.Activity
import android.content.Intent

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */
interface IXLogin {

    fun login(context: Activity, loginListener: ITLoginListener)

    fun onActivityResult(reqCode: Int, resCode: Int, data: Intent)

    fun logout(context: Activity, logoutListener: ITLogoutListener)

    fun release()
}