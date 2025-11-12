package com.newAi302.app.widget.view

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutPasswordEditResViewBinding
import com.newAi302.app.databinding.LayoutPasswordEditViewBinding
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.utils.CommonEnum


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/8
 * desc   : 登录密码 View
 * version: 1.0  attrs: AttributeSet? = null
 */
class PassWorkResEditView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    val minNumber = 8  //最新密码字数
    private var mListener: LoginPasswordListener? = null
    private var mEyeSwitch: Boolean = false //眼睛切换状态 默认为false
    private var mHint = ""
    private var typeLogin = false

    private val mBinding: LayoutPasswordEditResViewBinding by lazy {
        LayoutPasswordEditResViewBinding.bind(
            View.inflate(
                context,
                R.layout.layout_password_edit_res_view,
                this
            )
        )
    }

    init {
        mBinding.editEmail.setText(if (!TextUtils.isEmpty(WearData.getInstance().emailPassWord)) WearData.getInstance().emailPassWord else "")
    }

    //当前登录类型 如：email，phone
    fun setCurrentLoginType(type: Int) {
        when (type) {
            CommonEnum.PassWordType.EMAIL -> {
                if (WearData.getInstance().isRememberPassWord) {
                    mBinding.editEmail.setText(if (!TextUtils.isEmpty(WearData.getInstance().emailPassWord)) WearData.getInstance().emailPassWord else "")
                } else {
                    mBinding.editEmail.setText("")
                }
            }

            CommonEnum.PassWordType.PHONE -> {
                if (WearData.getInstance().isRememberPassWordPhone) {
                    mBinding.editEmail.setText(if (!TextUtils.isEmpty(WearData.getInstance().loginPhonePassWord)) WearData.getInstance().loginPhonePassWord else "")
                } else {
                    mBinding.editEmail.setText("")
                }
            }
        }
    }

    //设置hint内容
    fun setPassWorkHint(hintStr: String) {
        mBinding.editEmail.setHint(hintStr)
    }

    private fun initView(attrs: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PassWorkInput)
        val count = typedArray.getIndexCount()
        for (i in 0 until count) {
            val index = typedArray.getIndex(i)
            when (index) {
                R.styleable.PassWorkInput_hint -> {
                    mHint = typedArray.getString(index).toString()
                    mBinding.editEmail.setHint(mHint)
                }
            }
        }
        typedArray.recycle()
    }

    init {
        initView(attrs)
        setEyeSwitch(false)

        mBinding.editEmail.setFilters(arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (source[i].code > 0x4E00 && source[i].code < 0x9FA5) {
                    // 检测到中文字符，返回空字符串表示不接受输入
                    return@InputFilter ""
                }
            }
            // 不是中文字符，允许输入
            null
        }));

        mBinding.editEmail.addTextChangedListener {
            var str = ""
            Log.e("ceshi","type>>>${typeLogin}")
            if (typeLogin){
                if(it.toString() == ""){
                    if (WearData.getInstance().isRememberPassWordPhone) {
                        str = WearData.getInstance().loginPhonePassWord
                    }else{
                        str = it.toString()
                    }
                }else{
                    str = it.toString()
                }
            }else{
                if(it.toString() == ""){
                    if (WearData.getInstance().isRememberPassWord) {
                        str = WearData.getInstance().emailPassWord
                    }else{
                        str = it.toString()
                    }
                }else{
                    str = it.toString()
                }
            }

            Log.e("ceshi","password:${str}>>>${WearData.getInstance().isRememberPassWord}")
            if (!TextUtils.isEmpty(str)) {
                mBinding.tvEmptyTipPass.text =
                    if (str.length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            } else {
                mBinding.tvEmptyTipPass.text = resources.getString(R.string.password_empty_tip)
            }

            mBinding.imbClose.visibility = if (!TextUtils.isEmpty(str)) View.VISIBLE else View.GONE

            if (mListener != null) {
                mListener?.inputContent(str)
            }
        }

        mBinding.imbEye.setOnClickListener {
            mBinding.editEmail.setSelection(mBinding.editEmail.length())
            mEyeSwitch = !mEyeSwitch
            setEyeSwitch(mEyeSwitch)
            if (mListener != null) {
                mListener?.eyeSwitch(true)
            }
        }

        mBinding.imbClose.setOnClickListener {
            mBinding.editEmail.setText("")
            if (mListener != null) {
                mListener?.cleanInput()
            }
        }
    }

    //设置眼睛开关
    private fun setEyeSwitch(isHide: Boolean) {
        mBinding.editEmail.text?.let { mBinding.editEmail.setSelection(it.length) }
        mBinding.editEmail.transformationMethod =
            if (isHide) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        mBinding.imbEye.setBackgroundResource(if (isHide) R.drawable.icon_visible else R.drawable.icon_invisible)
    }

    //点击注册时判断密码是否为空tip
    fun setPassWordIsEmpty() :Boolean{
        var str = ""
        if (typeLogin){
            /*if (WearData.getInstance().isRememberPassWordPhone) {
                str = WearData.getInstance().loginPhonePassWord
            }else{
                str = mBinding.tvEmptyTipPass.text.toString()
            }*/
            if (!TextUtils.isEmpty(mBinding.tvEmptyTipPass.text.toString())){
                str = mBinding.tvEmptyTipPass.text.toString()
            }else{
                if (WearData.getInstance().isRememberPassWordPhone) {
                    str = WearData.getInstance().loginPhonePassWord
                }else{
                    str =
                        if (!TextUtils.isEmpty(WearData.getInstance().loginPhonePassWord)) WearData.getInstance().loginPhonePassWord else ""
                }

            }
        }else{
            /*if (WearData.getInstance().isRememberPassWord) {
                str = WearData.getInstance().emailPassWord
            }else{
                str = mBinding.tvEmptyTipPass.text.toString()
            }*/
            if (!TextUtils.isEmpty(mBinding.tvEmptyTipPass.text.toString())){
                str = mBinding.tvEmptyTipPass.text.toString()
            }else{
                if (WearData.getInstance().isRememberPassWord) {
                    str = WearData.getInstance().emailPassWord
                }else{
                    str =
                        if (!TextUtils.isEmpty(WearData.getInstance().emailPassWord)) WearData.getInstance().emailPassWord else ""
                }
            }
        }

        Log.e("ceshi","1password:${str}>>>${WearData.getInstance().isRememberPassWord}")
        if (!TextUtils.isEmpty(str)) {
            mBinding.tvEmptyTipPass.text =
                if (str.length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            return true
        } else {
            mBinding.tvEmptyTipPass.text = resources.getString(R.string.password_empty_tip)
            return false
        }
    }

    fun setPassWordIsEmpty1() :Boolean{
        var str = ""
        if (typeLogin){
            if (WearData.getInstance().isRememberPassWordPhone) {
                str = WearData.getInstance().loginPhonePassWord
            }else{
                str = mBinding.tvEmptyTipPass.text.toString()
            }
        }else{
            if (WearData.getInstance().isRememberPassWord) {
                str = WearData.getInstance().emailPassWord
            }else{
                str = mBinding.tvEmptyTipPass.text.toString()
            }
        }

        Log.e("ceshi","1password:${str}>>>${WearData.getInstance().isRememberPassWord}")
        if (!TextUtils.isEmpty(str)) {
            mBinding.tvEmptyTipPass.text =
                if (str.length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            return true
        } else {
            mBinding.tvEmptyTipPass.text = resources.getString(R.string.password_empty_tip)
            return false
        }
    }

    fun setType(type:Boolean){
        typeLogin = type
    }

    /**
     * 判断是否满足8位数
     */
    fun getIsPassWorkTerm(): Boolean {
        val str = mBinding.editEmail.text.toString()
        return str.length >= minNumber
    }

    /**
     * 两次输入的密码不一致
     */
    fun setInputPassWordIsEqual() {
        mBinding.tvEmptyTipPass.text =
            resources.getString(R.string.passwords_entered_twice_inconsistent)
    }

    interface LoginPasswordListener {
        fun inputContent(password: String)

        fun eyeSwitch(isSwitch: Boolean)

        fun cleanInput()
    }

    fun setLoginPassWordListener(listener: LoginPasswordListener) {
        mListener = listener
    }
}