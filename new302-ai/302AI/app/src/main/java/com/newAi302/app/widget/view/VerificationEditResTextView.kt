package com.newAi302.app.widget.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.newAi302.app.R
import com.newAi302.app.databinding.LayoutVerificationResViewBinding
import com.newAi302.app.databinding.LayoutVerificationViewBinding

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/8
 * desc   : 验证码 view
 * version: 1.0
 */
class VerificationEditResTextView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var mListener: VerifyCodeListener? = null

    val mBinding: LayoutVerificationResViewBinding by lazy {
        LayoutVerificationResViewBinding.bind(
            View.inflate(
                context,
                R.layout.layout_verification_res_view,
                this
            )
        )
    }

    init {
//        mBinding.tvEmptyTip.text = resources.getString(R.string.empty_tip)
        initListener()
    }

    private fun initListener() {
        mBinding.editVerification.addTextChangedListener {
            val codeStr = it.toString()
            mBinding.tvEmptyTip.visibility =
                if (TextUtils.isEmpty(codeStr)) View.VISIBLE else View.GONE

            mBinding.tvEmptyTip.text =
                if (TextUtils.isEmpty(codeStr)) resources.getString(R.string.empty_tip) else ""

            if (mListener != null) {
                mListener?.verifyCode(it.toString())
            }
        }
    }

    //外面调用
    fun setVerifyCodeEmpty(): Boolean {
        var isCodeEmpty = false
        val codeStr = mBinding.editVerification.text.toString()
        if (TextUtils.isEmpty(codeStr)) {
            mBinding.tvEmptyTip.text = resources.getString(R.string.empty_tip)
            isCodeEmpty = false
        } else {
            mBinding.tvEmptyTip.text = ""
            isCodeEmpty = true
        }
        return isCodeEmpty
    }

    interface VerifyCodeListener {
        fun verifyCode(code: String)
    }

    fun setVerifyCodeListener(listener: VerifyCodeListener) {
        this.mListener = listener
    }
}