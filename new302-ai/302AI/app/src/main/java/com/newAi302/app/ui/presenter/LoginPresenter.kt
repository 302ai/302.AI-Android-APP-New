package com.newAi302.app.ui.presenter

import com.newAi302.app.R
import com.newAi302.app.base.mvp.BasePresenter
import com.newAi302.app.bean.LoginBean
import com.newAi302.app.constant.AppConstant
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.callback.ResponseData
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.LoginModel
import com.newAi302.app.ui.view.ILoginView
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.google.gson.Gson
import com.newAi302.app.bean.PhoneSmsRnyCodeBean

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   :
 * version: 1.0
 */
class LoginPresenter : BasePresenter<ILoginView>() {

    /**
     * 邮箱登录
     * @param email 邮箱
     * @param passWord 密码
     * @param verifyCode 验证码
     * @param code 获取验证码随机生产的随机码
     */
    fun goLogin(email: String, passWord: String, verifyCode: String, code: String) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = email
        hashMap["phone"] = ""
        hashMap["password"] = passWord
        hashMap["ref"] = ""
        hashMap["event"] = ""
        hashMap["captcha"] = verifyCode //验证码
        hashMap["code"] = code
        hashMap["login_from"] = AppConstant.loginForm  //登录来源
        LoginModel.goLogin(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginSuccess(data?.data)
                    WearData.getInstance().saveLoginEmailCode(email)
                    WearData.getInstance().saveLoginEmailPassWord(passWord)
                }

//                LogUtils.e("ceshi 接口返回的内容是什么==========：", data?.data?.token)
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginFail(e)
                }
                when (e?.code) {
                    NetException.VERIFICATION_CODE_ERROR -> {
                        ToastUtils.showShort(R.string.verify_code_error)
                    }

                    NetException.LOGIN_WRONG_PASSWORD -> {
                        ToastUtils.showShort(R.string.login_password_error)
                    }
                }
            }
        })
    }

    fun goLoginAll(email: String, passWord: String, verifyCode: String, code: String) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["account"] = email
        hashMap["phone"] = ""
        hashMap["password"] = passWord
        hashMap["ref"] = ""
        hashMap["event"] = ""
        hashMap["captcha"] = verifyCode //验证码
        hashMap["code"] = code
        hashMap["login_from"] = AppConstant.loginForm  //登录来源
        LoginModel.goLoginAll(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginSuccess(data?.data)
                    WearData.getInstance().saveLoginEmailCode(email)
                    WearData.getInstance().saveLoginEmailPassWord(passWord)
                }

//                LogUtils.e("ceshi 接口返回的内容是什么==========：", data?.data?.token)
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginFail(e)
                }
                when (e?.code) {
                    NetException.VERIFICATION_CODE_ERROR -> {
                        ToastUtils.showShort(R.string.verify_code_error)
                    }

                    NetException.LOGIN_WRONG_PASSWORD -> {
                        ToastUtils.showShort(R.string.login_password_error)
                    }
                }
            }
        })
    }

    /**
     * 获取容联云手机验证码
     * @param mobile
     * @param verifyCode 图形验证码
     * @param code 生成图形验证码的字符串
     */
    fun getPhoneSmsRnyCode(mobile: String, captcha: String, code: String) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["mobile"] = mobile
        hashMap["captcha"] = captcha
        hashMap["code"] = code
        LoginModel.getUserSmsRnyCode(hashMap,
            object : RequestCallback<BaseResponse<PhoneSmsRnyCodeBean>>() {
                override fun onSuccess(data: BaseResponse<PhoneSmsRnyCodeBean>?) {
                    if (getView() != null) {
                        getView()?.onPhoneCodeSuccess()
                        getView()?.hideLoading()
                    }
                }

                override fun onError(e: NetException?) {
                    if (getView() != null) {
                        getView()?.onFail()
                        getView()?.hideLoading()
                    }
                    when (e?.code) {
                        NetException.VERIFICATION_CODE_ERROR -> {
                            ToastUtils.showShort(R.string.verify_code_error)
                        }

                        NetException.LOGIN_WRONG_PASSWORD -> {
                            ToastUtils.showShort(R.string.login_password_error)
                        }

                        NetException.ALREADY_REGISTERED -> {
                            ToastUtils.showShort(R.string.register_already_email_error)
                        }
                    }
                }
            })
    }

    /**
     * 手机号码登录
     */
    fun goLoginPhone(
        phoneNumber: String,
        password: String,
        verifyCode: String,
        code: String
    ) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = ""
        hashMap["phone"] = phoneNumber
        hashMap["password"] = password
        hashMap["ref"] = ""
        hashMap["event"] = ""
        hashMap["captcha"] = verifyCode //验证码
        hashMap["code"] = code
        hashMap["login_from"] = AppConstant.loginForm  //登录来源
        LoginModel.goLoginPhone(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginSuccess(data?.data)
                    WearData.getInstance().saveLoginPhoneCode(phoneNumber)
                    WearData.getInstance().saveLoginPhonePassWord(password)
                    LogUtils.e("ceshi ======手机号码登录成功啦=========：", data?.data?.token)
                }
            }

            override fun onError(e: NetException?) {
//                LogUtils.e("ceshi  phone接口返回的错误===========：", e?.code, e?.message)
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginFail(e)
                }

                when (e?.code) {
                    NetException.VERIFICATION_CODE_ERROR -> {
                        ToastUtils.showShort(R.string.verify_code_error)
                    }

                    NetException.LOGIN_WRONG_PASSWORD -> {
                        ToastUtils.showShort(R.string.login_password_error)
                    }
                }
            }
        })
    }

    fun goLoginPhoneSms(
        phoneNumber: String,
        password: String,
        verifyCode: String,
        code: String
    ) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = ""
        hashMap["id_token"] = ""
        hashMap["phone_number"] = phoneNumber
        hashMap["sms_code"] = password
        hashMap["ref"] = ""
        hashMap["event"] = ""
        hashMap["captcha"] = verifyCode //验证码
        hashMap["code"] = code
        hashMap["login_from"] = AppConstant.loginForm  //登录来源
        LoginModel.goLoginSmsPhone(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginSuccess(data?.data)
                    WearData.getInstance().saveLoginPhoneCode(phoneNumber)
                    WearData.getInstance().saveLoginPhonePassWord(password)
                    LogUtils.e("ceshi ======手机号码登录成功啦=========：", data?.data?.token)
                }
            }

            override fun onError(e: NetException?) {
//                LogUtils.e("ceshi  phone接口返回的错误===========：", e?.code, e?.message)
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginFail(e)
                }

                when (e?.code) {
                    NetException.VERIFICATION_CODE_ERROR -> {
                        ToastUtils.showShort(R.string.verify_code_error)
                    }

                    NetException.LOGIN_WRONG_PASSWORD -> {
                        ToastUtils.showShort(R.string.login_password_error)
                    }
                }
            }
        })
    }


    //google register and login 2
    fun goRegisterLoginGoogle2(user_id:String,email: String){
        val hashMap: MutableMap<String, String> = HashMap()
        hashMap["user_id"] = user_id
        hashMap["email"] = email
        LoginModel.goLoginGoogle2(hashMap,  object: LoginCallback {
            override fun onSuccess(responseBody: String?) {
                val gson = Gson()
                android.util.Log.e("ceshi","0success=${responseBody}")
                val response = gson.fromJson(responseBody, ResponseData::class.java)
//                val token = response.data.token.replace("Basic|\\s".toRegex(), "")
//                android.util.Log.e("ceshi","success=${token}")
//                WearData.getInstance().saveToken(token)
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onLoginSuccess(response.data)
                }
            }

            override fun onFailure(errorCode: Int, errorMessage: String?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                }

                if (getView() != null) {
                    //getView()?.onLoginFail()
                    getView()?.hideLoading()
                }
                when (errorCode.toString()) {
                    NetException.VERIFICATION_CODE_ERROR -> {
                        ToastUtils.showShort(R.string.verify_code_error)
                    }

                    NetException.LOGIN_WRONG_PASSWORD -> {
                        ToastUtils.showShort(R.string.login_password_error)
                    }

                    NetException.ALREADY_REGISTERED -> {
                        ToastUtils.showShort(R.string.register_already_email_error)
                    }
                }
            }

        })
    }

}