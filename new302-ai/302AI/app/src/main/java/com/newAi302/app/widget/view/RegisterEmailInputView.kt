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
import com.newAi302.app.bean.EmailCodeBeanRes
import com.newAi302.app.databinding.LayoutEmailRegisterBinding
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.LoginModel
import com.newAi302.app.utils.StringUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.utils.CommonUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 邮箱注册 用户操作 view
 * version: 1.0
 */
class RegisterEmailInputView(context: Context, attrs: AttributeSet? = null) :
    LinearLayoutCompat(context, attrs) {

    private var mUserName = ""     //用户名
    private var mInviteCode = ""   //邀请码
    private var mEmail = ""        //邮箱
    private var mPassWord = ""     //密码
    private var mPassWordAgain = "" //再次密码
    private var mVerifyCode = ""   //验证码
    private var mEmailVerifyCode = "" //邮箱验证码
    private var mRandomNum = ""    //图形验证码生产的随机数
    private var mListener: RegisterEmailInputListener? = null
    private var countDownTimer: CountDownTimer? = null

    val mBinding: LayoutEmailRegisterBinding by lazy {
        LayoutEmailRegisterBinding.bind(View.inflate(context, R.layout.layout_email_register_new, this))
    }

    init {
        initListener()
    }

    private fun initListener() {
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
        //邮箱
        mBinding.editEmail.addTextChangedListener {
            mEmail = it.toString()
            mBinding.tvEmptyTipEmail.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.email_empty) else ""
        }
        //邮箱验证码
        mBinding.editEmailCode.addTextChangedListener {
            mEmailVerifyCode = it.toString()
            mBinding.tvEmptyTipEmailCode.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.email_code_empty) else ""
        }
        //获取邮箱验证
        mBinding.tvEmailVerifyCode.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                val isValidEmail = CommonUtils.isValidEmail(mEmail)
                if(mEmail == "" || mVerifyCode == "" || !isValidEmail){
                    ToastUtils.showShort(R.string.email_vc_error)
                }else{
                    sendEmailCode()
                }

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

        //再次输入密码
        mBinding.rlPasswordAgain.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordAgain = password
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
        //刷新验证码
        mBinding.verifyCodeView.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }

        })
        //去注册
        mBinding.btnRegister.setOnClickListener {
            mBinding.rlPassword.setType(false)
            val mIsFillName = !TextUtils.isEmpty(mUserName) //满足名字不为空
            if (TextUtils.isEmpty(mUserName)) {
                mBinding.tvEmptyTipUseName.text = resources.getString(R.string.user_name_empty)
                return@setOnClickListener
            }

            //判断邮箱不为空
            var mIsEmailEmpty = false
            if (!TextUtils.isEmpty(mEmail.trim())) {
                //判断邮箱是否合法
                val isValidEmail = CommonUtils.isValidEmail(mEmail)
                if (isValidEmail) {
                    mIsEmailEmpty = true
                    mBinding.tvEmptyTipEmail.text = ""
                } else {
                    mBinding.tvEmptyTipEmail.text = resources.getString(R.string.email_illegal)
                }
            } else {
                mBinding.tvEmptyTipEmail.text = resources.getString(R.string.email_empty)
            }

            //输入的密码是否满足条件
            val mIsFillPassWork: Boolean = if (!TextUtils.isEmpty(mPassWord)) {
                mBinding.rlPassword.getIsPassWorkTerm()
            } else {
                if (WearData.getInstance().isRememberPassWord){
                    mPassWord = WearData.getInstance().emailPassWord
                    mPassWordAgain = WearData.getInstance().emailPassWord
                }
                mBinding.rlPassword.setPassWordIsEmpty()
                //false
            }
            Log.e("ceshi","${mPassWord}>>${mPassWordAgain}")

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

            //验证码判断
            val isCode = mBinding.verificationInput.setVerifyCodeEmpty()

            //邮箱验证码判断
            val mIsEmailCode: Boolean
            if (TextUtils.isEmpty(mEmailVerifyCode)) {
                mBinding.tvEmptyTipEmailCode.text = resources.getString(R.string.email_code_empty)
                mIsEmailCode = false
            } else {
                mBinding.tvEmptyTipEmailCode.text = ""
                mIsEmailCode = true
            }


            if (mIsFillName && mIsEmailEmpty && mIsFillPassWork && mIsFillPassWorkAgain && isCode && mIsEmailCode) {
                if (mListener != null) {
                    mListener?.goRegister(
                        mUserName.trim(),
                        mInviteCode.trim(),
                        mEmail.trim(),
                        mPassWord.trim(),
                        mVerifyCode.trim(),
                        mRandomNum,
                        mEmailVerifyCode.trim()
                    )
                }
            }
        }
        //google注册
        mBinding.rlRegisterGoogle.setOnClickListener {
            if (mListener != null) {
                mListener?.googleRegister()
            }
        }
    }

    //发送邮箱验证
    private fun sendEmailCode() {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = mEmail
        hashMap["captcha"] = mVerifyCode
        hashMap["code"] = mRandomNum
        LoginModel.registerEmailCode(hashMap,
            object : RequestCallback<BaseResponse<EmailCodeBeanRes>>() {
                override fun onSuccess(data: BaseResponse<EmailCodeBeanRes>?) {
                    setPhoneVerifyCodeState()
                }

                override fun onError(e: NetException?) {
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

    //设置发送手机验证码状态
    private fun setPhoneVerifyCodeState() {
        //发送手机验证码成功后，倒计时60秒，改变按钮文案，并且设置不可点击，
        countDownTimer = object : CountDownTimer(60000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvEmailVerifyCode.text =
                    resources.getString(R.string.verify_code_had_send)+" ${millisUntilFinished/1000}S"  //验证码已发送
                mBinding.tvEmailVerifyCode.isClickable = false
                mBinding.tvEmailVerifyCode.setTextColor(resources.getColor(R.color.un_selected))
            }

            override fun onFinish() {
                mBinding.tvEmailVerifyCode.text =
                    resources.getString(R.string.get_email_verify_code)  //获取验证码
                mBinding.tvEmailVerifyCode.isClickable = true
                mBinding.tvEmailVerifyCode.setTextColor(resources.getColor(R.color.selected))
            }
        }.start()
    }

    interface RegisterEmailInputListener {
        fun goRegister(
            userName: String,
            inviteCode: String,
            email: String,
            passWord: String,
            verifyCode: String,
            randomNum: String,
            emailCode: String,
        )

        fun googleRegister()
    }

    fun setRegisterEmailInputListener(listener: RegisterEmailInputListener) {
        mListener = listener
    }
}