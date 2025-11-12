package com.newAi302.app.widget.view

import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutPhoneRegisterBinding
import com.newAi302.app.utils.StringUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.listener.IClickListener

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   :
 * version: 1.0
 */
class RegisterPhoneInputView(context: Context, attrs: AttributeSet? = null) :
    LinearLayoutCompat(context, attrs) {

    private var mUserName = ""          //用户名
    private var mInviteCode = ""        //邀请码
    private var mPhoneNumber = ""       //手机号码
    private var mVerifyCode = ""        //手机验证码
    private var mPassWord = ""          //密码
    private var mPassWordAgain = ""     //再次输入密码
    private var mImgVerifyCode = ""   //图形验证码
    private var mRandomNum = ""    //图形验证码生产的随机数
    private var mListener: RegisterPhoneInputListener? = null
    private var countDownTimer: CountDownTimer? = null

    val mBinding: LayoutPhoneRegisterBinding by lazy {
        LayoutPhoneRegisterBinding.bind(View.inflate(context, R.layout.layout_phone_register_new, this))
    }

    init {
        initListener()
    }

    private fun initListener() {

        mPhoneNumber =
            if (!TextUtils.isEmpty(WearData.getInstance().phoneCode)) WearData.getInstance().phoneCode else ""
        //用户名
        mBinding.editUserName.addTextChangedListener {
            mUserName = it.toString()
            mBinding.tvEmptyTipUseName.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.user_name_empty) else ""
        }

        //邀请码
        mBinding.editInviteCode.addTextChangedListener {
            mInviteCode = it.toString()
        }
        //地区选择
        mBinding.rlPhoneLocation.setSelectPhoneLocationListener(object :
            PhoneLocationView.SelectPhoneLocationListener {
            override fun selectPhone(phone: String) {
                mPhoneNumber = phone
            }
        })

        mBinding.verificationInput.setVerifyCodeListener(object :
            VerificationEditResTextView.VerifyCodeListener {
            override fun verifyCode(code: String) {
                mImgVerifyCode = code
            }
        })
        //刷新验证码
        mBinding.verifyCodeView.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }

        })

        //验证码
        mBinding.editVerifyCode.addTextChangedListener {
            mVerifyCode = it.toString()
            mBinding.tvCodeEmpty.text =
                if (TextUtils.isEmpty(mVerifyCode)) resources.getString(R.string.empty_tip) else ""
        }
        //获取验证码
        mBinding.tvVerifyCode.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    Log.e("ceshi","phone register:$mPhoneNumber")
                    mListener?.getPhoneSmsRnyCode(mPhoneNumber, mImgVerifyCode, mRandomNum)
                }
            }
        })

        //设置密码
        mBinding.rlPassword.setLoginPassWordListener(object :
            PassWorkResEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWord = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })
        //再次输入密码
        mBinding.rlPasswordAgain.setLoginPassWordListener(object :
            PassWorkResEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordAgain = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }

        })
        //去注册
        mBinding.btnRegisterPhone.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                mBinding.rlPassword.setType(true)
                val mIsFillName = !TextUtils.isEmpty(mUserName) //满足名字不为空
                if (TextUtils.isEmpty(mUserName)) {
                    mBinding.tvEmptyTipUseName.text = resources.getString(R.string.user_name_empty)
                }

                //输入的密码是否满足条件
                val mIsFillPassWork: Boolean = if (!TextUtils.isEmpty(mPassWord)) {
                    mBinding.rlPassword.getIsPassWorkTerm()
                } else {
                    if(WearData.getInstance().isRememberPassWordPhone){
                        mPassWord = WearData.getInstance().loginPhonePassWord
                        mPassWordAgain = WearData.getInstance().loginPhonePassWord
                    }
                    mBinding.rlPassword.setPassWordIsEmpty()
                    //false
                }

                //二次输入密码是否满足条件
                val mIsFillPassWorkAgain: Boolean = if (!TextUtils.isEmpty(mPassWordAgain)) {
                    mBinding.rlPasswordAgain.getIsPassWorkTerm()
                } else {
                    mBinding.rlPasswordAgain.setPassWordIsEmpty()
                    //false
                }

                //两次输入的密码是否一致
                val mIsEquals = TextUtils.equals(mPassWord, mPassWordAgain)
                if (!mIsEquals) {
                    mBinding.rlPasswordAgain.setInputPassWordIsEqual()
                }

                //提交时判断手机号码不能为空
                val isPhone = mBinding.rlPhoneLocation.setPhoneNumberIsValid()

                //验证码判断
                val isCode = mBinding.verificationInput.setVerifyCodeEmpty()

                //验证码不能为空
                val isVerifyCode = !TextUtils.isEmpty(mBinding.editVerifyCode.text.toString())
                mBinding.tvCodeEmpty.text =
                    if (TextUtils.isEmpty(mBinding.editVerifyCode.text.toString())) resources.getString(
                        R.string.empty_tip
                    ) else ""

                if (mIsFillName && mIsFillPassWork && mIsFillPassWorkAgain && mIsEquals && isPhone && isCode && isVerifyCode) {
                    if (mListener != null) {
                        mListener?.goRegister(
                            mUserName,
                            mInviteCode,
                            mPhoneNumber,
                            mVerifyCode,
                            mPassWord,
                            mPassWordAgain
                        )
                    }
                }
            }
        })
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


    interface RegisterPhoneInputListener {
        fun goRegister(
            userName: String,
            inviteCode: String,
            phoneNumber: String,
            verifyCode: String,
            passWord: String,
            passWordAgain: String
        )

        fun getPhoneSmsRnyCode(phoneNumber: String, imgVerifyCode: String, randomNum: String)
    }

    fun setRegisterPhoneInputListener(listener: RegisterPhoneInputListener) {
        this.mListener = listener
    }
}