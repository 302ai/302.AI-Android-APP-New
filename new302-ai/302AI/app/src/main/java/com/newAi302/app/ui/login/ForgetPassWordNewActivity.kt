package com.newAi302.app.ui.login

import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.base.mvp.MVPBaseActivity
import com.newAi302.app.databinding.ActivityForgetPasswordNewBinding

import com.newAi302.app.ui.presenter.ForgetPassWordPresenter
import com.newAi302.app.ui.view.IForgetPassWordView
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.ProxyActivityNavUtil
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.view.PassWorkEditView
import com.newAi302.app.widget.view.PhoneLocationView
import com.newAi302.app.widget.view.VerificationEditTextView
import com.newAi302.app.widget.view.VerifyCodeView

class ForgetPassWordNewActivity :
    MVPBaseActivity<IForgetPassWordView, ActivityForgetPasswordNewBinding, ForgetPassWordPresenter>(),
    IForgetPassWordView {

    private var mPhoneNumber = ""  //手机号码
    private var mVerifyCode = ""   //验证码
    private var mPassWordPhone = ""
    private var mPassWordAgainPhone = ""
    private var mImgVerifyCode = ""
    private var mRandomNum = ""
    private var countDownTimer: CountDownTimer? = null
    private var mVerifyPhoneCode = ""        //手机验证码

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

        mPhoneNumber =
            if (!TextUtils.isEmpty(WearData.getInstance().phoneCode)) WearData.getInstance().phoneCode else ""


        /**手机修改密码*/
        //手机号码
        mBinding?.rlPhoneLocation?.setSelectPhoneLocationListener(object :
            PhoneLocationView.SelectPhoneLocationListener {
            override fun selectPhone(phone: String) {
                mPhoneNumber = phone
            }
        })

        //图形验证码
        mBinding?.verificationInput?.setVerifyCodeListener(object :
            VerificationEditTextView.VerifyCodeListener {
            override fun verifyCode(code: String) {
                mVerifyCode = code
            }
        })

        //刷新验证码
        mBinding?.verifyCodeView?.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }
        })

        //手机验证码
        mBinding?.editVerifyCode?.addTextChangedListener {
            mVerifyPhoneCode = it.toString()
            mBinding?.tvCodeEmpty?.text =
                if (TextUtils.isEmpty(mVerifyPhoneCode)) resources.getString(R.string.empty_tip) else ""
        }

        //手机密码
        mBinding?.rlPassword?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordPhone = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })

        //手机再次密码
        mBinding?.rlPasswordAgain?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordAgainPhone = password
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
                mPresenter.getPhoneSmsRnyCode(mPhoneNumber, mVerifyCode, mRandomNum)
            }
        }

        //修改密码
        mBinding?.btnResetPassPhone?.setOnClickListener {
            mBinding?.rlPassword?.setType(true)
            //输入的密码是否满足条件
            val mIsFillPassWork = if (!TextUtils.isEmpty(mPassWordPhone)) {
                mBinding?.rlPassword?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPassword?.setPassWordIsEmpty()
                //false
            }

            //二次输入密码是否满足条件
            var mIsFillPassWorkAgain = false
            if (!TextUtils.isEmpty(mPassWordAgainPhone)) {
                mIsFillPassWorkAgain = mBinding?.rlPasswordAgain?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPasswordAgain?.setPassWordIsEmpty()
            }


            Log.e("ceshi","forget>>>$mPassWordPhone>>$mPassWordAgainPhone")
            //两次输入的密码是否一致
            val mIsEquals = TextUtils.equals(mPassWordPhone, mPassWordAgainPhone)
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
                mPresenter.resetLoginPassWordPhone(mPhoneNumber, mVerifyCode, mPassWordPhone)
            }
        }

        mBinding?.btnBackLoginPhone?.setOnClickListener {
            finish()
        }

        mBinding?.backMainTv?.setOnClickListener {
            finish()
        }

        mBinding?.smsRegisterTv?.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                ProxyActivityNavUtil.navToRegister(this@ForgetPassWordNewActivity)
                finish()
            }
        })


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
        finish()
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