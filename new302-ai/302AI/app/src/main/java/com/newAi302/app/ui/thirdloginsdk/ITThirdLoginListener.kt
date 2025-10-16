package com.newAi302.app.ui.thirdloginsdk

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */

//登录成功回调接口
interface ITLoginListener {

    fun onStart(tLoginType: TLoginMgr.LoginType?)

    fun onSuccess(result: TLoginBean?)

    fun onSuccess1(token:String?)

    fun onSuccess2(user_id:String?,email:String?)

    fun onCancel()

    fun onFailed(errMsg: String?)
}


interface ITLogoutListener {
    fun onLogout()
}