package com.newAi302.app.ui.view

import com.newAi302.app.base.mvp.BaseIView

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   : 忘记密码 view
 * version: 1.0
 */
interface IForgetPassWordView : BaseIView {

    override fun onPhoneCodeSuccess()  //发送手机验证码成功

    override fun onFail()  //失败处理

    fun onChangePhonePassWordSuccess()
}