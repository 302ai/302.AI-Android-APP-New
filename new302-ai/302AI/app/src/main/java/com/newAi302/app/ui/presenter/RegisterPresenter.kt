package com.newAi302.app.ui.presenter

import com.newAi302.app.R
import com.newAi302.app.base.mvp.BasePresenter
import com.newAi302.app.bean.LoginBean
import com.newAi302.app.bean.PhoneSmsRnyCodeBean
import com.newAi302.app.bean.RegisterBeanRes
import com.newAi302.app.constant.AppConstant
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.callback.ResponseData
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.LoginModel
import com.newAi302.app.ui.view.IRegisterView
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.google.gson.Gson
import okhttp3.FormBody


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 注册 presenter
 * version: 1.0
 */
class RegisterPresenter : BasePresenter<IRegisterView>() {

    /**
     * 邮箱注册
     *
     * @param userName 用户名称
     * @param inviteCode 邀请码
     * @param email 邮箱
     * @param password 密码
     * @param verifyCode 图形验证码
     * @param code 图形验证随机生产的随机数
     */
    fun goRegisterEmail(
        userName: String,
        inviteCode: String,
        email: String,
        password: String,
        emailCode: String,
        verifyCode: String,
        code: String,
    ) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["password"] = password
        hashMap["name"] = userName
        hashMap["email"] = email
        hashMap["phone"] = ""
        hashMap["from_invite_code"] = inviteCode
        hashMap["email_code"] = emailCode
        hashMap["captcha"] = verifyCode
        hashMap["code"] = code
        LoginModel.registerEmail(hashMap,
            object : RequestCallback<BaseResponse<RegisterBeanRes>>() {
                override fun onSuccess(data: BaseResponse<RegisterBeanRes>?) {
                    if (getView() != null) {
                        goRegisterLoginEmail(email, password, verifyCode, code)
                    }
                    ToastUtils.showShort(R.string.register_success)
                }

                override fun onError(e: NetException?) {
                    if (getView() != null) {
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
     * 手机号码注册
     * @param name 用户名称
     * @param inviteCode 邀请码
     * @param phoneNumber 手机号码
     * @param password 密码
     * @param confirmPassword 二次密码
     * @param smsCode 手机验证码
     */
    fun goRegisterPhone(
        name: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,
        smsCode: String,
        inviteCode: String,
    ) {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["sms_code"] = smsCode
        hashMap["phone_number"] = phoneNumber
        hashMap["name"] = name
        hashMap["password"] = password
        hashMap["confirmPassword"] = confirmPassword
        hashMap["from_invite_code"] = inviteCode

        LoginModel.registerPhone(hashMap,
            object : RequestCallback<BaseResponse<RegisterBeanRes>>() {
                override fun onSuccess(data: BaseResponse<RegisterBeanRes>?) {
                    LogUtils.e("ceshi goRegisterPhone==========:++++++")
                    ToastUtils.showShort(R.string.register_success)
                    if (getView() != null) {
                        getView()?.hideLoading()
                        getView()?.onRegisterSuccess()
                    }
                    //ToastUtils.showShort(R.string.register_success)
                }

                override fun onError(e: NetException?) {
                    LogUtils.e("ceshi goRegisterPhone==========:", e?.code, e?.message)

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

    //google register and login
    fun goRegisterLoginGoogle(idToken:String){
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["id_token"] = idToken

//        // 使用FormBody来构造请求体，添加id_token参数
//        val formBody: FormBody = Builder()
//            .add("id_token", id_token)
//            .build()
        android.util.Log.e("ceshi","1token$idToken")
        LoginModel.goLoginGoogle(idToken, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onRegisterSuccess()
                }
                android.util.Log.e("ceshi","success=${data?.data?.token}")
                WearData.getInstance().saveToken(data?.data?.token)
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                }

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

    //google register and login 1
    fun goRegisterLoginGoogle1(user_id:String,email: String){
        val hashMap: MutableMap<String, String> = HashMap()
        hashMap["user_id"] = user_id
        hashMap["email"] = email
        LoginModel.goLoginGoogle1(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onRegisterSuccess()
                }
                android.util.Log.e("ceshi","success=${data?.data?.token}")
                WearData.getInstance().saveToken(data?.data?.token)
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                }

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

    //google register and login 2
    fun goRegisterLoginGoogle2(user_id:String,email: String){
        val hashMap: MutableMap<String, String> = HashMap()
        hashMap["user_id"] = user_id
        hashMap["email"] = email
        LoginModel.goLoginGoogle2(hashMap,  object: LoginCallback {
            override fun onSuccess(responseBody: String?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onRegisterSuccess()
                }
                val gson = Gson()
                android.util.Log.e("ceshi","0success=${responseBody}")
                val response = gson.fromJson(responseBody, ResponseData::class.java)
                val token = response.data.token.replace("Basic|\\s".toRegex(), "")
                android.util.Log.e("ceshi","success=${token}")
                WearData.getInstance().saveToken(token)
            }

            override fun onFailure(errorCode: Int, errorMessage: String?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                }

                if (getView() != null) {
                    getView()?.onFail()
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

    /**
     * 邮箱注册即登录
     */
    private fun goRegisterLoginEmail(
        email: String,
        password: String,
        captcha: String,
        code: String
    ) {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = email
        hashMap["phone"] = ""
        hashMap["password"] = password
        hashMap["ref"] = ""
        hashMap["event"] = ""
        hashMap["captcha"] = captcha //验证码
        hashMap["code"] = code
        hashMap["login_from"] = AppConstant.loginForm  //登录来源
        LoginModel.goLogin(hashMap, object : RequestCallback<BaseResponse<LoginBean>>() {
            override fun onSuccess(data: BaseResponse<LoginBean>?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onRegisterSuccess()
                }
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                }

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
}