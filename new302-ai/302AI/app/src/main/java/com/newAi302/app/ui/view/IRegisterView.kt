package com.newAi302.app.ui.view

import com.newAi302.app.base.mvp.BaseIView

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 注册 view
 * version: 1.0
 */
interface IRegisterView : BaseIView {

    fun onEmailCodeSuccess()  //获取email_code成功

    override fun onPhoneCodeSuccess()  //发送手机验证码成功

    fun onRegisterSuccess()   //注册成功

    override fun onFail()  //失败处理
}