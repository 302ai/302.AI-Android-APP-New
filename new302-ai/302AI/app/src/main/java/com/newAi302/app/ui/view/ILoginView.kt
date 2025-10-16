package com.newAi302.app.ui.view

import com.newAi302.app.base.mvp.BaseIView
import com.newAi302.app.bean.LoginBean
import com.newAi302.app.network.common_bean.exception.NetException

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : 登录 view
 * version: 1.0
 */
interface ILoginView : BaseIView {

    fun onLoginSuccess(bean: LoginBean?)  //登录成功

    fun onLoginFail(msg: NetException?)  //登录失败
}