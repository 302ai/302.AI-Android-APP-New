package com.newAi302.app.ui.presenter

import com.newAi302.app.R
import com.newAi302.app.base.mvp.BasePresenter
import com.newAi302.app.bean.PhoneSmsRnyCodeBean
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.LoginModel
import com.newAi302.app.ui.view.IForgetPassWordView
import com.newAi302.app.utils.ToastUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   : 忘记密码 presenter
 * version: 1.0
 */
class ForgetPassWordPresenter : BasePresenter<IForgetPassWordView>() {

    /**
     * 重置密码 发送邮箱 email
     *
     * @param email 输入的邮箱
     * @param verifyCode 图形验证码
     * @param code 生成图形验证码的字符串
     *
     */
    fun resetLoginPassWordEmail(email: String, verifyCode: String, code: String) {
        if (getView() != null){
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = email
        hashMap["code"] = code
        hashMap["captcha"] = verifyCode
        LoginModel.resetLoginPassWord(hashMap, object : RequestCallback<BaseResponse<Any>>() {
            override fun onSuccess(data: BaseResponse<Any>?) {
                ToastUtils.showShort(R.string.change_password_success)
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onPhoneCodeSuccess()
                }
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    when (e?.code) {
                        NetException.VERIFICATION_CODE_ERROR -> {
                            ToastUtils.showShort(R.string.verify_code_error)
                        }

                        NetException.LOGIN_WRONG_PASSWORD -> {
                            ToastUtils.showShort(R.string.login_password_error)
                        }

                        NetException.NO_REGISTER -> {
                            ToastUtils.showShort(R.string.register_no_email_error)
                        }
                    }
                }
            }
        })
    }

    fun resetLoginPassWordEmailNew(email: String, email_code: String, password:String, confirmPassword:String) {
        if (getView() != null){
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = email
        hashMap["email_code"] = email_code
        hashMap["password"] = password
        hashMap["confirmPassword"] = confirmPassword
        hashMap["captcha"] = ""
        hashMap["code"] = ""
        hashMap["phone"] = ""
        LoginModel.resetLoginPassWordEmailNew(hashMap, object : RequestCallback<BaseResponse<Any>>() {
            override fun onSuccess(data: BaseResponse<Any>?) {
                ToastUtils.showShort(R.string.change_password_success_phone)
                if (getView() != null) {
                    getView()?.hideLoading()
                    getView()?.onChangePhonePassWordSuccess()
                }
            }

            override fun onError(e: NetException?) {
                if (getView() != null) {
                    getView()?.hideLoading()
                    when (e?.code) {
                        NetException.VERIFICATION_CODE_ERROR -> {
                            ToastUtils.showShort(R.string.verify_code_error)
                        }

                        NetException.LOGIN_WRONG_PASSWORD -> {
                            ToastUtils.showShort(R.string.login_password_error)
                        }

                        NetException.NO_REGISTER -> {
                            ToastUtils.showShort(R.string.register_no_email_error)
                        }
                    }
                }
            }
        })
    }

    /**
     * 手机号重置密码 phone
     *
     * @param phoneNumber 手机号码
     * @param smsCode 发送的手机验证码
     * @param passWord 输入的密码
     */
    fun resetLoginPassWordPhone(phoneNumber: String, smsCode: String, passWord: String) {
        if (getView() != null) {
            getView()?.showLoading()
        }
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["phone_number"] = phoneNumber
        hashMap["sms_code"] = smsCode
        hashMap["password"] = passWord
        LoginModel.resetLoginPassWordPhone(hashMap, object : RequestCallback<BaseResponse<Any>>() {
            override fun onSuccess(data: BaseResponse<Any>?) {
                ToastUtils.showShort(R.string.change_password_success_phone)
                if (getView() != null) {
                    getView()?.onChangePhonePassWordSuccess()
                    getView()?.hideLoading()
                }
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

                    NetException.NO_REGISTER -> {
                        ToastUtils.showShort(R.string.register_no_phone_error)
                    }
                }
            }
        })
    }

    /**
     * 获取容联云手机验证码
     *
     * @param mobile
     * @param verifyCode 图形验证码
     * @param code 生成图形验证码的字符串
     */
    fun getPhoneSmsRnyCode(mobile: String, captcha: String, code: String) {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["mobile"] = mobile
        hashMap["captcha"] = captcha
        hashMap["code"] = code
        LoginModel.getUserSmsRnyCode(hashMap,
            object : RequestCallback<BaseResponse<PhoneSmsRnyCodeBean>>() {
                override fun onSuccess(data: BaseResponse<PhoneSmsRnyCodeBean>?) {
                    if (getView() != null) {
                        getView()?.onPhoneCodeSuccess()
                    }
                }

                override fun onError(e: NetException?) {
                    if (getView() != null) {
                        getView()?.onFail()
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