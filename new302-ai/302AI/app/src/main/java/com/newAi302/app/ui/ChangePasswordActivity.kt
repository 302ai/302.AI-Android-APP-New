package com.newAi302.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.R
import com.newAi302.app.databinding.ActivityChangePasswordBinding
import com.newAi302.app.http.ApiService
import com.newAi302.app.http.NetworkFactory
import com.newAi302.app.ui.login.LoginOneActivity
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.viewModel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    val minNumber = 8  //最新密码字数
    private var mEyeSwitch: Boolean = true //眼睛切换状态 默认为false
    private var mEyeSwitch1: Boolean = true //眼睛切换状态 默认为false
    private var mEyeSwitch2: Boolean = true //眼睛切换状态 默认为false

    private val BASE_URL = "https://api.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    private val chatViewModel: ChatViewModel by viewModels()

    private var isCanChangePassword = false


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }
        chatViewModel.changeUserPswResult.observeForever { result ->
            result?.let {
                Log.e("ceshi", "机器人有回复修改密码：$it,,")
                if (it == "success"){
                    Toast.makeText(this, getString(R.string.change_password_sucess_toast_message), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginOneActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, getString(R.string.change_password_failed_toast_message), Toast.LENGTH_SHORT).show()
                }


            }

        }
        initView()

    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initView(){
        binding.editTitle.addTextChangedListener {
            Log.e("ceshi","password:${it.toString()}")
            if (!TextUtils.isEmpty(it.toString())) {
                binding.tvEmptyTipPass.text =
                    if (it.toString().length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            } else {
                binding.tvEmptyTipPass.text = resources.getString(R.string.password_empty_tip)
            }
        }

        binding.editTitle1.addTextChangedListener {
            Log.e("ceshi","password:${it.toString()}")
            if (!TextUtils.isEmpty(it.toString())) {
                binding.tvEmptyTipPass1.text =
                    if (it.toString().length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            } else {
                binding.tvEmptyTipPass1.text = resources.getString(R.string.password_empty_tip)
            }
        }

        binding.editTitle2.addTextChangedListener {
            Log.e("ceshi","password:${it.toString()}")
            if (!TextUtils.isEmpty(it.toString())) {
                binding.tvEmptyTipPass2.text =
                    if (it.toString().length >= minNumber) "" else resources.getString(R.string.password_min_8_bits)
            } else {
                binding.tvEmptyTipPass2.text = resources.getString(R.string.password_empty_tip)
            }
        }

        binding.textEyeImage.setOnClickListener {
            //binding.editTitle.setSelection(binding.editTitle.length())
            mEyeSwitch = !mEyeSwitch
            setEyeSwitch(mEyeSwitch)
            binding.editTitle.setSelection(binding.editTitle.text?.length?:0)

        }

        binding.textEyeImage1.setOnClickListener {
            //binding.editTitle1.setSelection(binding.editTitle1.length())
            mEyeSwitch1 = !mEyeSwitch1
            setEyeSwitch1(mEyeSwitch1)
            binding.editTitle1.setSelection(binding.editTitle1.text?.length?:0)

        }

        binding.textEyeImage2.setOnClickListener {
            //binding.editTitle2.setSelection(binding.editTitle2.length())
            mEyeSwitch2 = !mEyeSwitch2
            setEyeSwitch2(mEyeSwitch2)
            binding.editTitle2.setSelection(binding.editTitle2.text?.length?:0)

        }

        binding.changePasswordConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (binding.tvEmptyTipPass.text.toString() == "" && binding.tvEmptyTipPass1.text.toString() == "" && binding.tvEmptyTipPass.text.toString() == ""
                && binding.editTitle1.text.toString() == binding.editTitle2.text.toString() && (binding.editTitle1.text?.length
                    ?: 0) >= 8
            ){
                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                    chatViewModel.changeUserPassword(WearData.getInstance().token,apiService,binding.editTitle.text.toString(),binding.editTitle1.text.toString())
                }
            }else{
                if (binding.editTitle1.text.toString() == binding.editTitle2.text.toString() && (binding.editTitle1.text?.length
                        ?: 0) >= 8){
                    Toast.makeText(this, getString(R.string.change_password_not_same_toast_message), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, getString(R.string.change_password_fail_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    //设置眼睛开关
    private fun setEyeSwitch(isHide: Boolean) {
        binding.editTitle.text?.let { binding.editTitle.setSelection(it.length) }
        binding.editTitle.transformationMethod =
            if (isHide) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        binding.textEyeImage.setBackgroundResource(if (isHide) R.drawable.icon_visible else R.drawable.icon_invisible)
    }

    private fun setEyeSwitch1(isHide: Boolean) {
        binding.editTitle1.text?.let { binding.editTitle1.setSelection(it.length) }
        binding.editTitle1.transformationMethod =
            if (isHide) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        binding.textEyeImage1.setBackgroundResource(if (isHide) R.drawable.icon_visible else R.drawable.icon_invisible)
    }

    private fun setEyeSwitch2(isHide: Boolean) {
        binding.editTitle2.text?.let { binding.editTitle2.setSelection(it.length) }
        binding.editTitle2.transformationMethod =
            if (isHide) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        binding.textEyeImage2.setBackgroundResource(if (isHide) R.drawable.icon_visible else R.drawable.icon_invisible)
    }

}