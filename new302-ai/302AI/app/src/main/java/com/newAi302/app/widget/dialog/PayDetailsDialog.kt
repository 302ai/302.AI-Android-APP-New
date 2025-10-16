package com.newAi302.app.widget.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.newAi302.app.R
import com.newAi302.app.bean.HtmlQuickAccessBean
import com.newAi302.app.databinding.DialogPayDetailsBinding
import com.newAi302.app.MainActivity
import com.newAi302.app.utils.ActivityUtils.startActivity
import com.newAi302.app.utils.ObjectUtils.requireNonNull
import com.newAi302.app.widget.dialog.base.BaseCenterDialog
import com.newAi302.app.widget.utils.CommonEnum


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
class PayDetailsDialog(context1: Activity, private val proxyBean: HtmlQuickAccessBean?) :
    BaseCenterDialog(context1) {

    private lateinit var mBinding: DialogPayDetailsBinding
    private var onButtonClickListener: OnButtonClickListener? = null
    private var url:String? = null

    private var context2:Activity = context1
    private val stripePublishableKey = "pk_live_51HkxCuE8EKY4y2jIZs7cPDz7SemQvFSUVP3rPDXT8k7sh7r2IeTFqg3MPPsukqtJmIfOkCfkIzcJVmygGHNgI5hx0079QxszZX"

    override fun getView(): View {
        mBinding = DialogPayDetailsBinding.bind(
            View.inflate(
                context,
                R.layout.dialog_pay_details,
                null
            )
        )
        return mBinding.root
    }

    override fun initView(view: View?) {
        window?.setBackgroundDrawableResource(R.drawable.shape_r4_ffffff_bg)
        mBinding.webViewUrl.visibility = View.GONE
        mBinding.llPay.visibility = View.VISIBLE
        initListener()
    }

    private fun initListener() {
        mBinding.btnClose.setOnClickListener {
            Log.e("ceshi","btnClose")
            dismiss()
            cancel()
        }

        mBinding.btnConfirm.setOnClickListener {
//            mBinding.llType.visibility = View.VISIBLE
//            mBinding.ipinfoWebView.visibility = View.GONE
            dismiss()
            cancel()
        }

        mBinding.llAlipay.setOnClickListener {
            val buttonText = "aliPay"
            Log.e("ceshi","aliPay")
            onButtonClickListener?.onButtonClick(buttonText)
            dismiss()
            cancel()
        }
        mBinding.llUsdt.setOnClickListener {
            val buttonText = "usdtPay"
            onButtonClickListener?.onButtonClick(buttonText)
            Log.e("ceshi","usdtpay")
            dismiss()
            cancel()
        }
        mBinding.llStripe.setOnClickListener {
            val buttonText = "stripePay"
            onButtonClickListener?.onButtonClick(buttonText)
            dismiss()
            cancel()
        }


    }


    private var requestCode: Int? = 0
    private var resultData: Intent? = null





    interface OnButtonClickListener {
        fun onButtonClick(buttonText: String)
    }

    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        onButtonClickListener = listener
    }

    fun setUrlListener(url:String) {
        show()
        this.url = url
        mBinding.webViewUrl.visibility = View.VISIBLE
        mBinding.llPay.visibility = View.GONE
        mBinding.webViewUrl.loadUrl(url)

        mBinding.webViewUrl?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.e("ceshi","11url+++++${url}")
                if (url == "https://dash.proxy302.com/charge") {//https://dash.proxy302.com/charge
                    // 在这里可以进行你想要的操作，比如记录日志、更新UI等
                    Log.d("ceshi", "监听到跳转到https://dash.proxy302.com/webapp/charge")
                    mBinding.webViewUrl.loadUrl("https://dash.proxy302.com/webapp/charge")
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

    }
}

