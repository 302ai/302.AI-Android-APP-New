package com.newAi302.app.ui.login

import android.os.CountDownTimer
import android.view.View
import com.newAi302.app.R
import com.newAi302.app.base.mvp.MVPBaseActivity
import com.newAi302.app.databinding.ActivityForgetPasswordNewBinding

import com.newAi302.app.ui.presenter.ForgetPassWordPresenter
import com.newAi302.app.ui.view.IForgetPassWordView

class ForgetPassWordNewActivity :
    MVPBaseActivity<IForgetPassWordView, ActivityForgetPasswordNewBinding, ForgetPassWordPresenter>(),
    IForgetPassWordView {

    private var mPhoneNumber = ""  //手机号码
    private var mVerifyCode = ""   //验证码
    private var mPassWord = ""
    private var mPassWordAgain = ""
    private var mImgVerifyCode = ""
    private var mRandomNum = ""
    private var countDownTimer: CountDownTimer? = null

    override fun createDataBinding(): ActivityForgetPasswordNewBinding {
        return ActivityForgetPasswordNewBinding.inflate(layoutInflater)
    }

    override fun initListener() {
        mBinding?.llEmailLogin?.setOnClickListener {
            setLoginSwitch(false)
        }

        mBinding?.llPhoneLogin?.setOnClickListener {
            setLoginSwitch(true)
        }
        
    }

    override fun initView() {
        setLoginSwitch(false)
    }

    override fun createPresenter(): ForgetPassWordPresenter {
        return ForgetPassWordPresenter()
    }

    override fun onPhoneCodeSuccess() {

    }

    override fun onFail() {

    }

    override fun onChangePhonePassWordSuccess() {

    }

    /**
     * @param isEmail 是否邮箱登录 true为邮箱， false为手机  默认为邮箱登录
     */
    private fun setLoginSwitch(isEmail: Boolean) {

        mBinding?.tvEmailLoginName?.setTextColor(
            if (!isEmail) resources.getColor(R.color.color302AI) else resources.getColor(
                R.color.un_selected
            )
        )
        mBinding?.viewEmailLine?.visibility = if (!isEmail) View.VISIBLE else View.GONE
        mBinding?.emailConst?.visibility = if (isEmail) View.VISIBLE else View.GONE

        mBinding?.tvEmailPhoneName?.setTextColor(
            if (!isEmail) resources.getColor(R.color.un_selected) else resources.getColor(
                R.color.color302AI
            )
        )
        mBinding?.viewPhoneLine?.visibility = if (!isEmail) View.GONE else View.VISIBLE
        mBinding?.SmsConst?.visibility = if (isEmail) View.GONE else View.VISIBLE

    }

}