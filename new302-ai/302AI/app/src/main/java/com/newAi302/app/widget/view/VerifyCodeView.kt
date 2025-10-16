package com.newAi302.app.widget.view

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.newAi302.app.R
import com.newAi302.app.bean.ImageVerifyCodeBean
import com.newAi302.app.databinding.LayoutPasswordLoginBinding
import com.newAi302.app.databinding.LayoutVerifyCodeViewBinding
import com.newAi302.app.network.NetConfig
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.MainModel
import com.newAi302.app.utils.ThreadUtils
import com.newAi302.app.widget.listener.IClickListener
import com.newAi302.app.widget.utils.CommonUtils

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   : 图片验证码 view
 * version: 1.0
 */
class VerifyCodeView(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private var mListener: VerifyCodeViewListener? = null

    val mBinding: LayoutVerifyCodeViewBinding by lazy {
        LayoutVerifyCodeViewBinding.bind(
            View.inflate(
                context,
                R.layout.layout_verify_code_view,
                this
            )
        )
    }


    init {
        getImgVerifyCode()
        initListener()
    }

    private fun initListener() {
        mBinding.verifyCodeView.setOnClickListener(object : IClickListener() {
            override fun onIClick(v: View?) {
                getImgVerifyCode()
            }
        })
    }

    /**
     * 获取图片验证码
     */
    fun getImgVerifyCode() {
        val code = CommonUtils.generateRandomString(10)

        val url = NetConfig.URL_PROXY_STATIC_IMAGE + "?code=" + code
        MainModel.getProxyStaticImage(url,
            object : RequestCallback<BaseResponse<ImageVerifyCodeBean>>() {
                override fun onSuccess(data: BaseResponse<ImageVerifyCodeBean>?) {
                    Log.e("ceshi","0返回")
                }

                override fun onError(e: NetException?) {
                    Log.e("ceshi","1返回")
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun imgSuccess(bytes: ByteArray?) {
                    super.imgSuccess(bytes)
                    Log.e("ceshi","2返回")

                    ThreadUtils.runOnUiThreadDelayed({
                        val bitmap =
                            bytes?.size?.let { BitmapFactory.decodeByteArray(bytes, 0, it) }
                        if (bitmap != null) {
                            mBinding.verifyCodeView.setImageBitmap(bitmap)
                        }
                    }, 100)

                    if (mListener != null) {
                        mListener?.randomStr(code)
                    }
                }
            })
    }

    interface VerifyCodeViewListener {
        fun randomStr(randomNum: String)   //生成的随机数
    }

    fun setVerifyCodeViewListener(listener: VerifyCodeViewListener) {
        mListener = listener
    }


}