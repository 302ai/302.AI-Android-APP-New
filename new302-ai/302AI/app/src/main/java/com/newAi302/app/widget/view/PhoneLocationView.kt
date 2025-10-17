package com.newAi302.app.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutPhoneLocationBinding
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.utils.CommonUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/13
 * desc   : 地区选择 View
 * version: 1.0
 */
@SuppressLint("SetTextI18n")
class PhoneLocationView(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private var mSelectPhone = ""  //选择地区拼接后的手机号码
    private var mSelectListener: SelectPhoneLocationListener? = null
    private var mCountryCode = "86"  //区号名称 默认86

    private val mBinding: LayoutPhoneLocationBinding by lazy {
        LayoutPhoneLocationBinding.bind(View.inflate(context, R.layout.layout_phone_location, this))
    }

    init {
//        mBinding.tvAreaCode.text = "+$mCountryCode"

        if (WearData.getInstance().isRememberPassWordPhone) {
            mBinding.tvAreaCode.text =
                if (!TextUtils.isEmpty(WearData.getInstance().countryCode)) "+" + WearData.getInstance().countryCode else "+$mCountryCode"
            mBinding.codePicker.setDefaultCountryUsingPhoneCodeAndApply(
                if (!TextUtils.isEmpty(WearData.getInstance().countryCode)) WearData.getInstance().countryCode.toInt() else mCountryCode.toInt()
            )
            mBinding.editPhone.setText(if (!TextUtils.isEmpty(WearData.getInstance().phoneCode)) WearData.getInstance().phoneCode else "")
        } else {
            mBinding.tvAreaCode.text = "+$mCountryCode"
            mBinding.editPhone.setText("+$mCountryCode")
        }


        initListener()
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initListener() {

        //输入手机号码
        mBinding.editPhone.addTextChangedListener {
            //val phoneStr = " " + it.toString()
            val phoneStr = it.toString()
            //mSelectPhone = mBinding.tvAreaCode.text.toString() + phoneStr
            mSelectPhone = phoneStr
            mBinding.tvPhoneNumberErrorTip.text = if (TextUtils.isEmpty(it.toString()))
                resources.getString(R.string.please_enter_correct_phone_number) else ""
            if (mSelectListener != null) {
                mSelectListener?.selectPhone(mSelectPhone)
            }
            WearData.getInstance().saveLoginPhoneCode(mSelectPhone)
        }

        //地区选择
        mBinding.codePicker.setOnCountryChangeListener {
            mBinding.tvAreaCode.text = "+" + it.phoneCode
            mCountryCode = it.phoneCode
            Log.e("ceshi","选择区号：${it.phoneCode},,${mBinding.editPhone.text.toString()}")
            if (mBinding.editPhone.text.toString().length < 10){
                mBinding.editPhone.setText("+$mCountryCode")
            }
            WearData.getInstance().saveCountryCode(mCountryCode)
//            LogUtils.e("ceshi 选择的区号号名称：======", mCountryCode, it.phoneCode, it.iso)
        }

        mBinding.codePicker.setBackgroundColor(ContextCompat.getColor(context, R.color.input_bg))

    }

    /**
     * 判断手机是否输入正常
     */
    fun setPhoneNumberIsValid(): Boolean {
        var isPhoneEmpty = false
        val phoneStr = mBinding.editPhone.text.toString()
        //val mInputPhone = mBinding.tvAreaCode.text.toString() + phoneStr
        val mInputPhone = phoneStr
        //判断输入的手机号码格式是否正常
        val isValid = CommonUtils.isPhoneNumberValid(context, mInputPhone, mCountryCode)
//        LogUtils.e("ceshi 手机号码是否符合规矩===========：", isValid)
        if (TextUtils.isEmpty(phoneStr)) {
            isPhoneEmpty = false
            mBinding.tvPhoneNumberErrorTip.text =
                resources.getString(R.string.please_enter_correct_phone_number)
        } else {
            //如果输入的手机号符合选择的国家区号
            if (isValid) {
                isPhoneEmpty = true
                mBinding.tvPhoneNumberErrorTip.text = ""
            } else {
                isPhoneEmpty = false
                mBinding.tvPhoneNumberErrorTip.text =
                    resources.getString(R.string.please_enter_correct_phone_number)
            }
        }
        return isPhoneEmpty
    }

    interface SelectPhoneLocationListener {
        fun selectPhone(phone: String)
    }

    fun setSelectPhoneLocationListener(listener: SelectPhoneLocationListener) {
        this.mSelectListener = listener
    }
}