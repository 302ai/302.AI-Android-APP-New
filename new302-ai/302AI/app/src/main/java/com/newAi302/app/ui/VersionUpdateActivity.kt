package com.newAi302.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.R
import com.newAi302.app.bean.ProxyApkBean
import com.newAi302.app.databinding.ActivityPersonalCenterBinding
import com.newAi302.app.databinding.ActivityVersionUpdateBinding
import com.newAi302.app.network.common_bean.bean.BaseResponse
import com.newAi302.app.network.common_bean.callback.RequestCallback
import com.newAi302.app.network.common_bean.exception.NetException
import com.newAi302.app.ui.model.MainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VersionUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVersionUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVersionUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            getProxyAPK()
        }
        binding.versionNewCons.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://302.ai/downloads/") // 设置要跳转的网址
            this.startActivity(intent) // 启动活动
        }
    }

    private fun getProxyAPK() {
        Log.e("ceshi","获取代理apk")
        MainModel.getProxyApk(
            object : RequestCallback<BaseResponse<ProxyApkBean>>() {
                override fun onSuccess(data: BaseResponse<ProxyApkBean>?) {
                    Log.d("ceshi","获取apk：${data?.data?.latest_download_url},min_version:${data?.data?.min_version},recommend_version:${data?.data?.recommend_version}")

                    lifecycleScope.launch(Dispatchers.Main) {
                        data?.data.let {
                            getAPK(it?.min_version!!,it?.recommend_version!!,it?.latest_download_url!!)
                        }

                    }

                }

                override fun onError(e: NetException?) {
                    //LogUtils.e(e?.message)
                    Log.e("ceshi","获取代理apk错误${e?.message}")
                }
            })
    }

    private fun getAPK(min:String,recommend:String,url:String){


    }

}