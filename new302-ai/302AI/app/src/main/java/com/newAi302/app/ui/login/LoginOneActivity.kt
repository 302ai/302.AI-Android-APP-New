package com.newAi302.app.ui.login

import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.R
import com.newAi302.app.base.mvp.MVPBaseActivity
import com.newAi302.app.bean.LoginBean
import com.newAi302.app.databinding.ActivityLoginBinding
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.presenter.LoginPresenter
import com.newAi302.app.ui.thirdloginsdk.ITLoginListener
import com.newAi302.app.ui.thirdloginsdk.TLoginBean
import com.newAi302.app.ui.thirdloginsdk.TLoginMgr
import com.newAi302.app.ui.view.ILoginView
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.utils.dataStore
import com.newAi302.app.utils.keyGoogleLogin
import com.newAi302.app.widget.ProxyActivityNavUtil
import com.newAi302.app.widget.dialog.BottomSheetDialog
import com.newAi302.app.widget.view.LoginPhoneInputView
import com.newAi302.app.databinding.ActivityNewLoginBinding
import com.newAi302.app.databinding.ActivityNewLoginNewBinding
import com.newAi302.app.widget.view.LoginEmailInputView
import kotlinx.coroutines.launch

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/8
 * desc   : 登录页
 * version: 1.0
 */
class LoginOneActivity : MVPBaseActivity<ILoginView, ActivityNewLoginNewBinding, LoginPresenter>(),
    ILoginView {

    private var mPassWordStr: String = "" //输入的密码
    private var mBottomSheetDialog: BottomSheetDialog? = null

    override fun createDataBinding(): ActivityNewLoginNewBinding {
        return ActivityNewLoginNewBinding.inflate(layoutInflater)//ActivityNewLoginBinding//ActivityNewLoginNewBinding

    }

    override fun initView() {
//        mBinding?.incEmailLogin?.rlPassword?.setPassWorkHint(this.resources.getString(R.string.email_password_again))
        setLoginSwitch(false)
    }

    override fun createPresenter(): LoginPresenter {
        return LoginPresenter()
    }

    override fun initListener() {
        mBinding?.llEmailLogin?.setOnClickListener {
            setLoginSwitch(false)
        }

        mBinding?.llPhoneLogin?.setOnClickListener {
            setLoginSwitch(true)
        }

        //登录按钮 - email
        mBinding?.emailInput?.setLoginEmailListener(object :
            LoginEmailInputView.LoginEmailListener {
            override fun onForgetPassWord() {
                goForgetPassWordAct()
            }

            override fun onLogin(
                email: String,
                passWord: String,
                verifyCode: String,
                randomNum: String
            ) {
                loginAll(email, passWord, verifyCode, randomNum)
            }


            override fun onGoogleLogin() {
                loginGoogle()
            }

            override fun readMessage(type: String) {
                mBottomSheetDialog = BottomSheetDialog()
                mBottomSheetDialog?.setMessageType(type)
                mBottomSheetDialog?.show(supportFragmentManager, "BottomSheetDialog")
            }

        })

        //登录按钮 - phone
        mBinding?.phoneInput?.setLoginPhoneInputListener(object :
            LoginPhoneInputView.LoginPhoneInputListener {
            override fun forgetPassWord() {
                goForgetPassWordPhone()
            }

            override fun goLogin(
                phone: String,
                passWord: String,
                verifyCode: String,
                randomNum: String
            ) {
                mPresenter.goLoginPhone(phone, passWord, verifyCode, randomNum)
            }

            override fun goLoginSms(
                phone: String,
                passWord: String,
                verifyCode: String,
                randomNum: String
            ) {
                mPresenter.goLoginPhoneSms(phone, passWord, verifyCode, randomNum)
            }

            override fun readMessage(type: String) {
                mBottomSheetDialog = BottomSheetDialog()
                mBottomSheetDialog?.setMessageType(type)
                mBottomSheetDialog?.show(supportFragmentManager, "BottomSheetDialog")

            }

            //获取手机验证码
            override fun getPhoneSmsRnyCode(
                phoneNumber: String,
                imgVerifyCode: String,
                randomNum: String
            ) {
                mPresenter.getPhoneSmsRnyCode(phoneNumber, imgVerifyCode, randomNum)
            }

        })


        //没有账户去注册
        mBinding?.tvNoAccount?.setOnClickListener {
            ProxyActivityNavUtil.navToRegister(this@LoginOneActivity)
        }

        mBinding?.backMainTv?.setOnClickListener {
            finish()
        }
    }

    /**
     * 忘记密码 email
     */
    private fun goForgetPassWordAct() {
        ProxyActivityNavUtil.navToForgetPassWordEmail(this@LoginOneActivity)
    }

    /**
     * 忘记密码 phone
     */
    private fun goForgetPassWordPhone() {
        ProxyActivityNavUtil.navToForgetPassWordPhone(this@LoginOneActivity)
    }

    /**
     * 邮箱登录
     */
    private fun loginEmail(email: String, passWord: String, verifyCode: String, code: String) {
        mPresenter.goLogin(email, passWord, verifyCode, code)
    }

    private fun loginAll(email: String, passWord: String, verifyCode: String, code: String) {
        mPresenter.goLoginAll(email, passWord, verifyCode, code)
    }

    private fun loginSms(email: String, passWord: String, verifyCode: String, code: String) {
        mPresenter.goLogin(email, passWord, verifyCode, code)
    }



    /**
     * 手机号码登录
     */
    private fun loginPhone() {

    }

    /**
     * google登录
     */
    private fun loginGoogle() {
        lifecycleScope.launch {
            dataStore.edit{
                it[keyGoogleLogin] = true
            }
        }

        TLoginMgr.getInstance().login(this@LoginOneActivity,TLoginMgr.LoginType.Google,object : ITLoginListener{
            override fun onStart(tLoginType: TLoginMgr.LoginType?) {
                LogUtils.e("ceshi  第三方登录========：onStart=====")
            }

            override fun onSuccess(result: TLoginBean?) {
                LogUtils.e("ceshi  第三方登录========：onSuccess=====:",result)

            }

            override fun onSuccess1(token: String?) {

            }

            override fun onSuccess2(user_id: String?, email: String?) {
                user_id?.let { email?.let { it1 -> mPresenter.goRegisterLoginGoogle2(it, it1) } }
            }

            override fun onCancel() {
                LogUtils.e("ceshi  第三方登录========：onCancel=====")
            }

            override fun onFailed(errMsg: String?) {
                LogUtils.e("ceshi  第三方登录========：onFailed=====:",errMsg)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        TLoginMgr.getInstance().onActivityResult(requestCode, resultCode, data)
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
        mBinding?.emailInput?.visibility = if (isEmail) View.VISIBLE else View.GONE

        mBinding?.tvEmailPhoneName?.setTextColor(
            if (!isEmail) resources.getColor(R.color.un_selected) else resources.getColor(
                R.color.color302AI
            )
        )
        mBinding?.viewPhoneLine?.visibility = if (!isEmail) View.GONE else View.VISIBLE
        mBinding?.phoneInput?.visibility = if (isEmail) View.GONE else View.VISIBLE

    }

    /**
     * 登录成功
     */
    override fun onLoginSuccess(bean: LoginBean?) {
        val token = bean?.token?.replace("Basic|\\s".toRegex(), "")
        LogUtils.e("ceshi 登录成功了============：", token)
        WearData.getInstance().saveToken(token)
        ProxyActivityNavUtil.navMain(this@LoginOneActivity)
        finish()
    }

    /**
     * 登录失败
     */
    override fun onLoginFail(msg: NetException?) {

    }

    override fun onPhoneCodeSuccess() {
        mBinding?.phoneInput?.setPhoneVerifyCodeState()
    }

    override fun onFail() {
        mBinding?.phoneInput?.mBinding?.verifyCodeView?.getImgVerifyCode()
        mBinding?.emailInput?.mBinding?.verifyCodeView?.getImgVerifyCode()
    }

    private fun getScreenSize(){

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val density = displayMetrics.density
        val widthDp = (screenWidth / density).toInt()
        val heightDp = (screenHeight / density).toInt()
        Log.e("ceshi","widthDp:$widthDp>>>heightDp:$heightDp")//360,745
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBottomSheetDialog != null) {
            mBottomSheetDialog?.dismiss()
        }
    }

}