package com.newAi302.app.ui.login

import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.base.mvp.MVPBaseActivity
import com.newAi302.app.databinding.ActivityForgetPasswordPhoneBinding
import com.newAi302.app.ui.presenter.ForgetPassWordPresenter
import com.newAi302.app.ui.view.IForgetPassWordView
import com.newAi302.app.widget.ProxyActivityNavUtil
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.view.PassWorkEditView
import com.newAi302.app.widget.view.PhoneLocationView
import com.newAi302.app.widget.view.VerificationEditTextView
import com.newAi302.app.widget.view.VerifyCodeView

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/17
 * desc   : 忘记密码 手机号码
 * version: 1.0
 */
class ForgetPassWordPhoneActivity :
    MVPBaseActivity<IForgetPassWordView, ActivityForgetPasswordPhoneBinding, ForgetPassWordPresenter>(),
    IForgetPassWordView {

    private var mPhoneNumber = ""  //手机号码
    private var mVerifyCode = ""   //验证码
    private var mPassWord = ""
    private var mPassWordAgain = ""
    private var mImgVerifyCode = ""
    private var mRandomNum = ""
    private var countDownTimer: CountDownTimer? = null

    override fun createDataBinding(): ActivityForgetPasswordPhoneBinding {
        return ActivityForgetPasswordPhoneBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun createPresenter(): ForgetPassWordPresenter {
        return ForgetPassWordPresenter()
    }

    override fun initListener() {
        //地区选择
        mBinding?.rlPhoneLocation?.setSelectPhoneLocationListener(object :
            PhoneLocationView.SelectPhoneLocationListener {
            override fun selectPhone(phone: String) {
                mPhoneNumber = phone
            }
        })

        //验证码
        mBinding?.editVerifyCode?.addTextChangedListener {
            mVerifyCode = it.toString()
            mBinding?.tvCodeEmpty?.text =
                if (TextUtils.isEmpty(mVerifyCode)) resources.getString(R.string.empty_tip) else ""
        }
        //图形验证码
        mBinding?.verificationInput?.setVerifyCodeListener(object :
            VerificationEditTextView.VerifyCodeListener {
            override fun verifyCode(code: String) {
                mImgVerifyCode = code
            }
        })
        //刷新验证码
        mBinding?.verifyCodeView?.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }
        })

        //设置密码
        mBinding?.rlPassword?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWord = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })
        //再次输入密码
        mBinding?.rlPasswordAgain?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordAgain = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })

        mBinding?.tvVerifyCode?.setOnClickListener {
            //提交时判断手机号码不能为空
            val isPhone = mBinding?.rlPhoneLocation?.setPhoneNumberIsValid()
            if (isPhone == true) {
                mPresenter.getPhoneSmsRnyCode(mPhoneNumber, mImgVerifyCode, mRandomNum)
            }
        }

        //修改密码
        mBinding?.btnResetPass?.setOnClickListener {
            mBinding?.rlPassword?.setType(true)
            //输入的密码是否满足条件
            val mIsFillPassWork = if (!TextUtils.isEmpty(mPassWord)) {
                mBinding?.rlPassword?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPassword?.setPassWordIsEmpty()
                //false
            }

            //二次输入密码是否满足条件
            var mIsFillPassWorkAgain = false
            if (!TextUtils.isEmpty(mPassWordAgain)) {
                mIsFillPassWorkAgain = mBinding?.rlPasswordAgain?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPasswordAgain?.setPassWordIsEmpty()
            }


            Log.e("ceshi","forget>>>$mPassWord>>$mPassWordAgain")
            //两次输入的密码是否一致
            val mIsEquals = TextUtils.equals(mPassWord, mPassWordAgain)
            if (!mIsEquals) {
                mBinding?.rlPasswordAgain?.setInputPassWordIsEqual()
            }

            //提交时判断手机号码不能为空
            val isPhone = mBinding?.rlPhoneLocation?.setPhoneNumberIsValid()

            //验证码不能为空
            val isVerifyCode = !TextUtils.isEmpty(mBinding?.editVerifyCode?.text.toString())
            mBinding?.tvCodeEmpty?.text =
                if (TextUtils.isEmpty(mBinding?.editVerifyCode?.text.toString())) resources.getString(
                    R.string.empty_tip
                ) else ""

            //满足以上条件次才可以提交
            if (mIsFillPassWork!! && mIsFillPassWorkAgain && mIsEquals && isPhone == true && isVerifyCode) {
                mPresenter.resetLoginPassWordPhone(mPhoneNumber, mVerifyCode, mPassWord)
            }
        }

        mBinding?.btnBackLogin?.setOnClickListener {
            finish()
        }

        mBinding?.tvNoAccount?.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                ProxyActivityNavUtil.navToRegister(this@ForgetPassWordPhoneActivity)
                finish()
            }
        })
    }


    //设置发送手机验证码状态
    private fun setPhoneVerifyCodeState() {
        //发送手机验证码成功后，倒计时60秒，改变按钮文案，并且设置不可点击，
        countDownTimer = object : CountDownTimer(60000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding?.tvVerifyCode?.text =
                    resources.getString(R.string.verify_code_had_send)+" ${millisUntilFinished/1000}S"  //验证码已发送
                mBinding?.tvVerifyCode?.isClickable = false
                mBinding?.tvVerifyCode?.setTextColor(resources.getColor(R.color.un_selected))
            }

            override fun onFinish() {
                mBinding?.tvVerifyCode?.text =
                    resources.getString(R.string.get_verify_code)  //获取验证码
                mBinding?.tvVerifyCode?.isClickable = true
                mBinding?.tvVerifyCode?.setTextColor(resources.getColor(R.color.selected))
            }
        }.start()
    }

    override fun onPhoneCodeSuccess() {
        setPhoneVerifyCodeState()
    }

    override fun onFail() {

    }

    override fun onChangePhonePassWordSuccess() {
        ProxyActivityNavUtil.navLogin(this@ForgetPassWordPhoneActivity)
    }


    /*override fun createDataBinding(): ActivityForgetPasswordPhoneBinding {
        return ActivityForgetPasswordPhoneBinding.inflate(layoutInflater)
    }

    override fun initView() {
//         TODO("Not yet implemented")
    }

    override fun initListener() {
//         TODO("Not yet implemented")
    }*/
}