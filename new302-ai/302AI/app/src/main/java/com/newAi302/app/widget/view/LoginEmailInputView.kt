package com.newAi302.app.widget.view

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutPasswordLoginBinding
import com.newAi302.app.utils.StringUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.utils.CommonEnum
import com.newAi302.app.widget.utils.CommonUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 邮箱登录 用户操作 view
 * version: 1.0
 */
class LoginEmailInputView(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private var mEmail = ""        //邮箱
    private var mPassWord = ""     //密码
    private var mVerifyCode = ""   //输入的验证码
    private var mRandomNum = ""    //图片验证码生产的随机数
    private var mListener: LoginEmailListener? = null
    //private lateinit var mBinding: LayoutPasswordLoginBinding

    val mBinding: LayoutPasswordLoginBinding by lazy {
        //Log.e("ceshi","0来来来来来")
        LayoutPasswordLoginBinding.bind(View.inflate(context, R.layout.layout_password_login_new, this))
    }

//    val mBinding: LayoutPasswordLoginBinding by lazy {
//        Log.e("ceshi", "开始初始化 mBinding：加载 layout_password_login")
//        val rootView = try {
//            View.inflate(context, R.layout.layout_password_login, null)
//        } catch (e: Exception) {
//            Log.e("ceshi", "加载布局失败：${e.message}", e)
//            throw e // 抛出异常，让日志显示具体错误
//        }
//        Log.e("ceshi", "布局加载成功，根 View 类型：${rootView.javaClass.simpleName}")
//
//        this.addView(rootView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//
//        val binding = try {
//            LayoutPasswordLoginBinding.bind(rootView)
//        } catch (e: Exception) {
//            Log.e("ceshi", "Binding 绑定失败：${e.message}", e)
//            throw e
//        }
//        Log.e("ceshi", "mBinding 初始化成功")
//        binding
//    }

    init {
        //Log.e("ceshi","来来来来来")
        initView()
        initListener()
    }

    private fun initView() {
        val deviceModel = Build.MODEL
        android.util.Log.e("ceshi","闪退:$deviceModel")
        android.util.Log.e("ceshi","0闪退:${mBinding.rlLoginGoogle==null}")
        Log.e("ceshi", "rlLoginGoogle: ${mBinding.rlLoginGoogle}")
        if (deviceModel == "2411DRN47C"){
            mBinding.rlLoginGoogle.visibility = View.GONE
        }else{
            //mBinding.rlLoginGoogle.visibility = View.VISIBLE
        }
        // 检查 WearData 单例与 checkbox 是否为 null
        Log.e("ceshi", "WearData: ${WearData.getInstance()}, checkbox: ${mBinding.checkbox}")
        mBinding.checkbox.isChecked = WearData.getInstance().isRememberPassWord
        mBinding.checkbox1.isChecked = WearData.getInstance().isReadAndAgreeUTsAndPAs
        // 检查 editEmail 是否为 null
        Log.e("ceshi", "editEmail: ${mBinding.editEmail}")
        mEmail =
            if (!TextUtils.isEmpty(WearData.getInstance().emailCode)) WearData.getInstance().emailCode else ""
        mBinding.editEmail.setText(mEmail)
        // 检查 rlPassword 是否为 null
        Log.e("ceshi", "rlPassword: ${mBinding.rlPassword}")
        mBinding.rlPassword.setCurrentLoginType(CommonEnum.PassWordType.EMAIL)
    }


    private fun initListener() {
        //邮箱
        mBinding.editEmail.addTextChangedListener {
            mEmail = it.toString()
            mBinding.tvEmptyTipEmail.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.email_empty) else ""
        }

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

        //刷新验证码
        mBinding.verifyCodeView.setVerifyCodeViewListener(object :
            VerifyCodeView.VerifyCodeViewListener {
            override fun randomStr(randomNum: String) {
                mRandomNum = randomNum
            }
        })

        //记住密码
        mBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            WearData.getInstance().saveRememberPassword(isChecked)
        }

        //忘记密码
        mBinding.tvForgetPassword.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    mListener?.onForgetPassWord()
                }
            }
        })

        //登录
        mBinding.btnLogin.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                mBinding.rlPassword.setType(false)
                //判断邮箱不为空
                var mIsEmailEmpty = false
                if (!TextUtils.isEmpty(mEmail.trim())) {
                    //判断邮箱是否合法
                    var isValidEmail = CommonUtils.isValidEmail(mEmail)
                    isValidEmail = true
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
                val mIsFillPassWork: Boolean = if (!TextUtils.isEmpty(mPassWord.trim())) {
                    mBinding.rlPassword.getIsPassWorkTerm()
                } else {
                    if (WearData.getInstance().isRememberPassWord) {
                        mPassWord = WearData.getInstance().emailPassWord
                    }else{
                        mPassWord =
                            if (!TextUtils.isEmpty(WearData.getInstance().emailPassWord)) WearData.getInstance().emailPassWord else ""
                    }
                    mBinding.rlPassword.setPassWordIsEmpty()
                    //false
                }

                //验证码判断
                val isCode = mBinding.verificationInput.setVerifyCodeEmpty()

                if (mIsEmailEmpty && mIsFillPassWork && isCode) {
                    if (mListener != null) {
                        android.util.Log.e("ceshi",">>>${mPassWord.trim()}")
                        val isAgree = WearData.getInstance().isReadAndAgreeUTsAndPAs
                        if (true) {
                            mListener?.onLogin(
                                mEmail.trim(),
                                mPassWord.trim(),
                                mVerifyCode.trim(),
                                mRandomNum
                            )
                        }else {
                            ToastUtils.showShort(R.string.no_agree_error)
                        }


                    }
                }
            }
        })

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

        //google登录
        mBinding.rlLoginGoogle.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                if (mListener != null) {
                    val isAgree = WearData.getInstance().isReadAndAgreeUTsAndPAs
                    if (true) {
                        mListener?.onGoogleLogin()
                    }else {
                        ToastUtils.showShort(R.string.no_agree_error)
                    }
                }
            }
        })
    }

    interface LoginEmailListener {
        fun onForgetPassWord()

        fun onLogin(email: String, passWord: String, verifyCode: String, randomNum: String)

        fun onGoogleLogin()

        fun readMessage(type:String)

    }

    fun setLoginEmailListener(listener: LoginEmailListener) {
        this.mListener = listener
    }

}