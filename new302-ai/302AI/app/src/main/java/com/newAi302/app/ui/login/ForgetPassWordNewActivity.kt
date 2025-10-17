package com.newAi302.app.ui.login

import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.base.mvp.MVPBaseActivity
import com.newAi302.app.bean.EmailCodeBeanRes
import com.newAi302.app.databinding.ActivityForgetPasswordNewBinding
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.LoginModel

import com.newAi302.app.ui.presenter.ForgetPassWordPresenter
import com.newAi302.app.ui.view.IForgetPassWordView
import com.newAi302.app.utils.StringUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.ProxyActivityNavUtil
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.utils.CommonUtils
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

    private var mEmail = ""        //邮箱
    private var mEmailVerifyCode = ""   //邮箱验证码
    private var mPassWordEmail = ""
    private var mPassWordAgainEmail = ""

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
        /**邮箱验证*/
        //邮箱
        mBinding?.editEmail?.addTextChangedListener {
            mEmail = it.toString()
            mBinding?.tvEmptyTipEmail?.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.email_empty) else ""
        }

        //邮箱验证码
        mBinding?.editEmailCode?.addTextChangedListener {
            mEmailVerifyCode = it.toString()
            mBinding?.tvEmptyTipEmailCode?.text =
                if (StringUtils.isEmpty(it.toString())) resources.getString(R.string.email_code_empty) else ""
        }
        //获取邮箱验证
        mBinding?.tvEmailVerifyCode?.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                val isValidEmail = CommonUtils.isValidEmail(mEmail)
                if(mEmail == ""|| !isValidEmail){
                    ToastUtils.showShort(R.string.email_vc_error)
                }else{
                    sendEmailCode()
                }

            }
        })

        //邮箱密码
        mBinding?.rlPasswordEmail?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordEmail = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })

        //邮箱再次密码
        mBinding?.rlPasswordAgainEmail?.setLoginPassWordListener(object :
            PassWorkEditView.LoginPasswordListener {
            override fun inputContent(password: String) {
                mPassWordAgainEmail = password
            }

            override fun eyeSwitch(isSwitch: Boolean) {

            }

            override fun cleanInput() {

            }
        })

        //修改密码
        mBinding?.btnResetPassEmail?.setOnClickListener {
            mBinding?.rlPasswordEmail?.setType(true)
            //输入的密码是否满足条件
            val mIsFillPassWorkEmail = if (!TextUtils.isEmpty(mPassWordEmail)) {
                mBinding?.rlPasswordEmail?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPasswordEmail?.setPassWordIsEmpty()
                //false
            }

            //二次输入密码是否满足条件
            var mIsFillPassWorkAgain = false
            if (!TextUtils.isEmpty(mPassWordAgainEmail)) {
                mIsFillPassWorkAgain = mBinding?.rlPasswordAgainEmail?.getIsPassWorkTerm() == true
            } else {
                mBinding?.rlPasswordAgainEmail?.setPassWordIsEmpty()
            }


            Log.e("ceshi","forget>>>$mPassWordEmail>>$mPassWordAgainEmail")
            //两次输入的密码是否一致
            val mIsEquals = TextUtils.equals(mPassWordEmail, mPassWordAgainEmail)
            if (!mIsEquals) {
                mBinding?.rlPasswordAgainEmail?.setInputPassWordIsEqual()
            }

            //判断邮箱不为空
            var mIsEmailEmpty = false
            if (!TextUtils.isEmpty(mEmail.trim())) {
                //判断邮箱是否合法
                val isValidEmail = CommonUtils.isValidEmail(mEmail)
                if (isValidEmail) {
                    mIsEmailEmpty = true
                    mBinding?.tvEmptyTipEmail?.text = ""
                } else {
                    mBinding?.tvEmptyTipEmail?.text = resources.getString(R.string.email_illegal)
                }
            } else {
                mBinding?.tvEmptyTipEmail?.text = resources.getString(R.string.email_empty)
            }


            //邮箱验证码判断
            val mIsEmailCode: Boolean
            if (TextUtils.isEmpty(mEmailVerifyCode)) {
                mBinding?.tvEmptyTipEmailCode?.text = resources.getString(R.string.email_code_empty)
                mIsEmailCode = false
            } else {
                mBinding?.tvEmptyTipEmailCode?.text = ""
                mIsEmailCode = true
            }


            //满足以上条件次才可以提交
            if (mIsFillPassWorkEmail!! && mIsFillPassWorkAgain && mIsEquals && mIsEmailCode == true && mIsEmailEmpty) {
                Log.e("ceshi","邮箱修改密码：email：$mEmail）email_code：$mEmailVerifyCode）password：$mPassWordEmail）confirmPassword：$mPassWordAgainEmail")
                mPresenter.resetLoginPassWordEmailNew(mEmail, mEmailVerifyCode, mPassWordEmail,mPassWordAgainEmail)
            }
        }

        mBinding?.btnBackLoginEmail?.setOnClickListener {
            finish()
        }

        mBinding?.registerEmailTv?.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                ProxyActivityNavUtil.navToRegister(this@ForgetPassWordNewActivity)
                finish()
            }
        })



    }

    override fun initView() {
        setLoginSwitch(false)
        mEmail =
            if (!TextUtils.isEmpty(WearData.getInstance().emailCode)) WearData.getInstance().emailCode else ""
        mBinding?.editEmail?.setText(mEmail)

    }

    override fun createPresenter(): ForgetPassWordPresenter {
        return ForgetPassWordPresenter()
    }

    override fun onPhoneCodeSuccess() {
        finish()
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

    //发送邮箱验证
    private fun sendEmailCode() {
        val hashMap: MutableMap<String, Any> = HashMap()
        hashMap["email"] = mEmail
//        hashMap["captcha"] = mVerifyCode
//        hashMap["code"] = mRandomNum
        LoginModel.registerEmailCodeNew(hashMap,
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
                mBinding?.tvEmailVerifyCode?.setTextColor(resources.getColor(R.color.white))
                mBinding?.tvEmailVerifyCode?.text =
                    resources.getString(R.string.verify_code_had_send)+" ${millisUntilFinished/1000}S"  //验证码已发送
                mBinding?.tvEmailVerifyCode?.isClickable = false
                mBinding?.tvEmailVerifyCode?.setTextColor(resources.getColor(R.color.un_selected))
            }

            override fun onFinish() {
                mBinding?.tvEmailVerifyCode?.text =
                    resources.getString(R.string.get_email_verify_code)  //获取验证码
                mBinding?.tvEmailVerifyCode?.isClickable = true
                mBinding?.tvEmailVerifyCode?.setTextColor(resources.getColor(R.color.white))
            }
        }.start()
    }

}