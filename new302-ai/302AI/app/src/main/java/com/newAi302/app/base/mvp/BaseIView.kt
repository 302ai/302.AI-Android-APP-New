package com.newAi302.app.base.mvp

import android.os.Bundle

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
interface BaseIView {

    fun initData(savedInstanceState: Bundle?) //暴露给presenter，可以刷新
    fun showLoading()

    fun hideLoading()

    fun showError(errorMsg: String?) //显示错误信息，弹toast


//    fun getMyActivity(): BaseActivity?

    fun updateViews(type: Int, vararg data: Any?) //可用于刷新界面

    fun onPhoneCodeSuccess()  //发送手机验证码成功

    fun onFail()  //失败处理


}