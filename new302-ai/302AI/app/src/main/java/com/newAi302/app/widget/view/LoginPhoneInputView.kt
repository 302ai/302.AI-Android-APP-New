package com.newAi302.app.widget.view

import android.content.Context
import android.os.CountDownTimer

import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutPhoneLoginBinding
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.utils.CommonEnum

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 手机登录用户操作 view
 * version: 1.0
 */
class LoginPhoneInputView(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private var mPhoneNumber = ""  //手机号码
    private var mPassWord = ""     //密码
    private var mVerifyCode = ""   //验证码
    private var mRandomNum = ""     //图形验证码生成的随机数
    private var mListener: LoginPhoneInputListener? = null
    private var countDownTimer: CountDownTimer? = null

    private var mVerifyPhoneCode = ""        //手机验证码

    val mBinding: LayoutPhoneLoginBinding by lazy {
        LayoutPhoneLoginBinding.bind(View.inflate(context, R.layout.layout_phone_login_new, this))
    }

    init {
        mBinding.checkbox.isChecked = WearData.getInstance().isRememberPassWordPhone
        mBinding.checkbox1.isChecked = WearData.getInstance().isReadAndAgreeUTsAndPAs
        mBinding.rlPassword.setCurrentLoginType(CommonEnum.PassWordType.PHONE)

        initListener()
    }

    private fun initListener() {
        mPhoneNumber =
            if (!TextUtils.isEmpty(WearData.getInstance().phoneCode)) WearData.getInstance().phoneCode else ""


        //手机号码
        mBinding.rlPhoneLocation.setSelectPhoneLocationListener(object :
            PhoneLocationView.SelectPhoneLocationListener {
            override fun selectPhone(phone: String) {
                mPhoneNumber = phone
            }
        })

        //密码
        mBinding.rlPassword.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWord = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })

        //验证码
        mBinding.verificationInput.setVerifyCodeListener(object :
            VerificationEditTextView.VerifyCodeListener {
            override fun verifyCode(code: String) {
                mVerifyCode = code
            }
        })

        //验证码
        mBinding.editVerifyCode.addTextChangedListener {
            mVerifyPhoneCode = it.toString()
            mBinding.tvCodeEmpty.text =
                if (TextUtils.isEmpty(mVerifyPhoneCode)) resources.getString(R.string.empty_tip) else ""
        }

        //登录
        mBinding.btnLogin.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                mBinding.rlPassword.setType(true)
                //提交时判断手机号码不能为空
                val isPhone = mBinding.rlPhoneLocation.setPhoneNumberIsValid()

                //输入的密码是否满足条件
                val mIsFillPassWork: Boolean = if (!TextUtils.isEmpty(mPassWord)) {
                    mBinding.rlPassword.getIsPassWorkTerm()
                } else {
                    if (WearData.getInstance().isRememberPassWordPhone) {
                        mPassWord = WearData.getInstance().loginPhonePassWord
                    }else{
                        mPassWord =
                            if (!TextUtils.isEmpty(WearData.getInstance().loginPhonePassWord)) WearData.getInstance().loginPhonePassWord else ""
                    }
                    mBinding.rlPassword.setPassWordIsEmpty()
                    //false
                }
                //验证码判断
                val isCode = mBinding.verificationInput.setVerifyCodeEmpty()

                if (isPhone && mIsFillPassWork && isCode) {
                    if (mListener != null) {
                        android.util.Log.e("ceshi","phone_number:${mPhoneNumber}，，sms_code：$mVerifyPhoneCode，，captcha$mVerifyCode")
                        /*if (WearData.getInstance().isRememberPassWordPhone) {
                            mListener?.goLogin(WearData.getInstance().phoneCode, WearData.getInstance().loginPhonePassWord, mVerifyCode, mRandomNum)
                        }else{
                            mListener?.goLogin(mPhoneNumber, mPassWord, mVerifyCode, mRandomNum)
                        }*/
                        val isAgree = WearData.getInstance().isReadAndAgreeUTsAndPAs
                        if (true) {
                            //mListener?.goLogin(mPhoneNumber, mPassWord, mVerifyCode, mRandomNum)
                            mListener?.goLoginSms(mPhoneNumber, mVerifyPhoneCode, mVerifyCode, mRandomNum)
                        }else {
                            ToastUtils.showShort(R.string.no_agree_error)
                        }


                    }
                }
            }
        })

        //记住密码
        mBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            WearData.getInstance().saveRememberPasswordPhone(isChecked)
        }

        mBinding.checkbox1.setOnCheckedChangeListener { buttonView, isChecked ->
            WearData.getInstance().saveReadAndAgreeUTsAndPAs(isChecked)
        }

        mBinding.tvUserTerms.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    mListener?.readMessage("UTs")
                }
            }
        })

        mBinding.tvPrivacyAgreement.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    mListener?.readMessage("PAs")
                }
            }
        })

        //忘记密码
        mBinding.tvForgetPassword.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    mListener?.forgetPassWord()
                }
            }
        })

        //图形验证码
        mBinding.verifyCodeView.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }
        })

        //获取验证码
        mBinding.tvVerifyCode.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    Log.e("ceshi","phone register:$mPhoneNumber")
                    mListener?.getPhoneSmsRnyCode(mPhoneNumber, mVerifyCode, mRandomNum)
                }
            }
        })

    }


    interface LoginPhoneInputListener {
        fun forgetPassWord()

        fun goLogin(phone: String, passWord: String, verifyCode: String, randomNum: String)

        fun goLoginSms(phone: String, passWord: String, verifyCode: String, randomNum: String)

        fun readMessage(type:String)

        fun getPhoneSmsRnyCode(phoneNumber: String, imgVerifyCode: String, randomNum: String)

    }

    fun setLoginPhoneInputListener(listener: LoginPhoneInputListener) {
        mListener = listener
    }

    //设置发送手机验证码状态
    fun setPhoneVerifyCodeState() {
        //发送手机验证码成功后，倒计时60秒，改变按钮文案，并且设置不可点击，
        countDownTimer = object : CountDownTimer(60000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvVerifyCode.text =
                    resources.getString(R.string.verify_code_had_send)+" ${millisUntilFinished/1000}S"  //验证码已发送
                mBinding.tvVerifyCode.isClickable = false
                mBinding.tvVerifyCode.setTextColor(resources.getColor(R.color.un_selected))
            }

            override fun onFinish() {
                mBinding.tvVerifyCode.text =
                    resources.getString(R.string.get_verify_code)  //获取验证码
                mBinding.tvVerifyCode.isClickable = true
                mBinding.tvVerifyCode.setTextColor(resources.getColor(R.color.selected))
            }
        }.start()
    }

}