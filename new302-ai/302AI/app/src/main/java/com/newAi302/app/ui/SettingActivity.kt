package com.newAi302.app.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.newAi302.app.MainActivity
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.adapter.EmojiAdapter
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.bean.HtmlChargeJsonBean
import com.newAi302.app.bean.HtmlProxyJsonBean
import com.newAi302.app.bean.HtmlQuickAccessBean
import com.newAi302.app.constant.AppConstant
import com.newAi302.app.data.MainMessage
import com.newAi302.app.databinding.ActivityMainBinding
import com.newAi302.app.databinding.ActivitySettingBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.http.ApiService
import com.newAi302.app.http.NetworkFactory
import com.newAi302.app.network.common_bean.callback.LoginCallback
import com.newAi302.app.network.common_bean.callback.ResponseAliData
import com.newAi302.app.network.common_bean.callback.ResponseStripeData
import com.newAi302.app.network.common_bean.callback.ResponseUsdtData
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.UserConfigurationRoom
import com.newAi302.app.ui.model.MainModel
import com.newAi302.app.utils.ActivityUtils
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.LanguageUtil
import com.newAi302.app.utils.LanguageUtil.saveLanguageSetting
import com.newAi302.app.utils.LogUtils
import com.newAi302.app.utils.SystemUtils
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.utils.base.WearUtil
import com.newAi302.app.viewModel.ChatViewModel
import com.newAi302.app.widget.dialog.PayDetailsDialog
import com.newAi302.app.widget.utils.CommonEnum
import com.newAi302.app.widget.utils.CommonHtmlUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class SettingActivity : BaseActivity(), PayDetailsDialog.OnButtonClickListener {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var dataStoreManager: DataStoreManager
    private var mPayDetailDialog: PayDetailsDialog? = null
    private var mQuickAccessBean: HtmlQuickAccessBean? = null
    private var isUSDT = false
    private var mWebView: BridgeWebView? = null
    private var payType = ""
    @Volatile
    private var personalCenter = false
    private lateinit var chatDatabase: ChatDatabase
    private lateinit var modelListNull:MutableList<String>

    private lateinit var dialogUtils: DialogUtils

    private var defaultSystemLanguage = "zh"
    private var defaultSystemTheme = "light"
    private var defaultEmoji = "\uD83D\uDE00"
    private var mUserId = ""
    private var isNewChat = false

    private val chatViewModel: ChatViewModel by viewModels()
    private val BASE_URL = "https://api.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    var mLanguage = ""
    private var readImageUrl = ""

    // 初始化表情列表
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤯",
        "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔",
        "🤭", "🤫", "🤥", "😶", "😐", "😑", "😒", "🙄", "😳", "🤤",
        "😪", "😴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠",
        "😈", "👿", "👹", "👺", "💀", "☠️", "👻", "👽", "👾", "🤖",
        "💩", "👻", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀",
        "😿", "😾", "👐", "👏", "🤝", "👍", "👎", "✊", "👊", "🤛", "🤜",
        "🤞", "✌️", "🤟", "👌", "👈", "👉", "👆", "👇", "☝️", "✋",
        "🤚", "🖐️", "🖖", "👋", "🤙", "💪", "🦾", "👂", "🦻", "👃",
        "👁️", "👀", "👅", "👄", "👶", "🧒", "👦", "👧", "👨", "👩",
        "🧑", "👴", "👵", "👱", "👮", "🕵️", "👩‍⚕️", "👨‍⚕️", "👩‍🌾", "👨‍🌾",
        "👩‍🍳", "👨‍🍳", "👩‍🎓", "👨‍🎓", "👩‍🎤", "👨‍🎤", "👩‍💻", "👨‍💻", "👩‍🏫", "👨‍🏫",
        "👩‍⚖️", "👨‍⚖️", "👩‍🚒", "👨‍🚒", "👩‍✈️", "👨‍✈️", "👩‍🚀", "👨‍🚀", "👩‍⚙️", "👨‍⚙️",
        "👩‍🔧", "👨‍🔧", "👩‍🎨", "👨‍🎨", "👩‍🏭", "👨‍🏭", "👩‍💼", "👨‍💼", "👩‍🔬", "👨‍🔬",
        "👩‍💻", "👨‍💻", "👩‍🎓", "👨‍🎓", "👩‍🏫", "👨‍🏫", "👩‍⚕️", "👨‍⚕️", "👩‍🔧", "👨‍🔧",
        "👩‍🎤", "👨‍🎤", "👩‍🚒", "👨‍🚒", "👩‍✈️", "👨‍✈️", "👩‍🚀", "👨‍🚀", "👩‍⚖️", "👨‍⚖️"
    )
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_setting)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatDatabase = ChatDatabase.getInstance(this)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        val newChat = intent.getSerializableExtra("chat_new") as? Boolean
        if (newChat != null){
           isNewChat = newChat
        }
        chatViewModel.userInfoResult.observe(this){
            it.let {
                binding.userBalanceTv.text = it.balance.toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveUserBalance(it.balance)
                }
            }
        }
        initView()
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = dataStoreManager.readUserEmailData.first()?:""
            var userConfigurationRoom = chatDatabase.chatDao().getUserConfigByUserId(userId)
            userConfigurationRoom?.appEmojisData = defaultEmoji
            userConfigurationRoom?.systemLanguage = defaultSystemLanguage
            userConfigurationRoom?.systemTheme = defaultSystemTheme

//            userConfigurationRoom?.let {
//                chatDatabase.chatDao().insertUserConfig(
//                    UserConfigurationRoom(0,userConfigurationRoom.userId,userConfigurationRoom.systemLanguage,"light",readUseTracelessSwitch,
//                        readSlideBottomSwitch,readAppEmojisData,readSearchServiceType,readModelType,readBuildTitleModelType,modelList,readBuildTitleTime)
//                )
//            }



        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initView(){
        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                /*ContextCompat.getString(this@SettingActivity, R.string.language_ch_message) -> {
                    binding.systemLanguageTV.text = ContextCompat.getString(this@SettingActivity, R.string.language_ch_message)
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        dataStoreManager.saveBuildTitleTimeData("第一次对话")
//                    }
                    switchLanguage(LanguageUtil.LANGUAGE_ZH)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "zh"
                }
                ContextCompat.getString(this@SettingActivity, R.string.language_en_message) -> {
                    binding.systemLanguageTV.text = ContextCompat.getString(this@SettingActivity, R.string.language_en_message)
                    switchLanguage(LanguageUtil.LANGUAGE_EN)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "en"
                }

                ContextCompat.getString(this@SettingActivity, R.string.language_ja_message) -> {
                    binding.systemLanguageTV.text = ContextCompat.getString(this@SettingActivity, R.string.language_ja_message)
                    switchLanguage(LanguageUtil.LANGUAGE_JA)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "ja"
                }*/
                "中文" -> {
                    binding.systemLanguageTV.text = "中文"
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        dataStoreManager.saveBuildTitleTimeData("第一次对话")
//                    }
                    switchLanguage(LanguageUtil.LANGUAGE_ZH)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "zh"
                }
                "English" -> {
                    binding.systemLanguageTV.text = "English"
                    switchLanguage(LanguageUtil.LANGUAGE_EN)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "en"
                }

                "日本語" -> {
                    binding.systemLanguageTV.text = "日本語"
                    switchLanguage(LanguageUtil.LANGUAGE_JA)
                    applyLanguage()
                    //onLanguageChange()
                    defaultSystemLanguage = "ja"
                }


            }
        }

        binding.historyImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            lifecycleScope.launch(Dispatchers.IO) {
                //val chatItemHistory = chatDatabase.chatDao().getLastChatItem()
                //val chatItemHistory = chatDatabase.chatDao().getChatsByUserId(mUserId).last()
                // 1. 先获取列表（不要直接链式调用 last()）
                val chatList = chatDatabase.chatDao().getChatsByUserId(mUserId)

                // 2. 检查列表是否为空
                val chatItemHistory = if (chatList.isNotEmpty()) {
                    // 列表非空，安全获取最后一个元素
                    chatList.last()
                } else {
                    // 列表为空，根据业务需求处理（如返回 null 或默认值）
                    null
                }
                val intent = Intent(this@SettingActivity, MainActivity::class.java)
                if (chatItemHistory != null){
                    if (!isNewChat){
                        intent.putExtra("setting_chat_item", chatItemHistory)
                    }
                }
                val botMsg = getString(R.string.front_page_bottom_message)
                val welMsg = getString(R.string.front_page_message)
                val sendMsg = getString(R.string.chat_edit_message)

                intent.putExtra("msg_setting",MainMessage(welMsg,sendMsg,botMsg,readImageUrl))
                Log.e("ceshi","发送前：${welMsg}")
                startActivity(intent)
                finish()
            }

        }
        binding.toAnnouncementConst.setOnClickListener {
            val intent = Intent(this, AnnouncementActivity::class.java)
            startActivity(intent)
        }
        binding.toServiceConst.setOnClickListener {
            // 处理点击事件
            //android.widget.Toast.makeText(context, "你点击了官网地址", android.widget.Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://302.ai/") // 设置要跳转的网址
            this.startActivity(intent) // 启动活动
        }
        binding.toModelManagerConst.setOnClickListener {
            val intent = Intent(this, ModelManagerActivity::class.java)
            startActivity(intent)
        }
        val systemLanguage = SystemUtils.getSystemLanguage(this)
        val systemThem = SystemUtils.getSystemTheme(this)
        Log.e("ceshi","系统$systemLanguage,$systemThem")
//        if (systemLanguage == "中文 (中国)"){
//            binding.systemLanguageTV.text = "简体中文"
//        }else if (systemLanguage == "English (United States)"){
//            binding.systemLanguageTV.text = "English"
//        }
        val language = LanguageUtil.getSavedLanguage(this)
        Log.e("ceshi","获取的语言是:$language")
        mLanguage = language
        defaultSystemLanguage = language
        when (language) {
            LanguageUtil.LANGUAGE_ZH -> binding.systemLanguageTV.text = "简体中文"
            LanguageUtil.LANGUAGE_JA -> binding.systemLanguageTV.text = "日本語"
            else -> binding.systemLanguageTV.text = "English"
        }
        if (systemThem == "浅色模式"){
            binding.systemThemeTv.text = "light"
        }else if (systemThem == "深色模式"){
            binding.systemThemeTv.text = "night"
        }
        binding.systemLanguageCons.setOnClickListener {
            //SystemUtils.openLanguageSettings(this)
            /*val options = mutableListOf(ContextCompat.getString(this@SettingActivity, R.string.language_ch_message),
                ContextCompat.getString(this@SettingActivity, R.string.language_en_message),
                ContextCompat.getString(this@SettingActivity, R.string.language_ja_message))*/
            val options = mutableListOf("中文","English","日本語")
            dialogUtils.setupPopupWindow(options,"languageTypeList",this)
            dialogUtils.showPopup(binding.systemLanguageLine)
        }
        binding.systemThemeCons.setOnClickListener {
            SystemUtils.openThemeSettings(this)
        }

        binding.toPreferencesCons.setOnClickListener {
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
        }

        /*binding.imageProfile.setOnClickListener {
            showEmojiPickerDialog()
        }*/
        binding.detailsCons.setOnClickListener {
            val intent = Intent(this, ConsumptionDetailsActivity::class.java)
            startActivity(intent)
        }


        binding.logoutBtn.setOnClickListener {
            insertUserConfiguration()
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveData("")
                WearData.getInstance().saveToken("")
            }
            val botMsg = getString(R.string.front_page_bottom_message)
            val welMsg = getString(R.string.front_page_message)
            val sendMsg = getString(R.string.chat_edit_message)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("come_from","setting")
            intent.putExtra("msg_setting",MainMessage(welMsg,sendMsg,botMsg,readImageUrl))
            startActivity(intent)
            finish()
        }

        binding.versionConst.setOnClickListener {
            val intent = Intent(this, VersionUpdateActivity::class.java)
            startActivity(intent)
        }
        binding.protocolConst.setOnClickListener {
            val intent = Intent(this, ProtocolActivity::class.java)
            startActivity(intent)
        }

        binding.payBtn.setOnClickListener {
//            val intent = Intent(this, PayActivity::class.java)
//            startActivity(intent)

            showBottomPayDialog(this@SettingActivity)

            /*mPayDetailDialog = PayDetailsDialog(this@SettingActivity, mQuickAccessBean)
            // 为 BottomSheetDialog 设置布局
            val view: View = layoutInflater.inflate(R.layout.dialog_pay_details, null)
            mPayDetailDialog?.setContentView(view)
            mPayDetailDialog?.show()

            // 获取布局中的 Button，并设置点击事件
            val brWebView = view.findViewById<BridgeWebView>(R.id.webViewUrl)
            mWebView = brWebView

            //brWebView.visibility = View.VISIBLE
            loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,"https://302.ai/charge",mWebView!!)

            //view.findViewById<LinearLayout>(R.id.ll_pay).visibility = View.GONE




//            val chargeJsonBean = Gson().fromJson(data, HtmlChargeJsonBean::class.java)
//            if (chargeJsonBean != null) {
//                LogUtils.e(
//                    "ceshi 输出的数据类型是什么===========：",
//                    chargeJsonBean.note,
//                    chargeJsonBean.id
//                )
//
//            }

            val chargeId = 1
            mPayDetailDialog?.setOnButtonClickListener(object : PayDetailsDialog.OnButtonClickListener {
                override fun onButtonClick(buttonText: String) {
                    Log.e("ceshi","onButtonClick$buttonText")
                    when (buttonText) {
                        "aliPay" -> {
                            ToastUtils.showLong(resources.getString(R.string.toast_loading))
                            MainModel.getAliPayUrl(chargeId, object : LoginCallback {
                                override fun onSuccess(responseBody: String?) {
                                    val gson = Gson()
                                    Log.e("ceshi","ali+++++${responseBody}")
                                    val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                                    Log.e("ceshi","ali+++++${response.data.to}")
                                    runOnUiThread {
                                        //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to)
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setData(Uri.parse(response.data.to))
                                        ActivityUtils.startActivity(intent)
                                    }

                                }

                                override fun onFailure(errorCode: Int, errorMessage: String?) {
                                    Log.e("ceshi","ali----")
                                }

                            })


                        }
                        "usdtPay" -> {
                            ToastUtils.showLong(resources.getString(R.string.toast_loading))
                            MainModel.getUstdurl(chargeId, object : LoginCallback {
                                override fun onSuccess(responseBody: String?) {
                                    val gson = Gson()
                                    Log.e("ceshi","ali+++++${responseBody}")
                                    val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                                    Log.e("ceshi","ali+++++${response.data.url}")
                                    runOnUiThread {
                                        //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url)
                                        //mPayDetailDialog?.setUrlListener(response.data.url)
                                        Log.e("ceshi","++url${response.data.url}")
                                        //mBinding?.incMain?.webViewPay?.evaluateJavascript("document.body.innerHTML = \"\";", null)

                                        /*mBinding?.incMain?.webViewPay?.loadUrl(response.data.url)
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            // 延迟1秒（1000毫秒）
                                            delay(1300)
                                            mBinding?.incMain?.webView?.visibility = View.GONE
                                            mBinding?.incMain?.webViewPay?.visibility = View.VISIBLE
                                        }*/
                                    }
                                    //isUSDT = true

                                }

                                override fun onFailure(errorCode: Int, errorMessage: String?) {
                                    Log.e("ceshi","ali----")
                                }

                            })
                        }
                        "stripePay" -> {
                            ToastUtils.showLong(resources.getString(R.string.toast_loading))
                            //payType = "stripePay"
                            MainModel.getStripeKey(chargeId, object : LoginCallback {
                                override fun onSuccess(responseBody: String?) {
                                    Log.e("ceshi","stripe++++")
                                    val gson = Gson()
                                    Log.e("ceshi","ali+++++${responseBody}")
                                    val response = gson.fromJson(responseBody, ResponseStripeData::class.java)
                                    var publishableKey = ""
                                    publishableKey = response.data.jk+response.data.hk+response.data.pk+response.data.a
                                    Log.e("ceshi","ali+++++${publishableKey}")
                                    //sendDataToJavaScript(publishableKey)
                                    var url = "https://dash.proxy302.com/stripe?pk=$publishableKey&sid=${response.data.session_id}"
                                    Log.e("ceshi","url++++${url}")

                                    runOnUiThread {
                                        /*mWebView?.webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                                Log.e("ceshi","url+++++${url}")
                                                if (url == "https://dash.proxy302.com/charge") {//https://dash.proxy302.com/charge
                                                    // 在这里可以进行你想要的操作，比如记录日志、更新UI等
                                                    Log.d("ceshi", "监听到跳转到https://dash.proxy302.com/webapp/charge")
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,"https://dash.proxy302.com/webapp/charge")
                                                }
                                                return super.shouldOverrideUrlLoading(view, url)
                                            }
                                        }*/
                                        //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,url)
                                        //mPayDetailDialog?.setUrlListener(url)
                                        Log.e("ceshi","--url${url}")
                                        //loadWebHandler1(url)
                                    }
                                }

                                override fun onFailure(errorCode: Int, errorMessage: String?) {
                                    Log.e("ceshi","stripe----")
                                }

                            })
                        }
                    }
                }

            })*/
            /*MainModel.getAliPayUrl(chargeJsonBean.id, object : LoginCallback {
                override fun onSuccess(responseBody: String?) {
                    val gson = Gson()
                    Log.e("ceshi","ali+++++${responseBody}")
                    val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                    Log.e("ceshi","ali+++++${response.data.to}")
                    runOnUiThread {
                        //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to)
                        /*val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(response.data.to))
                        ActivityUtils.startActivity(intent)*/
                    }

                }

                override fun onFailure(errorCode: Int, errorMessage: String?) {
                    Log.e("ceshi","ali----")
                }

            })*/

            /*MainModel.getUstdurl(chargeJsonBean.id, object : LoginCallback {
                override fun onSuccess(responseBody: String?) {
                    val gson = Gson()
                    Log.e("ceshi","ali+++++${responseBody}")
                    val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                    Log.e("ceshi","ali+++++${response.data.url}")
                    runOnUiThread {
                        loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url)
                        /*val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(response.data.to))
                        ActivityUtils.startActivity(intent)*/
                    }

                }

                override fun onFailure(errorCode: Int, errorMessage: String?) {
                    Log.e("ceshi","ali----")
                }

            })*/



        }

        binding.cons0.setOnClickListener {
            val intent = Intent(this, PersonalCenterActivity::class.java)
            startActivity(intent)
        }

        binding.clearChatConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            lifecycleScope.launch (Dispatchers.IO){
                chatDatabase.chatDao().deleteAllChatsByUserId(mUserId)
            }
        }



    }

    override fun onResume() {
        super.onResume()
        val systemThem = SystemUtils.getSystemTheme(this)
        Log.e("ceshi","系统主题,$systemThem")
        if (systemThem == "浅色模式"){
            binding.systemThemeTv.text = "light"
        }else if (systemThem == "深色模式"){
            binding.systemThemeTv.text = "night"
        }

        lifecycleScope.launch((Dispatchers.IO)) {

            /*val readAppEmojisData = dataStoreManager.readAppEmojisData.first()
            readAppEmojisData?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("setting","readAppEmojisData是多少：$it")
                    binding.imageProfile.text = it
                }
            }*/
            val data = dataStoreManager.readImageUrl.first()
            data?.let {
                Log.e("ceshi", "imageurl是个多少：$it")
                readImageUrl = it
                lifecycleScope.launch(Dispatchers.Main) {
                    // 方法1：使用内置的CircleCrop变换
                    Glide.with(this@SettingActivity)
                        .load(it)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.imageProfile)
                }

            }

            val readUserNameData = dataStoreManager.readUserNameData.first()
            readUserNameData?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("setting","readUserNameData：$it")
                    binding.userSettingTv.text = it
                }
            }

            val readUserBalanceData = dataStoreManager.readUserBalanceData.first()
            readUserBalanceData?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("setting","readUserNameData：$it")
                    binding.userBalanceTv.text = it.toString()
                }
            }

            val readUserEmailData = dataStoreManager.readUserEmailData.first()
            readUserEmailData?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("setting","readUserEmailData：$it")
                    binding.userIdSettingTv.text = it
                    mUserId = readUserEmailData
                }
            }



        }
    }


    private fun showEmojiPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_emoji_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        var recyclerView = dialog.findViewById<RecyclerView>(R.id.emojiRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = EmojiAdapter(emojis) { selectedEmoji ->
            // 更新TextView显示选中的表情
            //binding.imageProfile.text = selectedEmoji
            defaultEmoji = selectedEmoji
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveAppEmojisData(selectedEmoji)
            }

            // 添加选中动画效果
            binding.imageProfile.scaleX = 0.8f
            binding.imageProfile.scaleY = 0.8f
            binding.imageProfile.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction {
                    binding.imageProfile.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()

            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPayDetailDialog != null) {
            mPayDetailDialog?.dismiss()
        }
    }

    override fun onButtonClick(buttonText: String) {
        // 在这里处理从Dialog回调过来的参数
        Log.e("ceshi","onButtonClick$buttonText")
        val chargeId = 0
        when (buttonText) {
            "aliPay" -> {
                MainModel.getAliPayUrl(chargeId, object : LoginCallback {
                    override fun onSuccess(responseBody: String?) {
                        val gson = Gson()
                        Log.e("ceshi","ali+++++${responseBody}")
                        val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                        Log.e("ceshi","ali+++++${response.data.to}")
                        runOnUiThread {
                            //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setData(Uri.parse(response.data.to))
                            ActivityUtils.startActivity(intent)
                        }

                    }

                    override fun onFailure(errorCode: Int, errorMessage: String?) {
                        Log.e("ceshi","ali----")
                    }

                })


            }
            "usdtPay" -> {
                MainModel.getUstdurl(chargeId, object : LoginCallback {
                    override fun onSuccess(responseBody: String?) {
                        val gson = Gson()
                        Log.e("ceshi","ali+++++${responseBody}")
                        val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                        Log.e("ceshi","ali+++++${response.data.url}")
                        runOnUiThread {
                            //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url)
                        }

                    }

                    override fun onFailure(errorCode: Int, errorMessage: String?) {
                        Log.e("ceshi","ali----")
                    }

                })
            }
            "stripePay" -> {
                //payType = "stripePay"
            }
        }
    }


    /**
     * 加载webView链接
     * @param type       加载类型
     * @param htmlUrl    加载路径
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun loadWebHandler(type: Int, htmlUrl: String,mWebView: BridgeWebView) {
        mWebView?.settings?.javaScriptEnabled = true
//        mWebView?.settings?.databaseEnabled = true //数据库缓存
        mWebView?.settings?.setGeolocationEnabled(true) // 允许网页定位
        mWebView?.settings?.loadsImagesAutomatically = true // 加快网页加载完成的速度，等页面完成再加载图片
        mWebView?.settings?.domStorageEnabled = true       // 开启 localStorage
        mWebView?.setDefaultHandler(DefaultHandler())
        mWebView?.webChromeClient = WebChromeClient()

        mWebView?.webChromeClient = object : WebChromeClient() {
            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri?>?>,
                fileChooserParams: FileChooserParams
            ): Boolean {

//                try {
//                    val intent = fileChooserParams?.createIntent()?:return super.onShowFileChooser(webView,filePathCallback,fileChooserParams)
//                    uploadMessageAboveL = filePathCallback
//                    launcher.launch(intent)
//                } catch (e: ActivityNotFoundException) {
//                    uploadMessageAboveL = null
//                    return false
//                }
                return true
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.e("ceshi","onReceivedTitle$title${view?.url}")
                if (view?.url == "https://dash-api.302.ai/charge") {//https://dash.proxy302.com/charge
                    // 在这里可以进行你想要的操作，比如记录日志、更新UI等
                    isUSDT = false
                    Log.d("ceshi", "监听到跳转到https://dash.proxy302.com/webapp/charge")//https://302.ai/charge
                    //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,"https://dash.proxy302.com/webapp/charge",mWebView)
                }
            }

        }

        mWebView?.apply {
            settings.javaScriptEnabled = true // 确保 JS 启用（部分网站依赖 JS 加载）
            settings.domStorageEnabled = true
            settings.useWideViewPort = true // 适配屏幕，避免加载后样式问题间接导致超时
            settings.loadWithOverviewMode = true

            // 优先使用缓存，无缓存时再请求网络
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            // 启用 HTML5 缓存（如 localStorage、IndexedDB）
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            // 1. 支持 TLS 1.0~1.3（适配低版本 Android）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许混合内容（HTTPS 加载 HTTP 资源）
                webViewClient = object : WebViewClient() {
                    // 解决 TLS 协议支持问题
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        // 注意：仅调试时使用！正式环境需验证证书，避免安全风险
                        handler?.proceed() // 忽略 SSL 证书错误（临时排查用）
                    }

                    // 捕获加载错误，打印日志定位问题
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        // 打印错误信息（在 Logcat 中搜索 "WebViewError"）
                        Log.e("WebViewError", "错误码：${error?.errorCode}，描述：${error?.description}")
                        when (error?.errorCode) {
                            -8 -> { // ERR_CONNECTION_TIMED_OUT
                                Toast.makeText(context, "连接超时，请检查网络或稍后重试", Toast.LENGTH_SHORT).show()
                            }
                            -2 -> { // 无网络
                                Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, "加载失败：${error?.description}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }




        var url = ""
        when (type) {
            CommonEnum.LoadHtmlType.NEED_SPLICED_PARA -> {
                url =
                    WearUtil.APP_SERVER_HTTPS_HTML + htmlUrl + WearData.getInstance().token + "/?device=" + AppConstant.device
            }

            CommonEnum.LoadHtmlType.NOT_NEED_SPLICED_PARA -> {
                url = WearUtil.APP_SERVER_HTTPS_HTML + htmlUrl
            }

            CommonEnum.LoadHtmlType.DIRECT_LINK -> {
                url = htmlUrl
            }
        }

        LogUtils.e("ceshi  url是什么=================：", url)
        mWebView?.loadUrl(url)
        if(url == "https://dash.proxy302.com/webapp/user-center"){
            lifecycleScope.launch {
                delay(2000)
                personalCenter = true
            }
            lifecycleScope.launch {
                delay(2000)
                Log.e("ceshi","personalCenter>>>$personalCenter")
            }
        }
        //mWebView?.callHandler(CommonHtmlUtil.handlerName, url, null)
        //mWebView?.callHandler(CommonHtmlUtil.handlerNamePara, url, null)
        mWebView?.registerHandler(CommonHtmlUtil.handlerNamePara, object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.e("测试","ffffffffffff")
                LogUtils.e("ceshi 是否有数据过来============:", data.toString())

                showHtmlAnalysisData(data)
            }
        })


    }

    /**
     * 数据处理
     */
    fun showHtmlAnalysisData(jsonStr: String?) {
        var chargeId = 0
        if (jsonStr != null) {
            val jsonObj = JSONObject(jsonStr)

            val type = jsonObj.optString(CommonHtmlUtil.htmlType)
            val data = jsonObj.optString(CommonHtmlUtil.htmlData)
            LogUtils.e("ceshi 解析的数据是什么==============：", type)

            when (type) {
                CommonHtmlUtil.chargeHtml -> {

                    mPayDetailDialog = PayDetailsDialog(this@SettingActivity, mQuickAccessBean)
                    mPayDetailDialog?.show()




                    val chargeJsonBean = Gson().fromJson(data, HtmlChargeJsonBean::class.java)
                    if (chargeJsonBean != null) {
                        LogUtils.e(
                            "ceshi 输出的数据类型是什么===========：",
                            chargeJsonBean.note,
                            chargeJsonBean.id
                        )
                        chargeId = chargeJsonBean.id
                        mPayDetailDialog?.setOnButtonClickListener(object : PayDetailsDialog.OnButtonClickListener {
                            override fun onButtonClick(buttonText: String) {
                                Log.e("ceshi","onButtonClick$buttonText")
                                when (buttonText) {
                                    "aliPay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        MainModel.getAliPayUrl(chargeId, object : LoginCallback {
                                            override fun onSuccess(responseBody: String?) {
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                                                Log.e("ceshi","ali+++++${response.data.to}")
                                                runOnUiThread {
                                                    //loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to)
                                                    val intent = Intent(Intent.ACTION_VIEW)
                                                    intent.setData(Uri.parse(response.data.to))
                                                    ActivityUtils.startActivity(intent)
                                                }

                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","ali----")
                                            }

                                        })


                                    }
                                    "usdtPay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        MainModel.getUstdurl(chargeId, object : LoginCallback {
                                            override fun onSuccess(responseBody: String?) {
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                                                Log.e("ceshi","ali+++++${response.data.url}")
                                                runOnUiThread {
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url,mWebView!!)
                                                    //mPayDetailDialog?.setUrlListener(response.data.url)
                                                    Log.e("ceshi","++url${response.data.url}")
                                                    //mBinding?.incMain?.webViewPay?.evaluateJavascript("document.body.innerHTML = \"\";", null)

                                                    /*mBinding?.incMain?.webViewPay?.loadUrl(response.data.url)
                                                    lifecycleScope.launch(Dispatchers.Main) {
                                                        // 延迟1秒（1000毫秒）
                                                        delay(1300)
                                                        mBinding?.incMain?.webView?.visibility = View.GONE
                                                        mBinding?.incMain?.webViewPay?.visibility = View.VISIBLE
                                                    }*/
                                                }
                                                isUSDT = true

                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","ali----")
                                            }

                                        })
                                    }
                                    "stripePay" -> {
                                        ToastUtils.showLong(resources.getString(R.string.toast_loading))
                                        payType = "stripePay"
                                        MainModel.getStripeKey(chargeId, object : LoginCallback{
                                            override fun onSuccess(responseBody: String?) {
                                                Log.e("ceshi","stripe++++")
                                                val gson = Gson()
                                                Log.e("ceshi","ali+++++${responseBody}")
                                                val response = gson.fromJson(responseBody, ResponseStripeData::class.java)
                                                var publishableKey = ""
                                                publishableKey = response.data.jk+response.data.hk+response.data.pk+response.data.a
                                                Log.e("ceshi","ali+++++${publishableKey}")
                                                //sendDataToJavaScript(publishableKey)
                                                var url = "https://dash.proxy302.com/stripe?pk=$publishableKey&sid=${response.data.session_id}"
                                                Log.e("ceshi","url++++${url}")

                                                runOnUiThread {
                                                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,url,mWebView!!)
                                                    //mPayDetailDialog?.setUrlListener(url)
                                                    Log.e("ceshi","--url${url}")
                                                    //loadWebHandler1(url)
                                                }
                                            }

                                            override fun onFailure(errorCode: Int, errorMessage: String?) {
                                                Log.e("ceshi","stripe----")
                                            }

                                        })
                                    }
                                }
                            }

                        })

                    }
                }

                CommonHtmlUtil.payBillHtml -> {
                    //HtmlManager.jumpHtml(this@HomeActivity, data)
                    loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,data,mWebView!!)
                }
            }
        }
    }


    /*private fun applyLanguage() {
        val language = LanguageUtil.getSavedLanguage(this)
        Log.e("ceshi","获取的语言是:$language")
        val context = LanguageUtil.applyLanguage(this, language)
        val resources = context.resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics
        // 更新配置
        resources.updateConfiguration(configuration, displayMetrics)

    }*/
    private fun applyLanguage() {
        val language = LanguageUtil.getSavedLanguage(this)
        Log.e("ceshi","获取的语言是:$language")

        // 1. 获取新的上下文（已应用语言设置）
        val newContext = LanguageUtil.applyLanguage(this, language)

        // 2. 重启当前Activity，使新上下文生效
        val intent = intent // 获取当前Activity的启动意图
        finish() // 销毁当前Activity
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) // 重建Activity
        overridePendingTransition(0, 0) // 可选：去除切换动画
    }

    // 语言切换按钮点击事件
    fun onLanguageChange() {
        // 例如切换到中文
        LanguageUtil.changeLanguage(this, LanguageUtil.LANGUAGE_ZH)
    }

    private fun insertUserConfiguration(){
        //MyApplication.isFirstLaunch = MyApplication.sharedPreferences.getBoolean("isFirstLaunch", true)
        lifecycleScope.launch(Dispatchers.IO) {
            //val readAppEmojisData = dataStoreManager.readAppEmojisData.first()?:"\uD83D\uDE00"
            val readImageUrl = dataStoreManager.readImageUrl.first()?:""
            val readBuildTitleModelType = dataStoreManager.readBuildTitleModelType.first()?:"gpt-4o"
            val readModelType = dataStoreManager.readModelType.first()?:"gemini-2.5-flash-nothink"
            val readUserEmailData = dataStoreManager.readUserEmailData.first()?:""
            val systemLanguage = LanguageUtil.getSavedLanguage(this@SettingActivity)
            val systemTheme = SystemUtils.getSystemTheme(this@SettingActivity)
            val readSlideBottomSwitch = dataStoreManager.readSlideBottomSwitch.first()?:false
            val readUseTracelessSwitch = dataStoreManager.readUseTracelessSwitch.first()?:false
            var modelList = dataStoreManager.modelListFlow.first()
            val readSearchServiceType = dataStoreManager.readSearchServiceType.first()?:"search1api"
            val readBuildTitleTime = dataStoreManager.readBuildTitleTime.first()?:"第一次对话"

            chatDatabase.chatDao().insertUserConfig(UserConfigurationRoom(0,readUserEmailData,systemLanguage,"light",readUseTracelessSwitch,
                readSlideBottomSwitch,readImageUrl,readSearchServiceType,readModelType,readBuildTitleModelType,modelList,readBuildTitleTime))
            //dataStoreManager.saveAppEmojisData("\uD83D\uDE00")
            dataStoreManager.saveImageUrl("")
            dataStoreManager.saveBuildTitleModeTypeData("gpt-4o")
            dataStoreManager.saveModelType("gemini-2.5-flash-nothink")
            //dataStoreManager.saveModelList(modelListNull)
            dataStoreManager.saveSearchServiceTypeData("search1api")
            dataStoreManager.saveSlideBottomSwitch(false)
            dataStoreManager.saveUseTracelessSwitch(false)
            dataStoreManager.saveBuildTitleTimeData("第一次对话")
            dataStoreManager.saveUserEmail("")
        }



    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @SuppressLint("MissingInflatedId")
    fun showBottomPayDialog(context: Context) {
        var isSelectPay5 = false
        var isSelectPay20 = false
        var isSelectPay50 = false
        var isSelectPay100 = false
        var isSelectPay220 = false
        var isSelect = false
        var payId = 0
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_pay_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val payConst5 = view.findViewById<ConstraintLayout>(R.id.payConst5)
        val payConst20 = view.findViewById<ConstraintLayout>(R.id.payConst20)
        val payConst50 = view.findViewById<ConstraintLayout>(R.id.payConst50)
        val payConst100 = view.findViewById<ConstraintLayout>(R.id.payConst100)
        val payConst220 = view.findViewById<ConstraintLayout>(R.id.payConst220)
        val payDialogBackLine = view.findViewById<LinearLayout>(R.id.payDialogBackLine)

        val nextLine = view.findViewById<LinearLayout>(R.id.nextLine)

        val payProtocol = view.findViewById<TextView>(R.id.payProtocol)

        val paySelectImage5 = view.findViewById<ImageView>(R.id.paySelectImage5)
        val paySelectImage20 = view.findViewById<ImageView>(R.id.paySelectImage20)
        val paySelectImage50 = view.findViewById<ImageView>(R.id.paySelectImage50)
        val paySelectImage100 = view.findViewById<ImageView>(R.id.paySelectImage100)
        val paySelectImage220 = view.findViewById<ImageView>(R.id.paySelectImage220)

        val payTv5 = view.findViewById<TextView>(R.id.payTv5)
        val pay1Tv5 = view.findViewById<TextView>(R.id.pay1Tv5)
        val payTv20 = view.findViewById<TextView>(R.id.payTv20)
        val pay1Tv20 = view.findViewById<TextView>(R.id.pay1Tv20)
        val payTv50 = view.findViewById<TextView>(R.id.payTv50)
        val pay1Tv50 = view.findViewById<TextView>(R.id.pay1Tv50)
        val payTv100 = view.findViewById<TextView>(R.id.payTv100)
        val pay1Tv100 = view.findViewById<TextView>(R.id.pay1Tv100)
        val payTv220 = view.findViewById<TextView>(R.id.payTv220)
        val pay1Tv220 = view.findViewById<TextView>(R.id.pay1Tv220)

        payDialogBackLine.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnDismissListener {
            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.getUserInfo( WearData.getInstance().token,apiService)
            }
        }

        payConst5.setOnClickListener {
            if (!isSelectPay5){
                isSelectPay5 = true
                payId = 27
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage5.visibility = View.VISIBLE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay20 = false
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage20.visibility = View.GONE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay50 = false
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage50.visibility = View.GONE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay100 = false
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage100.visibility = View.GONE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay220 = false
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage220.visibility = View.GONE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.black))


            }else{
                isSelectPay5 = false
                payId = 0
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage5.visibility = View.GONE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        payConst20.setOnClickListener {
            if (!isSelectPay20){
                isSelectPay20 = true
                payId = 28
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage20.visibility = View.VISIBLE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay5 = false
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage5.visibility = View.GONE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay50 = false
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage50.visibility = View.GONE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay100 = false
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage100.visibility = View.GONE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay220 = false
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage220.visibility = View.GONE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.black))

            }else{
                isSelectPay20 = false
                payId = 0
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage20.visibility = View.GONE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        payConst50.setOnClickListener {
            if (!isSelectPay50){
                isSelectPay50 = true
                payId = 29
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage50.visibility = View.VISIBLE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay5 = false
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage5.visibility = View.GONE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay20 = false
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage20.visibility = View.GONE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay100 = false
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage100.visibility = View.GONE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay220 = false
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage220.visibility = View.GONE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.black))
            }else{
                isSelectPay50 = false
                payId = 0
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage50.visibility = View.GONE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        payConst100.setOnClickListener {
            if (!isSelectPay100){
                isSelectPay100 = true
                payId = 30
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage100.visibility = View.VISIBLE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay50 = true
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage50.visibility = View.VISIBLE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay5 = false
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage5.visibility = View.GONE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay20 = false
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage20.visibility = View.GONE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay50 = false
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage50.visibility = View.GONE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay220 = false
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage220.visibility = View.GONE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.black))
            }else{
                isSelectPay100 = false
                payId = 0
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage100.visibility = View.GONE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        payConst220.setOnClickListener {
            if (!isSelectPay220){
                isSelectPay220 = true
                payId = 31
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage220.visibility = View.VISIBLE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay50 = true
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_selected_line)
                paySelectImage50.visibility = View.VISIBLE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.color302AI))

                isSelectPay5 = false
                payConst5.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage5.visibility = View.GONE
                payTv5.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv5.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay20 = false
                payConst20.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage20.visibility = View.GONE
                payTv20.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv20.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay50 = false
                payConst50.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage50.visibility = View.GONE
                payTv50.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv50.setTextColor(ContextCompat.getColor(context, R.color.black))

                isSelectPay100 = false
                payConst100.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage100.visibility = View.GONE
                payTv100.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv100.setTextColor(ContextCompat.getColor(context, R.color.black))
            }else{
                isSelectPay220 = false
                payId = 0
                payConst220.setBackgroundResource(R.drawable.shape_select_site_bg_write_pay_line)
                paySelectImage220.visibility = View.GONE
                payTv220.setTextColor(ContextCompat.getColor(context, R.color.black))
                pay1Tv220.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        payProtocol.setOnClickListener {
            showBottomPayProtocolDialog(context)
        }
        nextLine.setOnClickListener {
            if (payId == 0){
                Toast.makeText(context, ContextCompat.getString(this@SettingActivity, R.string.pay_text_pay_toast_message), Toast.LENGTH_SHORT).show()
            }else{
                showBottomPayWayDialog(context,payId)
            }

        }


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomPayWayDialog(context: Context, payId:Int){
        var isAiliPay = false
        var isUsdtPay = false
        var isStripePay = false
        var payWay = ""
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_pay_way_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        val payWayProtocol = view.findViewById<TextView>(R.id.payWayProtocol)
        val payDialogWayBackLine = view.findViewById<LinearLayout>(R.id.payDialogWayBackLine)
        val payBalanceTv = view.findViewById<TextView>(R.id.payBalanceTv)

        val ailiPaySelectImage = view.findViewById<ImageView>(R.id.ailiPaySelectImage)
        val ailiPaySelectedImage = view.findViewById<ImageView>(R.id.ailiPaySelectedImage)

        val payCardSelectImage = view.findViewById<ImageView>(R.id.payCardSelectImage)
        val payCardSelectedImage = view.findViewById<ImageView>(R.id.payCardSelectedImage)

        val usdtSelectImage = view.findViewById<ImageView>(R.id.usdtSelectImage)
        val usdtSelectedImage = view.findViewById<ImageView>(R.id.usdtSelectedImage)

        val ailiPayCons = view.findViewById<ConstraintLayout>(R.id.ailiPayCons)
        val stripePayCons = view.findViewById<ConstraintLayout>(R.id.stripePayCons)
        val usdtPayCons = view.findViewById<ConstraintLayout>(R.id.usdtPayCons)

        val payWayConst = view.findViewById<ConstraintLayout>(R.id.payWayConst)
        val payWeb = view.findViewById<BridgeWebView>(R.id.payWeb)

        val payLine = view.findViewById<LinearLayout>(R.id.payLine)


        var payBalance = 5.00
        when(payId){
            27 -> {
                payBalance = 5.00
            }
            28 -> {
                payBalance = 20.00
            }
            29 -> {
                payBalance = 50.00
            }
            30 -> {
                payBalance = 100.00
            }
            31 -> {
                payBalance = 200.00
            }

        }

        payBalanceTv.text = "\$ ${payBalance.toString()}"


        payWayProtocol.setOnClickListener {
            showBottomPayProtocolDialog(context)
        }

        payDialogWayBackLine.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        ailiPayCons.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (!isAiliPay){
                isAiliPay = true
                isStripePay = false
                isUsdtPay = false
                payWay = "ailiPay"
                ailiPaySelectImage.visibility = View.GONE
                ailiPaySelectedImage.visibility = View.VISIBLE

                payCardSelectImage.visibility = View.VISIBLE
                payCardSelectedImage.visibility = View.GONE

                usdtSelectImage.visibility = View.VISIBLE
                usdtSelectedImage.visibility = View.GONE
            }else{
                isAiliPay = false
                payWay = ""
                ailiPaySelectImage.visibility = View.VISIBLE
                ailiPaySelectedImage.visibility = View.GONE
            }
        }

        stripePayCons.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (!isStripePay){
                isStripePay = true
                isAiliPay = false
                isUsdtPay = false
                payWay = "stripePay"
                payCardSelectImage.visibility = View.GONE
                payCardSelectedImage.visibility = View.VISIBLE

                ailiPaySelectImage.visibility = View.VISIBLE
                ailiPaySelectedImage.visibility = View.GONE

                usdtSelectImage.visibility = View.VISIBLE
                usdtSelectedImage.visibility = View.GONE
            }else{
                isStripePay = false
                payWay = ""
                payCardSelectImage.visibility = View.VISIBLE
                payCardSelectedImage.visibility = View.GONE
            }
        }

        usdtPayCons.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (!isUsdtPay){
                isUsdtPay = true
                isAiliPay = false
                isStripePay = false
                payWay = "usdtPay"
                usdtSelectImage.visibility = View.GONE
                usdtSelectedImage.visibility = View.VISIBLE

                payCardSelectImage.visibility = View.VISIBLE
                payCardSelectedImage.visibility = View.GONE

                ailiPaySelectImage.visibility = View.VISIBLE
                ailiPaySelectedImage.visibility = View.GONE
            }else{
                isUsdtPay = false
                payWay = ""
                usdtSelectImage.visibility = View.VISIBLE
                usdtSelectedImage.visibility = View.GONE
            }

        }

        payLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            when(payWay){
                "" -> {
                    Toast.makeText(context, ContextCompat.getString(this@SettingActivity, R.string.pay_text_pay_way_toast_message), Toast.LENGTH_SHORT).show()
                }
                "ailiPay" -> {
                    payWeb.visibility = View.VISIBLE
                    payWayConst.visibility = View.GONE
                    ToastUtils.showLong(resources.getString(R.string.toast_loading))
                    MainModel.getAliPayUrl(payId, object : LoginCallback {
                        override fun onSuccess(responseBody: String?) {
                            val gson = Gson()
                            Log.e("ceshi","ali+++++${responseBody}")
                            val response = gson.fromJson(responseBody, ResponseAliData::class.java)
                            Log.e("ceshi","ali+++++${response.data.to}")
                            runOnUiThread {
                                loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.to,payWeb)
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setData(Uri.parse(response.data.to))
                                ActivityUtils.startActivity(intent)
                            }

                        }

                        override fun onFailure(errorCode: Int, errorMessage: String?) {
                            Log.e("ceshi","ali----")
                        }

                    })
                }
                "stripePay" -> {
                    ToastUtils.showLong(resources.getString(R.string.toast_loading))
                    payWeb.visibility = View.VISIBLE
                    payWayConst.visibility = View.GONE
                    payType = "stripePay"
                    MainModel.getStripeKey(payId, object : LoginCallback {
                        override fun onSuccess(responseBody: String?) {
                            Log.e("ceshi","stripe++++")
                            val gson = Gson()
                            Log.e("ceshi","ali+++++${responseBody}")
                            val response = gson.fromJson(responseBody, ResponseStripeData::class.java)
                            var publishableKey = ""
                            publishableKey = response.data.jk+response.data.hk+response.data.pk+response.data.a
                            Log.e("ceshi","ali+++++${publishableKey}")
                            //sendDataToJavaScript(publishableKey)
                            var url = "https://dash.proxy302.com/stripe?pk=$publishableKey&sid=${response.data.session_id}"
                            Log.e("ceshi","url++++${url}")

                            runOnUiThread {
                                loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,url,payWeb)
                                //mPayDetailDialog?.setUrlListener(url)
                                Log.e("ceshi","--url${url}")
                                //loadWebHandler1(url)
                            }
                        }

                        override fun onFailure(errorCode: Int, errorMessage: String?) {
                            Log.e("ceshi","stripe----")
                        }

                    })
                }
                "usdtPay" -> {
                    ToastUtils.showLong(resources.getString(R.string.toast_loading))
                    payWeb.visibility = View.VISIBLE
                    payWayConst.visibility = View.GONE
                    MainModel.getUstdurl(payId, object : LoginCallback {
                        override fun onSuccess(responseBody: String?) {
                            val gson = Gson()
                            Log.e("ceshi","ali+++++${responseBody}")
                            val response = gson.fromJson(responseBody, ResponseUsdtData::class.java)
                            Log.e("ceshi","ali+++++${response.data.url}")
                            runOnUiThread {
                                loadWebHandler(CommonEnum.LoadHtmlType.DIRECT_LINK,response.data.url,payWeb)
                                //mPayDetailDialog?.setUrlListener(response.data.url)
                                Log.e("ceshi","++url${response.data.url}")
                                //mBinding?.incMain?.webViewPay?.evaluateJavascript("document.body.innerHTML = \"\";", null)

                                /*mBinding?.incMain?.webViewPay?.loadUrl(response.data.url)
                                lifecycleScope.launch(Dispatchers.Main) {
                                    // 延迟1秒（1000毫秒）
                                    delay(1300)
                                    mBinding?.incMain?.webView?.visibility = View.GONE
                                    mBinding?.incMain?.webViewPay?.visibility = View.VISIBLE
                                }*/
                            }
                            isUSDT = true

                        }

                        override fun onFailure(errorCode: Int, errorMessage: String?) {
                            Log.e("ceshi","ali----")
                        }

                    })
                }
            }
        }



        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    private fun showBottomPayProtocolDialog(context: Context){
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_pay_some_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        val payProtocolWeb = view.findViewById<WebView>(R.id.payProtocolWeb)

        val payDialogSomeBackLine = view.findViewById<LinearLayout>(R.id.payDialogSomeBackLine)
        when (defaultSystemLanguage) {
            LanguageUtil.LANGUAGE_ZH -> payProtocolWeb.loadDataWithBaseURL(null, payProtocolUrl, "text/html", "utf-8", null)
            LanguageUtil.LANGUAGE_JA -> payProtocolWeb.loadDataWithBaseURL(null, payProtocolUrlJa, "text/html", "utf-8", null)
            else -> payProtocolWeb.loadDataWithBaseURL(null, payProtocolUrlEn, "text/html", "utf-8", null)
        }


        payDialogSomeBackLine.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }
    val payProtocolUrl = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>用户充值协议</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: \"Microsoft Yahei\", sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            margin: 20px;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            background-color: #fff;\n" +
            "            padding: 20px;\n" +
            "            border: 1px solid #ddd;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        h1 {\n" +
            "            text-align: center;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        h2 {\n" +
            "            color: #333;\n" +
            "            margin-top: 20px;\n" +
            "            padding-bottom: 5px;\n" +
            "            border-bottom: 1px solid #eee;\n" +
            "        }\n" +
            "        p {\n" +
            "            margin: 10px 0;\n" +
            "        }\n" +
            "        ul {\n" +
            "            margin: 10px 0;\n" +
            "            padding-left: 20px;\n" +
            "        }\n" +
            "        li {\n" +
            "            margin: 5px 0;\n" +
            "        }\n" +
            "        .highlight {\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <h1>《用户充值协议》</h1>\n" +
            "        <p>尊敬的用户您好，在同意本协议前，请您认真阅读并充分知悉、理解302.AI的各项规则及要求，以及国家关于该类互联网信息服务的法律法规等。如果您对本协议的任何条款表示异议，您可以选择不使用；使用则意味着您将同意遵守本协议下全部规定，以及我们后续对使用协议随时所作的任何修改，并完全服从于我们的统一管理。本协议已对与您的权益有或可能具有重大关系的条款，及对302.AI具有或可能具有免责或限制责任的条款用粗体字予以标注，请您注意。</p>\n" +
            "        <p>请确认您具有完全民事行为能力，如您已年满18周岁，或您已年满16周岁且依靠自己的劳动收入作为主要生活来源，否则请您停止注册或使用本协议项下的服务。</p>\n" +
            "\n" +
            "        <h2>第一章 总则</h2>\n" +
            "        <p>第1条 302.AI（“302.AI”、“我们”或“我们”）是网站 302.AI（“本网站”），向用户提供的一个按用量付费的企业级AI平台。302.AI将为用户提供在线直接使用的应用超市和需要配置开发的API的服务。</p>\n" +
            "        <p>第2条 302.AI所有权、经营权、管理权均属302.AI。</p>\n" +
            "        <p>第3条 本协议最终解释权归属302.AI。</p>\n" +
            "\n" +
            "        <h2>第二章 PTC购买规则</h2>\n" +
            "        <p>第4条 PTC是302.AI向您提供的用于在302.AI上进行相关消费的虚拟货币，您可以用PTC自由购买302.AI上各项产品或服务。但是，PTC不能兑换为人民币或其他任何货币，您应根据自己的实际需求购买相应数量的PTC。</p>\n" +
            "        <p>第5条 您可以通过302.AI APP、302.AI官网:https://302.ai/等渠道购买PTC。</p>\n" +
            "        <p>第6条 于本协议签署日，“人民币”购买“PTC”的规则如下：</p>\n" +
            "        <ul>\n" +
            "            <li>1 PTC = 1 美元 ≈ 7 元人民币</li>\n" +
            "            <li>5 PTC = 5 美元 ≈ 35 元人民币</li>\n" +
            "            <li>20 PTC = 20 美元 ≈ 140 元人民币</li>\n" +
            "            <li>50 PTC = 50 美元 ≈ 350 元人民币</li>\n" +
            "            <li>100 PTC = 100 美元 ≈ 700 元人民币</li>\n" +
            "            <li>200 PTC = 200 美元 ≈ 1400 元人民币（赠送20 PTC）</li>\n" +
            "            <li>500 PTC = 500 美元 ≈ 3500 元人民币（赠送50 PTC）</li>\n" +
            "        </ul>\n" +
            "        <p>302.AI保留根据相关法律规定、主管部门要求、业务开展情况等因素对使用人民币购买PTC的规则进行单方面变更、调整、中止或终止的权利。您同意无条件接受对上述购买规则的变更、调整、中止或终止，302.AI开展前述行动时将以于302.AI公布的方式通知，并自公布之日起自动生效，而无需另行单独通知您，也不就该等行动给您造成的任何损失承担任何责任。</p>\n" +
            "        <p>第7条 在使用充值方式时，您务必仔细确认自己的账号并仔细选择相关操作选项。若因为您自身输入账号错误、操作不当或不了解充值计费方式等因素造成充错账号、错选充值种类等情形而损害自身权益的，302.AI将不会作出任何补偿或赔偿。</p>\n" +
            "        <p>第8条 若您以非法的方式，或使用非302.AI所指定的充值方式进行充值，302.AI不保证该充值顺利或者正确完成。若因此造成您权益受损，302.AI不会作出任何补偿或赔偿，302.AI同时保留随时终止您302.AI个人账号资格及使用各项充值服务的权利。</p>\n" +
            "        <p>第9条 充值成功后，充值所增加的账号内PTC可由您在302.AI上自由使用，但302.AI不会提供任何退还或逆向兑换服务。</p>\n" +
            "        <p>第10条 如果302.AI发现因系统故障或其他任何原因导致的处理错误，无论有利于302.AI还是有利于您，302.AI都有权在以电子邮件、微信或其他合理方式通知您后纠正该错误。如果该措施导致您实际收到的PTC数量少于您应获得的PTC，则302.AI在确认该处理错误后会尽快将差额补足至您的302.AI个人账户中。如果该错误导致您实际收到的PTC数量多于您应获得的PTC，则无论错误的性质和原因如何，302.AI有权从您的302.AI个人账户中直接扣除差额。</p>\n" +
            "\n" +
            "        <h2>第三章 权利声明</h2>\n" +
            "        <p>第11条 您可随时在手机APP上查看您的PTC的余额情况。如您对该记录有异议，应立即向302.AI提出，302.AI核对后确有错误的，将予以更正；否则您同意302.AI上的交易记录将作为PTC交易的唯一有效依据。</p>\n" +
            "        <p>第12条 302.AI有权基于交易安全等方面的考虑不时设定涉及交易的相关事项，包括但不限于交易限额、交易次数等。您了解，302.AI的前述设定可能对您的交易造成一定不便，您对此没有异议。</p>\n" +
            "        <p>第13条 在任何情况下，对于您购买PTC时涉及由第三方提供相关服务的责任由该第三方承担，302.AI不承担该等责任。</p>\n" +
            "        <p>第14条 进行充值时，您应确保您是绑定的支付宝账户持有人，可合法、有效使用该账户且未侵犯任何第三方合法权益，否则因此造成支付宝账户实际所有人损失的，您应单独负责解决由此产生的纠纷并承担全部法律责任。</p>\n" +
            "        <p>第15条 因您自身的原因导致302.AI无法提供PTC购买服务或提供PTC购买服务时发生任何错误而产生的任何损失或责任，由您自行负责，302.AI不承担责任，包括但不限于：</p>\n" +
            "        <ul>\n" +
            "            <li>（1）您未按照本协议或302.AI不时公布的任何规则进行操作；</li>\n" +
            "            <li>（2）因您的个人账号失效、丢失、被封停；</li>\n" +
            "            <li>（3）因您绑定的第三方支付机构账户的原因导致的损失或责任，包括您使用未认证的第三方支付账户或使用非您本人的第三方支付账户，您的第三方支付账户被冻结、查封等；</li>\n" +
            "            <li>（4）您将密码告知他人导致的财产损失；</li>\n" +
            "            <li>（5）因您个人的故意或重大过失所造成的财产损失。</li>\n" +
            "        </ul>\n" +
            "        <p>第16条 302.AI系统因下列状况无法正常运作，使您无法使用各项服务或任何虚拟财产丢失时，302.AI不承担损害赔偿责任，该状况包括但不限于：</p>\n" +
            "        <ul>\n" +
            "            <li>（1）在302.AI公告之系统停机维护、升级、调整期间；</li>\n" +
            "            <li>（2）电信通讯设备出现故障不能进行数据传输的；</li>\n" +
            "            <li>（3）因台风、地震、海啸、洪水、停电、战争、恐怖袭击、政府管制等不可抗力之因素，造成302.AI系统障碍不能执行业务的；</li>\n" +
            "            <li>（4）由于黑客攻击、电信部门技术调整或故障、网站升级、相关第三方的问题等原因而造成的服务中断或者延迟。</li>\n" +
            "        </ul>\n" +
            "\n" +
            "        <h2>第四章 处罚规则</h2>\n" +
            "        <p>第17条 如发生下列任何一种情形，302.AI有权随时中断或终止向您提供本协议项下的网络服务而无需通知您：(1) 您提供的个人资料不真实；（2）您违反本协议中规定的购买规则。除前款所述情形外，302.AI同时保留在不事先通知您的情况下随时中断或终止部分或全部网络充值服务的权利，对于充值服务的中断或终止而造成的任何损失，302.AI无需对您或任何第三方承担任何责任。</p>\n" +
            "        <p>第18条 如果用户违规使用非本人苹果手机应用商店代充值，或者通过其它非302.AI认可的渠道非法购买PTC，则302.AI有权冻结该账户，并进行相应惩罚，严重者可以进行封号处理。</p>\n" +
            "        <p>第19条 用户在使用302.AI时，如出现违反国家法律法规、《用户注册协议》约定、《用户兑换协议》约定、本协议约定或其他302.AI对用户的管理规定的情形，302.AI有权暂时或永久封禁您的账号。账号封禁后至解禁（如有）前，您账户上的剩余的PTC将被暂时冻结或全部扣除，不可继续用于购买平台上的虚拟产品或服务，同时不予返还您购买PTC时的现金价值。</p>\n" +
            "\n" +
            "        <h2>第五章 附则</h2>\n" +
            "        <p>第20条 302.AI保留修改或增补本协议内容的权利。本协议的修改文本将公告于302.AI或以其他302.AI认为可行的方式公告。修改文本一旦公布则立即生效，且对生效前的用户同样适用。若您在本协议内容发生修订后，继续使用本服务的，则视为您同意最新修订的协议内容；否则您须立即停止使用本服务。</p>\n" +
            "        <p>第21条 因本协议引起的或与本协议有关的争议，均适用中华人民共和国法律。</p>\n" +
            "        <p>第22条 因本协议引起的或与本协议有关的争议，302.AI与您将协商解决。协商不成的，任何一方均有权向本协议签订地有管辖权的法院提起诉讼。</p>\n" +
            "        <p>第23条 本协议部分内容被有管辖权的法院认定为违法或无效的，不因此影响其他内容的效力。</p>\n" +
            "        <p>第24条 本协议未涉及的问题参见国家有关法律法规，当本协议与国家法律法规冲突时，以国家法律法规为准。</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>".trimIndent()

    val payProtocolUrlJa = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>ユーザーリチャージ契約</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: \"Microsoft Yahei\", sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            margin: 20px;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            background-color: #fff;\n" +
            "            padding: 20px;\n" +
            "            border: 1px solid #ddd;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        h1 {\n" +
            "            text-align: center;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        h2 {\n" +
            "            color: #333;\n" +
            "            margin-top: 20px;\n" +
            "            padding-bottom: 5px;\n" +
            "            border-bottom: 1px solid #eee;\n" +
            "        }\n" +
            "        p {\n" +
            "            margin: 10px 0;\n" +
            "        }\n" +
            "        ul {\n" +
            "            margin: 10px 0;\n" +
            "            padding-left: 20px;\n" +
            "        }\n" +
            "        li {\n" +
            "            margin: 5px 0;\n" +
            "        }\n" +
            "        .highlight {\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <h1>《ユーザーリチャージ契約》</h1>\n" +
            "        <p>ユーザーの皆様、本契約に同意する前に、302.AIの各種規則および要件、ならびにこの種のインターネット情報サービスに関する国内法および規制をよくお読みになり、十分に理解してください。本契約のいずれかの条項に異議がある場合は、本契約の適用を中止することができます。本契約の適用は、本契約のすべての条項、ならびに当社が随時変更する利用規約の条項を遵守し、当社の統一的な管理に完全に従うことに同意することを意味します。本契約では、お客様の権利および利益に重大な影響を与える、または重大な影響を与える可能性のある条項、および302.AIの責任を免除または制限する、または免除または制限する可能性のある条項が太字で示されています。ご注意ください。</p>\n" +
            " <p>お客様が18歳以上である、または16歳以上で主な収入源として労働収入に依存しているなど、完全な民事行為能力を有していることを確認してください。そうでない場合は、本契約に基づくサービスの登録または利用を中止してください。</p>\n" +
            "\n" +
            " <h2>第1章 総則</h2>\n" +
            " <p>第1条 302.AI（以下「302.AI」または「当社」）は、302.AIのウェブサイト（以下「本サイト」）であり、ユーザーに従量課金制のエンタープライズレベルAIプラットフォームを提供します。302.AIは、ユーザーに直接利用可能なオンラインアプリケーションストアと、設定および開発を必要とするAPIサービスを提供します。</p>\n" +
            " <p>第2条 302.AIは、所有権、運営権、および管理権を有します。</p>\n" +
            " <p>第3条 302.AIは、本契約の最終的な解釈権を有します。</p>\n" +
            "\n" +
            " <h2>第2章 PTC購入規則</h2>\n" +
            " <p>第4条 PTCとは、302.AIが関連する購入のために提供する仮想通貨です。 302.AI。PTCは、302.AI上で様々な商品やサービスを自由にご購入いただけます。ただし、PTCは人民元（RMB）またはその他の通貨と交換することはできません。実際のニーズに応じて、必要な金額のPTCをご購入ください。</p>\n" +
            " <p>第5条 PTCは、302.AIアプリ、302.AI公式ウェブサイト（https://302.ai/）、その他のチャネルを通じてご購入いただけます。</p>\n" +
            " <p>第6条 本契約締結日現在、人民元でPTCをご購入いただく場合のルールは以下のとおりです。</p>\n" +
            " <ul>\n" +
            " <li>1 PTC = 1 USD ≈ 7 RMB</li>\n" +
            " <li>5 PTC = 5 USD ≈ 35 RMB</li>\n" +
            " <li>20 PTC = 20 USD ≈ 140 RMB</li>\n" +
            " <li>50 PTC = 50 USD ≈ 350 RMB</li>\n" +
            " <li>100 PTC = 100 USD ≈ 700 RMB</li>\n" +
            " <li>200 PTC = 200 USD ≈ 1400 RMB (20 PTCボーナス付き)</li>\n" +
            " <li>500 PTC = USD 500 ≈ 3500 RMB (50 PTCボーナス付き)</li>\n" +
            " </ul>\n" +
            " <p>302.AIは、関連法、規制要件、事業展開、その他の要因に基づき、人民元でのPTC購入に関する規則を一方的に変更、調整、停止、または終了する権利を留保します。お客様は、上記の購入規則の変更、調整、停止、または終了を無条件に受け入れることに同意するものとします。302.AIは、変更、調整、停止、または終了があった場合は、お客様に通知します。上記の購入規則は、302.AI上に公開することにより終了するものとし、これらの変更は、公開日から自動的に有効となり、お客様への通知なしに有効となり、302.AIは、かかる行為によってお客様に生じたいかなる損害についても一切責任を負いません。</p>\n" +
            " <p>第7条 トップアップ方法をご利用の際は、アカウントを慎重に確認し、関連するオプションを慎重に選択してください。誤ったアカウント入力、不適切な操作、トップアップ課金方法の理解不足などの要因により、誤ったアカウントにチャージしたり、誤ったトップアップタイプを選択したりすることでお客様の権利利益を損なった場合、302.AIはいかなる賠償または補償も行いません。</p>\n" +
            " <p>第8条 お客様が違法な手段でチャージしたり、302.AIが指定していないトップアップ方法を使用したりした場合、302.AIはトップアップがスムーズにまたは正確に完了することを保証しません。これによりお客様の権利利益が損なわれた場合、302.AIはいかなる賠償または補償も行いません。 302.AIは、お客様の302.AI個人アカウントの資格および各種トップアップサービスの利用をいつでも終了する権利を留保します。</p>\n" +
            " <p>第9条 トップアップが完了すると、お客様のアカウントに追加されたPTCは302.AIで自由にご利用いただけますが、302.AIは返金または交換サービスを提供しません。</p>\n" +
            " <p>第10条 302.AIは、システム障害またはその他の理由により、302.AIまたはお客様の利益にかかわらず、処理エラーを発見した場合、Eメール、WeChat、またはその他の合理的な手段でお客様に通知した後、エラーを修正する権利を留保します。この措置により、お客様が実際に受け取るPTCの数が、お客様が受け取るべき金額よりも少なくなる場合、302.AIは処理エラーを確認した後、速やかにお客様の302.AI個人アカウントに差額を返金します。 302.AIは、エラーの性質や原因に関わらず、お客様が受け取るべき金額よりも多く受け取った場合、その差額をお客様の302.AI個人アカウントから直接差し引く権利を留保します。</p>\n" +
            "\n" +
            " <h2>第3章 権利表明</h2>\n" +
            " <p>第11条 PTC残高はモバイルアプリでいつでも確認できます。記録に異議がある場合は、直ちに302.AIに提出してください。302.AIはエラーを確認し、発見された場合は修正します。それ以外の場合、お客様は302.AI上の取引記録がPTC取引の唯一の有効な根拠となることに同意するものとします。</p>\n" +
            " <p>第12条 302.AIは、取引のセキュリティ（取引限度額や取引時間などを含むがこれらに限定されない）などを考慮し、取引関連事項を随時設定する権利を有します。お客様は、302.AIによる上記の設定により、 </p>\n" +
            " <p>第13条 いかなる場合でも、PTC購入時に第三者が提供する関連サービスに関する責任は第三者が負うものとし、302.AIはそのような責任を負わないものとします。</p>\n" +
            " <p>第14条 チャージを行う際は、お客様が当該Alipayアカウントの所有者であること、アカウントを合法かつ有効に使用できること、および第三者の法的権利を侵害していないことを保証する必要があります。そうでない場合、Alipayアカウントの実際の所有者がその結果損失を被った場合、お客様は結果として生じる紛争の解決に単独で責任を負い、すべての法的責任を負うものとします。</p>\n" +
            " <p>第15条 302.AIがPTC購入サービスを提供できないこと、またはお客様自身の理由によりPTC購入サービスの提供に誤りがあったことに起因する損失または責任については、お客様が単独で責任を負うものとし、302.AIは責任を負いません。これには以下が含まれますが、これらに限定されません。</p>\n" +
            " <ul>\n" +
            " <li>（1）お客様が本契約または302.AIが随時発行する規則に従って操作を行わなかった場合。</li>\n" +
            " <li>（2）お客様の個人情報がアカウントが無効、紛失、またはブロックされている場合。</li>\n" +
            " <li>(3) お客様が紐付けた第三者決済機関のアカウントに関連する事由（認証されていない第三者決済アカウントまたはお客様自身のものではない第三者決済アカウントの使用、第三者決済アカウントの凍結または封印など）により発生した損失または責任。</li>\n" +
            " <li>(4) お客様がパスワードを他人に開示したことに起因する財産的損失。</li>\n" +
            " <li>(5) お客様の個人的な故意または重大な過失により発生した財産的損失。</li>\n" +
            " </ul>\n" +
            " <p>第16条 302.AIシステムは、以下の状況により正常に動作せず、お客様が各種サービスを利用できなくなったり、仮想資産が失われたりした場合、302.AIは、以下を含むがこれに限定されない損害について責任を負いません。</p>\n" +
            " <ul>\n" +
            " <li>(1) 302.AIが告知するシステムメンテナンス、アップグレード、調整期間中。</li>\n" +
            " <li>(2) 通信設備に故障が発生し、データの送信が不可能になった場合。</li>\n" +
            " <li>(3) 台風、地震、津波、洪水、停電、戦争、テロ攻撃、政府規制などの不可抗力により、302.AIシステムが業務を遂行できない場合。</li>\n" +
            " <li>(4) ハッカー攻撃、通信部門の技術調整または故障、ウェブサイトのアップグレード、関連する第三者の問題などにより、サービスの中断または遅延が発生した場合。</li>\n" +
            " </ul>\n" +
            "\n" +
            " <h2>第4章 罰則</h2>\n" +
            " <p>第17条 以下のいずれかの状況が発生した場合、 302.AIは、以下の場合、お客様に通知することなく、いつでも本契約に基づくお客様へのネットワークサービスの提供を中断または終了する権利を有します。(1) お客様が提供する個人情報が真実でない場合。(2) お客様が本契約に定める購入規則に違反した場合。前項に規定する状況に加えて、302.AIは、お客様に事前に通知することなく、いつでもネットワークリチャージサービスの一部または全部を中断または終了する権利も留保します。302.AIは、リチャージサービスの中断または終了によって生じたお客様または第三者の損害について、一切の責任を負いません。</p>\n" +
            " <p>第18条 ユーザーが本規則に違反し、自身のAppleモバイルアプリストア以外のストアでリチャージを行った場合、または302.AIが承認していない他のチャネルを通じてPTCを不正に購入した場合、302.AIはアカウントを凍結し、それに応じた罰則を科す権利を有します。重大な場合には、アカウントがブロックされる場合があります。</p>\n" +
            " <p>第18条19 ユーザーが302.AIの利用中に、国内法規、ユーザー登録契約、ユーザー償還契約、本契約、またはその他の302.AIユーザー管理規則に違反した場合、302.AIはユーザーのアカウントを一時的または永久的に禁止する権利を留保します。アカウントが禁止された時点から解除されるまで（該当する場合）、アカウントに残っているPTCは一時的に凍結されるか全額差し引かれ、プラットフォーム上で仮想製品またはサービスを購入するために使用できなくなります。PTC購入の現金価値は返金されません。</p>\n" +
            "\n" +
            " <h2>第5章 補足規定</h2>\n" +
            " <p>第20条 302.AIは、本契約の内容を変更または補足する権利を留保します。本契約の改訂版は、302.AIまたは302.AIが適切と判断するその他の方法で発表されます。改訂版は公開後直ちに発効し、本規約の改訂は、発効前に利用者に適用されます。本規約の内容が改訂された後も引き続き本サービスを利用する場合は、最新の改訂内容に同意したものとみなされます。そうでない場合は、直ちに本サービスの利用を中止してください。</p>\n" +
            " <p>第21条 本規約に起因または関連するすべての紛争は、中華人民共和国の法律に準拠するものとします。</p>\n" +
            " <p>第22条 本規約に起因または関連するすべての紛争は、302.AIと利用者との間の協議により解決されるものとします。協議により合意に至らない場合、いずれの当事者も、本規約が締結された場所を管轄する裁判所に訴訟を提起する権利を有します。</p>\n" +
            " <p>第23条 本規約の一部が管轄裁判所によって違法または無効と判断された場合でも、その他の内容の有効性は影響を受けません。</p>\n" +
            " <p>第24条 本規約に規定されていない事項については、本契約の適用範囲については、関連する国内法令をご参照ください。本契約と国内法令が抵触する場合は、国内法令が優先するものとします。</p>\n" +
            " </div>\n" +
            "</body>\n" +
            "</html>".trimIndent()

    val payProtocolUrlEn = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>User Recharge Agreement</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: \"Microsoft Yahei\", sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            margin: 20px;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            background-color: #fff;\n" +
            "            padding: 20px;\n" +
            "            border: 1px solid #ddd;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        h1 {\n" +
            "            text-align: center;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        h2 {\n" +
            "            color: #333;\n" +
            "            margin-top: 20px;\n" +
            "            padding-bottom: 5px;\n" +
            "            border-bottom: 1px solid #eee;\n" +
            "        }\n" +
            "        p {\n" +
            "            margin: 10px 0;\n" +
            "        }\n" +
            "        ul {\n" +
            "            margin: 10px 0;\n" +
            "            padding-left: 20px;\n" +
            "        }\n" +
            "        li {\n" +
            "            margin: 5px 0;\n" +
            "        }\n" +
            "        .highlight {\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <h1>《User Recharge Agreement》</h1>\n" +
            "<p>Dear user, before agreeing to this Agreement, please carefully read and fully understand the various rules and requirements of 302.AI, as well as the national laws and regulations regarding this type of internet information services. If you object to any term of this Agreement, you may choose not to use it; using it means that you agree to abide by all provisions of this Agreement, as well as any subsequent modifications we may make to the Terms of Use at any time, and fully submit to our unified management. This Agreement has marked in bold the terms that are or may be material to your rights and interests, and the terms that have or may exempt or limit the liability of 302.AI. Please pay attention. </p>\n" +
            " <p>Please confirm that you have full civil capacity, such as being 18 years of age or older, or being 16 years of age or older and relying on your own labor income as your primary source of income. Otherwise, please stop registering or using the services under this Agreement. </p>\n" +
            "\n" +
            " <h2>Chapter 1 General Provisions</h2>\n" +
            " <p>Article 1 302.AI ('302.AI', 'we', or 'us') is the website 302.AI ('this website'), which provides users with a pay-per-use enterprise-level AI platform. 302.AI will provide users with an online application supermarket for direct use and API services that require configuration and development. </p>\n" +
    " <p>Article 2 The ownership, operation rights, and management rights of 302.AI belong to 302.AI. </p>\n" +
    " <p>Article 3 The final right of interpretation of this Agreement belongs to 302.AI. </p>\n" +
    "\n" +
    " <h2>Chapter 2 PTC Purchase Rules</h2>\n" +
    " <p>Article 4 PTC is the virtual currency provided by 302.AI for related purchases on 302.AI. You can use PTC to freely purchase various products or services on 302.AI. However, PTC cannot be exchanged for RMB or any other currency. You should purchase the appropriate amount of PTC based on your actual needs. </p>\n" +
    " <p>Article 5 You can purchase PTC through channels such as the 302.AI app and the 302.AI official website: https://302.ai/. </p>\n" +
    " <p>Article 6: As of the date of this Agreement, the rules for purchasing PTC with RMB are as follows:</p>\n" +
    " <ul>\n" +
    " <li>1 PTC = US$1 ≈ RMB 7</li>\n" +
    " <li>5 PTC = US$5 ≈ RMB 35</li>\n" +
    " <li>20 PTC = US$20 ≈ RMB 140</li>\n" +
    " <li>50 PTC = US$50 ≈ RMB 350</li>\n" +
    " <li>100 PTC = US$100 ≈ RMB 700</li>\n" +
    " <li>200 PTC = US$200 ≈ RMB 1400 (with 20 PTC bonus)</li>\n" +
    " <li>500 PTC = 500 USD ≈ 3500 RMB (with 50 PTC bonus)</li>\n" +
    "</ul>\n" +
    " <p>302.AI reserves the right to unilaterally change, adjust, suspend, or terminate the rules for purchasing PTC using RMB based on relevant laws, regulatory requirements, business development, and other factors. You agree to unconditionally accept any changes, adjustments, suspension, or termination of these purchase rules. 302.AI will notify you of any such changes, adjustments, suspension, or termination by posting them on 302.AI, and these changes will automatically take effect from the date of posting without further notice to you, and 302.AI will not be liable for any losses incurred by you as a result of such actions.</p>\n" +
    " <p>Article 7: When using a top-up method, you must carefully verify your account and select the relevant options. 302.AI will not provide any compensation or reimbursement for any damages to your rights and interests caused by incorrect account number entry, improper operation, or lack of understanding of top-up billing methods. </p>\n" +
    " <p>Article 8 If you recharge in an illegal manner or using a recharge method not specified by 302.AI, 302.AI does not guarantee that the recharge will be completed smoothly or correctly. If your rights and interests are damaged as a result, 302.AI will not make any compensation or compensation. 302.AI also reserves the right to terminate your 302.AI personal account qualifications and use of various recharge services at any time. </p>\n" +
    " <p>Article 9 After a successful recharge, the PTC added to your account can be freely used on 302.AI, but 302.AI will not provide any refund or reverse exchange service. </p>\n" +
    " <p>Article 10 If 302.AI discovers a processing error caused by system failure or any other reason, regardless of whether it is beneficial to 302.AI or you, 302.AI has the right to correct the error after notifying you by email, WeChat or other reasonable means. If this action results in you receiving less PTC than you should have received, 302.AI will promptly credit the difference to your 302.AI personal account after confirming the processing error. If this error results in you receiving more PTC than you should have received, 302.AI reserves the right to directly deduct the difference from your 302.AI personal account, regardless of the nature and cause of the error. </p>\n" +
    "\n" +
    " <h2>Chapter 3 Rights Statement</h2>\n" +
    " <p>Article 11 You may check your PTC balance at any time on the mobile app. If you have any objections to this record, you should immediately notify 302.AI. If 302.AI verifies that an error is confirmed, it will be corrected. Otherwise, you agree that the transaction record on 302.AI will serve as the sole valid basis for PTC transactions. </p>\n" +
    " <p>Article 12 302.AI reserves the right to set transaction-related matters from time to time based on considerations such as transaction security, including but not limited to transaction limits and transaction times. You understand that the aforementioned settings of 302.AI may cause certain inconveniences to your transactions, and you have no objection to this. </p>\n" +
    " <p>Article 13 In any case, the responsibility for the services provided by a third party when you purchase PTC shall be borne by the third party, and 302.AI shall not bear such responsibility. </p>\n" +
    " <p>Article 14 When making a top-up, you shall ensure that you are the holder of the bound Alipay account, that you can use the account legally and effectively, and that you have not infringed upon the legal rights of any third party. Otherwise, if any losses are caused to the actual owner of the Alipay account, you shall be solely responsible for resolving any disputes arising therefrom and bear all legal liability. </p>\n" +
    " <p>Article 15 You are solely responsible for any losses or liabilities arising from 302.AI's inability to provide PTC purchase services or any errors in providing PTC purchase services due to your own reasons, and 302.AI shall not be liable, including but not limited to:</p>\n" +
    " <ul>\n" +
    " <li>（1）You fail to operate in accordance with this Agreement or any rules announced by 302.AI from time to time;</li>\n" +
    " <li>（2）Your personal account is invalid, lost, or blocked;</li>\n" +
    " <li>（3）Losses or liabilities caused by reasons related to your bound third-party payment institution account, including your use of an unauthenticated third-party payment account or a third-party payment account that is not yours, your third-party payment account is frozen or sealed, etc.;</li>\n" +
    " <li>（4）Property losses caused by your disclosure of your password to others;</li>\n" +
    " <li>(5) Property loss caused by your personal intentional or gross negligence. </li>\n" +
    " </ul>\n" +
    " <p>Article 16 302.AI system fails to operate normally due to the following conditions, which makes you unable to use various services or lose any virtual property. 302.AI shall not be liable for damages. Such conditions include but are not limited to: </p>\n" +
    " <ul>\n" +
    " <li>(1) During the system downtime maintenance, upgrade, and adjustment period announced by 302.AI; </li>\n" +
    " <li>(2) Telecommunications equipment fails and data transmission cannot be carried out; </li>\n" +
    " <li>(3) Due to force majeure factors such as typhoons, earthquakes, tsunamis, floods, power outages, wars, terrorist attacks, government regulations, etc., 302.AI system is blocked and cannot perform business; </li>\n" +
    " <li>(4) Service interruption or delay due to hacker attacks, technical adjustments or failures of telecommunications departments, website upgrades, problems with related third parties, etc. </li>\n" +
    " </ul>\n" +
    "\n" +
    " <h2>Chapter 4 Penalty Rules</h2>\n" +
    " <p>Article 17 If any of the following circumstances occurs, 302.AI has the right to interrupt or terminate the provision of network services to you under this Agreement at any time without notifying you: (1) The personal information you provide is untrue; (2) You violate the purchase rules stipulated in this Agreement. In addition to the circumstances described in the preceding paragraph, 302.AI also reserves the right to interrupt or terminate part or all of the network recharge service at any time without prior notice to you. 302.AI does not need to bear any responsibility for any losses caused by the interruption or termination of the recharge service. </p>\n" +
    " <p>Article 18 If a user violates regulations by using an Apple App Store account not authorized by the user, or illegally purchases PTC through other channels not approved by 302.AI, 302.AI reserves the right to freeze the account and impose penalties. In serious cases, the account may be blocked. </p>\n" +
    " <p>Article 19: If a user violates national laws and regulations, the User Registration Agreement, the User Redemption Agreement, this Agreement, or other 302.AI user management regulations while using 302.AI, 302.AI reserves the right to temporarily or permanently block your account. From the time your account is blocked until it is unblocked (if any), the remaining PTC in your account will be temporarily frozen or fully deducted and cannot be used to purchase virtual products or services on the platform. The cash value of your PTC purchase will not be refunded. </p>\n" +
    "\n" +
    " <h2>Chapter V Supplementary Provisions</h2>\n" +
    " <p>Article 20 302.AI reserves the right to modify or supplement the contents of this Agreement. The revised text of this Agreement will be announced on 302.AI or in other ways that 302.AI deems feasible. The revised text will take effect immediately upon publication and will also apply to users before it takes effect. If you continue to use this service after the content of this Agreement is revised, it will be deemed that you agree to the latest revised content of the Agreement; otherwise, you must immediately stop using this service. </p>\n" +
    " <p>Article 21 All disputes arising from or related to this Agreement shall be subject to the laws of the People's Republic of China. </p>\n" +
    " <p>Article 22 All disputes arising from or related to this Agreement shall be resolved through negotiation between 302.AI and you. If no agreement is reached through negotiation, either party shall have the right to file a lawsuit in the court with jurisdiction over the place where this Agreement was signed. </p>\n" +
    " <p>Article 23 If part of this Agreement is found to be illegal or invalid by a court with jurisdiction, the validity of other contents shall not be affected thereby. </p>\n" +
    " <p>Article 24 For matters not covered by this Agreement, please refer to relevant national laws and regulations. In the event of a conflict between this Agreement and national laws and regulations, national laws and regulations shall prevail. </p>\n" +
    " </div>\n" +
    "</body>\n" +
    "</html>".trimIndent()


}