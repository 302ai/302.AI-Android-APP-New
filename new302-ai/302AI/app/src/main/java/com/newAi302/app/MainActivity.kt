package com.newAi302.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PorterDuff
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.adapter.ChatAdapter
import com.newAi302.app.adapter.HomeMessageAdapter
import com.newAi302.app.data.BackChatToolItem
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.data.ChatTitle
import com.newAi302.app.data.ImageBack
import com.newAi302.app.databinding.ActivityMainBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.dialog.FeedBackDialog
import com.newAi302.app.dialog.RenameDialog
import com.newAi302.app.http.ApiService
import com.newAi302.app.http.NetworkFactory
import com.newAi302.app.http.NetworkModule
import com.newAi302.app.infa.OnChatTitleSelectedListener
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.infa.OnPromptSelectedListener
import com.newAi302.app.infa.OnWordPrintOverClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemChat
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.screenshot.ScreenShotTools
import com.newAi302.app.screenshot.model.ScreenBitmap
import com.newAi302.app.screenshot.model.i.IScreenShotCallBack
import com.newAi302.app.service.ScreenshotService
import com.newAi302.app.ui.LoginActivity
import com.newAi302.app.ui.ResourceActivity
import com.newAi302.app.ui.SettingActivity
import com.newAi302.app.ui.ShowScreenImageActivity
import com.newAi302.app.utils.CommonDialogUtils
import com.newAi302.app.utils.CommonDialogUtils.showBottomSheetCodePreDialog
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.DrawableToUriUtil
import com.newAi302.app.utils.LongScreenshotManager
import com.newAi302.app.utils.PermissionUtils.checkRecordPermission
import com.newAi302.app.utils.RecyclerViewScreenshotUtils
import com.newAi302.app.utils.ScreenUtils
import com.newAi302.app.utils.ScreenUtils.getRecyclerViewContentHeight
import com.newAi302.app.utils.ScreenshotSaver
import com.newAi302.app.utils.StringObjectUtils
import com.newAi302.app.utils.SystemUtils
import com.newAi302.app.utils.TimeUtils
import com.newAi302.app.utils.TtsManagerUtils
import com.newAi302.app.utils.UriToFileUtils.convertUriToFile
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.VoiceToTextUtils
import com.newAi302.app.view.RemovableImageLayout
import com.newAi302.app.viewModel.ChatViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newAi302.app.MyApplication.Companion.myApplicationContext
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.data.MainMessage
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.room.ChatRepository
import com.newAi302.app.room.UserConfigurationRoom
import com.newAi302.app.screenshot.model.config.PermissionConst
import com.newAi302.app.ui.login.LoginOneActivity
import com.newAi302.app.utils.ActivityManager
import com.newAi302.app.utils.DeviceDetector
import com.newAi302.app.utils.ImageToGalleryUtil
import com.newAi302.app.utils.LanguageUtil
import com.newAi302.app.utils.PermissionUtils
import com.newAi302.app.utils.ScreenUtils.getScreenHeight
import com.newAi302.app.utils.base.WearData
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.notifyAll
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean


@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : BaseActivity(), OnItemClickListener, OnWordPrintOverClickListener {
    private lateinit var binding: ActivityMainBinding
    private var inputStr = ""
    private lateinit var messageAdapter: ChatAdapter
    private var messageList = mutableListOf<ChatMessage>()
    private val chatViewModel: ChatViewModel by viewModels()

    private var userId = ""
    private var apiKey = ""
    private val BASE_URL = "https://api.302.ai/"
    private val BASE_URL1 = "https://gptutils-chat.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    private var mMessageList = mutableListOf<String>()
    private var chatList = mutableListOf<ChatItemRoom>()

    private var chatListReversed = mutableListOf<ChatItemRoom>()

    private var chatListSearch = mutableListOf<ChatItemRoom>()
    private var chatListTitleSearch = mutableListOf<ChatItemRoom>()
    private lateinit var adapterHistorySearch: HomeMessageAdapter

    private lateinit var adapterHistory: HomeMessageAdapter
    private lateinit var chatDatabase: ChatDatabase

    private var chatId = 0
    private var chatTitle = "新会话"
    private var chatTime = ""
    private var modelType = "gemini-2.5-flash-nothink"
    private var buildTitleModelType = "gpt-4o"
    private var isMe = true
    private var isDeepThink = false
    private var isNetWorkThink = false
    private var isMcp = false
    private var isR1Fusion = false
    private var isHaveTitle = false

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private val FILE_IMAGE_REQUEST = 3
    private lateinit var currentPhotoPath: String
    private var isPicture = false
    private var imageUrlLocal = ""
    private var urlLocal = ""
    private var isToPicture = false
    private var imageUrlLocalList = CopyOnWriteArrayList<String>()
    private var imageUrlServiceResult = ""
    private var imageUrlServiceResultList = mutableListOf<String>()//CopyOnWriteArrayList
    private var imageUrlServiceResultList1 = mutableListOf<String>()//CopyOnWriteArrayList
    private var mImageUrlLocalList = mutableListOf<String>()
    private var mImageUrlServiceResultList = mutableListOf<String>()
    private var imageCounter = 0
    private var modelTypeHistory = ""
    private var mModelTypeHistory = ""

    private var netUrlResultList = mutableListOf<String>()//CopyOnWriteArrayList

    private lateinit var dataStoreManager: DataStoreManager

    //private var modelList = mutableListOf<String>()
    // 修改后（线程安全）
    private var modelList = CopyOnWriteArrayList<String>()
    private var isTrueApiKey = false

    private var isComeFromSetting = false

    private lateinit var dialogUtils: DialogUtils

    private var selectedList = mutableListOf<Int>()

    private lateinit var screenshotManager: LongScreenshotManager
    private val REQUEST_MEDIA_PROJECTION = 1001
    // 用于打开文件选择器的请求码
    private val REQUEST_CODE_OPEN_DOCUMENT = 1002

    private var isPrivate = false

    private var longPressStartTime: Long = 0L  // 长按触发的时间戳
    private var isLongPressed = false  // 标记是否已触发长按
    private val audioFilePath: String by lazy {
        "${getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)}/temp_audio.mp3"
    }
    // 新增变量：记录按下位置和滑动阈值（单位：像素）
    private var touchDownY: Float = 0f
    private val SWIPE_THRESHOLD = 100 // 可调整的上滑阈值

    private var selectedFileUri: Uri? = null
    private var isFile = false
    private var mPicFileUri: Uri? = null
    private var fileName = ""
    private var fileSize = ""
    private var fileNameList: MutableList<String> = mutableListOf()
    private var fileSizeList: MutableList<String> = mutableListOf()

    private var prompt = "这是删除过的内容变为空白"
    private var temperature:Double = 0.5

    private var nowChatPositon = 0

    private var isUseTracelessSwitch = false
    private var isSlideBottomSwitch = false
    private var isBuildTitleFirstTime = true
    private var isHasGetTitle = false
    private var searchServiceType = ""
    private var moreFunctionQuantity = 0

    private var isChatRes = false

    private var badPosition = 0

    //原子
    private val isSendMessage = AtomicBoolean(false)
    private val isSendMessageAll = AtomicBoolean(true)

    private val isSendMessageAgain = AtomicBoolean(false)

    private val leftDelect = AtomicBoolean(false)

    private lateinit var repository: ChatRepository

    private lateinit var vibrator: Vibrator

    private var isScreenTurnedOff = false

    private var isTemporary = false

    private var hideTitle = ""

    private var mReadImageUrl = ""

    private val urlMapper = ImageUrlMapper(myApplicationContext)

    private var isOpenFile = false

    // 保存 chatRecyclerView 的初始布局参数
    private var initialRecyclerLayoutParams: RelativeLayout.LayoutParams? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenTurnedOff = true
                    Log.e("ceshi", "屏幕关闭")
                }
                Intent.ACTION_SCREEN_ON -> {
                    //isScreenTurnedOff = false
                    Log.e("ceshi", "屏幕开启")
                }
            }
        }
    }




    @RequiresApi(35)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.e("ceshi","onCreate")
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 注册屏幕状态广播
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
        //screenshotManager = LongScreenshotManager(this)
        chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
        hideTitle = ContextCompat.getString(this@MainActivity, R.string.hide_title)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        // 获取震动服务
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //录音权限检测
        //checkRecordPermission(this)
        //TtsManagerUtils.initTts(this)
        chatTime = TimeUtils.getCurrentDateTime()
        initObserver()
        if (WearData.getInstance().token != ""){
            isTrueApiKey = true
            binding.modeTypeTv.visibility = View.VISIBLE
        }else{
            isTrueApiKey = false
        }
        initData()
        initView()
        saveInitialRecyclerLayoutParams()

    }

    override fun onStop() {
        super.onStop()
        Log.e("ceshi","onStop,,模型：$modelType,,$chatTitle")
        isTemporary = true
        isHaveTitle = false
        isSendMessage.set(false)
        moreFunctionQuantity = 0
        lifecycleScope.launch(Dispatchers.IO) {

            //直接插入，做了title唯一性，如果有了就替换成最新的
            if (!chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                //因为插入id是自动生成，所以不要自己去做插入
                chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, chatTime,modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
            }

            dataStoreManager.saveLastModelType(modelType)
            dataStoreManager.saveTemporaryModelType(modelType)
            dataStoreManager.saveTemporaryChatTitle(chatTitle)

            //Log.e("ceshi", "B插入完成时间：${System.currentTimeMillis()}") // 添加日志

        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("ceshi","onPause")

        // 判断是否因屏幕关闭触发 onPause
//        val pm = getSystemService(POWER_SERVICE) as PowerManager
//        isScreenTurnedOff = !pm.isInteractive


        Log.e("ceshi","0是否息屏$isScreenTurnedOff")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("ceshi","onRestart")

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onResume() {
        super.onResume()
        Log.e("ceshi","onResume获取到${WearData.getInstance().token != ""}")
        if (WearData.getInstance().token != ""){
            isTrueApiKey = true
            binding.modeTypeTv.visibility = View.VISIBLE
        }else{
            isTrueApiKey = false
        }
        //applyLanguage()
        // 每次 resume 时都确保只保留当前页面
        ActivityManager.finishAllExcept(this)
        Log.e("ceshi","onResume")
        binding.modeTypeTv.visibility = View.GONE
        val comeFrom = intent.getSerializableExtra("come_from") as? String
        val setMsg = intent.getSerializableExtra("msg_setting") as? MainMessage
        Log.e("ceshi","onResume返回信息$comeFrom")
        if (comeFrom != null){
            if (comeFrom == "setting"){
                isComeFromSetting = true
                buildNewChat(false)
            }
        }
        Log.e("ceshi","onResume0返回信息$setMsg")
        if ( setMsg != null ){
            // 初始化 UI 文本（使用当前语言配置）
            refreshUI(setMsg)
        }
        if (isDeepThink){
            moreFunctionQuantity++
        }
        if (isNetWorkThink){
            moreFunctionQuantity++
        }
        if (isR1Fusion){
            moreFunctionQuantity++
        }
        if (moreFunctionQuantity>0){
            binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
            binding.moreFunctionLine.visibility = View.VISIBLE
            binding.moreIdTv.text = moreFunctionQuantity.toString()
            binding.moreImage.setImageResource(R.drawable.icon_new_more1)
            binding.moreImage.setColorFilter(ContextCompat.getColor(this, R.color.color302AI), PorterDuff.Mode.SRC_IN)
        }else{
            binding.moreFunctionLine.visibility = View.GONE
            binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
            binding.moreImage.setImageResource(R.drawable.icon_new_more1)
            binding.moreImage.clearColorFilter()
        }
        binding.chatTitleTv.text = chatTitle
        lifecycleScope.launch(Dispatchers.IO) {
            chatViewModel.getUserInfo( WearData.getInstance().token,apiService)
            val data = dataStoreManager.readData.first()
            data?.let {
                Log.e("ceshi","appKey是多少：$it")
                apiKey = it
                chatViewModel.get302AiModelList(it,apiService)
            }

            val readCueWords = dataStoreManager.readCueWords.first()
            readCueWords?.let {
                Log.e("ceshi","提示词是多少：$it")
                prompt = it
            }

            val readUserNameData = dataStoreManager.readUserNameData.first()


            val readTemperatureValue = dataStoreManager.readTemperatureValue.first()
            readTemperatureValue?.let {
                temperature = it
            }

            val readModelType = dataStoreManager.readModelType.first()?:"gemini-2.5-flash-nothink"
            readModelType?.let {
                Log.e("ceshi","提示词是多少：$it")
                modelType = it
            }
            if (isTemporary){
                val readTemporaryModelType = dataStoreManager.readTemporaryModelType.first()?:"gemini-2.5-flash-nothink"
                readTemporaryModelType?.let {
                    Log.e("ceshi","临时保存是多少：$it")
                    modelType = it
                }

                val readTemporaryChatTitle = dataStoreManager.readTemporaryChatTitle.first()?:""
                readTemporaryChatTitle?.let {
                    Log.e("ceshi","临时保存标题是多少：$it")
                    chatTitle = it
                }

            }
            if (mModelTypeHistory != ""){
                modelType = mModelTypeHistory
            }
            if (modelTypeHistory != ""){
                modelType = modelTypeHistory
            }
            val readBuildTitleModelType = dataStoreManager.readBuildTitleModelType.first()?:"gpt-4o"
            readBuildTitleModelType?.let {
                Log.e("ceshi","提示词是多少：$it")
                buildTitleModelType = it
            }

            val readUserEmailData = dataStoreManager.readUserEmailData.first()?:""
            Log.e("ceshi","获取账号$readUserEmailData")
            if (readUserEmailData != ""){
                isTrueApiKey = true
            }



            val readUseTracelessSwitch = dataStoreManager.readUseTracelessSwitch.first()?:false
            val readSlideBottomSwitch = dataStoreManager.readSlideBottomSwitch.first()?:false

            isUseTracelessSwitch = readUseTracelessSwitch
            isSlideBottomSwitch = readSlideBottomSwitch


            val readBuildTitleTime = dataStoreManager.readBuildTitleTime.first()?:"第一次对话"
            if (readBuildTitleTime == "第一次对话"){
                isBuildTitleFirstTime = true
            }else{
                isBuildTitleFirstTime = false
            }

            val readSearchServiceType = dataStoreManager.readSearchServiceType.first()?:"search1api"
            searchServiceType = readSearchServiceType

            //val readAppEmojisData = dataStoreManager.readAppEmojisData.first()
            mReadImageUrl = dataStoreManager.readImageUrl.first()?:""


            // 2. 切换主线程统一更新 UI（避免多次切换线程）
            withContext(Dispatchers.Main){
                Log.e("ceshi","是否息屏$isScreenTurnedOff")
                if (!isScreenTurnedOff){
                    binding.modeTypeTv.text = modelType
                }


                readUserNameData?.let {
                    Log.e("setting","readUserNameData：$it")
                    binding.userName.text = it
                }
                slideBottom()
                if (messageList.isEmpty()){
                    if (isUseTracelessSwitch){
                        isPrivate = true
                        binding.hideImage.setImageResource(R.drawable.icon_hide)
                        //binding.hideImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                        binding.hideImage.imageTintList = ContextCompat.getColorStateList(
                            this@MainActivity,
                            R.color.color302AI
                        )
                        chatTitle = hideTitle
                        binding.chatTitleTv.text = hideTitle
                    }else{
                        Log.e("ceshi","6是否息屏$isScreenTurnedOff,,$chatTitle")
                        if (chatTitle == hideTitle){
                            isPrivate = true
                            binding.hideImage.setImageResource(R.drawable.icon_hide)
                            //binding.hideImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                            binding.hideImage.imageTintList = ContextCompat.getColorStateList(
                                this@MainActivity,
                                R.color.color302AI
                            )
                            chatTitle = hideTitle
                            binding.chatTitleTv.text = hideTitle
                        }else{
                            isPrivate = false
                            binding.hideImage.setImageResource(R.drawable.icon_hide)
                            //binding.hideImage.clearColorFilter()
                            binding.hideImage.imageTintList = null
                            chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                            binding.chatTitleTv.text = chatTitle
                        }

                    }
                }

                /*readAppEmojisData?.let {
                    Log.e("setting","readAppEmojisData是多少：$it")
                    binding.userImage.text = it
                }*/
                // 方法1：使用内置的CircleCrop变换
                Glide.with(this@MainActivity)
                    .load(mReadImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.userImage)

                if (data == ""){
                    binding.modeTypeTv.visibility = View.GONE
                }
            }

        }

        // 添加滚动监听
        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                Log.e("ceshi","屏幕高度：${getScreenHeight(this@MainActivity)/2},,$totalItemCount,,$lastVisibleItemPosition")
                Log.e("ceshi","recycle屏幕高度：${layoutManager.height}")
                // 当聊天内容超出屏幕且不是最后一项可见时显示按钮
                if (totalItemCount>=4 && (totalItemCount-lastVisibleItemPosition>1)) {
                    binding.floatingButton.visibility = View.VISIBLE
                } else {
                    binding.floatingButton.visibility = View.GONE
                }

                // 判断滑动方向：dy > 0 表示向下滑动（垂直方向滚动距离）
                if (dy > 0) {
                    Log.d("ScrollListener", "手指向下滑动，dy = $dy")
                    // 在这里处理向下滑动的逻辑（如加载更多、隐藏顶部控件等）
                    // 示例：显示"向下滑动中"
                    // showToast("向下滑动中...")
                } else if (dy < 0) {
                    Log.d("ScrollListener", "手指向上滑动，dy = $dy")



                }
            }

            // 滚动状态变化时触发（开始滚动、停止滚动等）
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        Log.d("ScrollListener", "滚动停止")
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        Log.d("ScrollListener", "手指正在拖动")
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        Log.d("ScrollListener", "惯性滚动中")
                    }
                }
            }


        })


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("ceshi","onDestroy")
        isScreenTurnedOff = false
        isTemporary = false
        unregisterReceiver(screenReceiver)
        //TtsManagerUtils.TtsStop()
//        Glide.with(this).clear(binding.userImage) // 清理单个 View
//        // 清理所有未完成的请求
//        Glide.get(this).clearMemory()
        // 释放震动资源
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            vibrator.cancel()
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initView(){

        // 设置按钮点击事件
        binding.floatingButton.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(binding.floatingButton)
            //(binding.chatRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
            if (messageList.isNotEmpty()) {
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
        }

        // 设置布局管理器
//        val layoutManager = LinearLayoutManager(this)
//        binding.chatRecyclerView.layoutManager = layoutManager
        /*messageList.add(ChatMessage("你好，你是谁？",true))
        messageList.add(ChatMessage("我是数据大模型，有什么需要帮助的吗？",false))
        for (i in 1..100){
            if (i % 2 == 0){
                messageList.add(ChatMessage("我是数据大模型，有什么需要帮助的吗？$i",true))
            }else{
                messageList.add(ChatMessage("我是数据大模型，有什么需要帮助的吗？$i",false))
            }

        }*/
        messageAdapter = ChatAdapter(messageList,this,this)
//        binding.chatRecyclerView.adapter = adapter
        // 初始化 chatRecyclerView 时添加
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true) // 固定尺寸，减少测量
            adapter = messageAdapter
            // 可选：添加 Item 缓存（默认是 2，可根据需求增加）
            recycledViewPool.setMaxRecycledViews(0, 5)
        }

        if (nowChatPositon != 0){
            binding.chatRecyclerView.layoutManager?.scrollToPosition(nowChatPositon)
        }


        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            private var lastLineCount = 0
            // 用于记录上一次的输入状态，避免重复触发
            private var hasContent = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastLineCount = binding.messageEditText.lineCount
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 检查是否有新行被添加
                /*if (binding.messageEditText.lineCount > lastLineCount) {
                    inputStr = s.toString()
                    onLineBreak(s.toString())
                }*/
            }

            override fun afterTextChanged(s: Editable?) {
                // 可在这里处理其他逻辑
                // 实时监听输入内容
                val currentHasContent = s?.isNotEmpty() == true

                // 只有当状态发生变化时才触发（空→有内容 或 有内容→空）
                if (currentHasContent != hasContent) {
                    hasContent = currentHasContent
                    if (hasContent) {
                        // 输入框有内容
                        binding.sendImage.visibility = View.VISIBLE
                        //binding.noSendImage.visibility = View.GONE
                        binding.mikeImage.visibility = View.GONE
                    } else {
                        // 输入框为空
                        binding.sendImage.visibility = View.GONE
                        //binding.noSendImage.visibility = View.VISIBLE
                        binding.mikeImage.visibility = View.VISIBLE
                    }
                }
            }
        })

        // 设置发送按钮的点击事件
        binding.sendImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            performVibration()
           if (isTrueApiKey){
               isSendMessageAll.set(false)
               val message = binding.messageEditText.text.toString().trim()
               Log.e("ceshi","点击")
               if (message.isNotEmpty()) {
                   //发送消息后隐私按钮消失
                   binding.hideImage.visibility = View.GONE
                   if (isPicture || isFile){
                       if (imageCounter != imageUrlServiceResultList.size && imageUrlServiceResultList.isEmpty()){//imageUrlServiceResult == "" || imageUrlServiceResultList.isEmpty() || (imageUrlServiceResultList.size-1) != imageCounter
                           Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.parsing_image_toast_message), Toast.LENGTH_SHORT).show()
                           return@setOnClickListener
                       }
                       isPicture = false
                       var mImageUrlLocalStrBulder = StringBuilder()
                       for (imagelocal in imageUrlLocalList){
                           mImageUrlLocalStrBulder.append("<img src=\"$imagelocal\" width=\"150\" height=\"150\">")
                           //mImageUrlLocalStrBulder.append("![示例图片](content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20250725_153926.jpg)")
                           // ![示例图片](content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20250725_153926.jpg)

                       }
                       //Log.e("ceshi","返回的URL${imageUrlServiceResultList.size},,$imageCounter")
                       Log.e("ceshi","文件插入：${isFile},,${fileName},,$fileSize")
                       if (isFile){
                           isFile = false
                           messageList.add(ChatMessage("${mImageUrlLocalStrBulder.toString()}<br>$message",true,"chat",false,false,fileNameList.toMutableList(), fileSizeList.toMutableList()))
                           fileName = ""
                           fileSize = ""
                       }else{
                           messageList.add(ChatMessage("${mImageUrlLocalStrBulder.toString()}<br>$message",true,"chat",false,false))
                       }

                       messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false))
                       mMessageList.add(message)
                       mImageUrlLocalStrBulder.clear()

                   }else{
                       messageList.add(ChatMessage(message,true,"",false,false))
                       messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false))
                       mMessageList.add(message)
                   }

                   messageAdapter.upDateIsNewChat(false)
                   lifecycleScope.launch(Dispatchers.IO) {
                       val model = "gpt-4o-image-generation"
                       isSendMessage.set(true)
                       chatViewModel.sendQuestion(message,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,false,apiKey,false,
                           apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)

                           fileNameList.clear()
                           fileSizeList.clear()
                       //Log.e("ceshi","现在的聊天数量:${messageList.size}")
                       //标题生成
                       if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                           chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(message),buildTitleModelType,apiKey,apiService)
                       }else if (!isBuildTitleFirstTime && !isPrivate){
                           chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(message),buildTitleModelType,apiKey,apiService)
                       }else if (isBuildTitleFirstTime && messageList.size ==2 && !isPrivate ){
                           chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(message),buildTitleModelType,apiKey,apiService)
                       }

                       mImageUrlServiceResultList.clear()
                       mImageUrlLocalList.clear()
                       imageUrlLocalList.clear()
                       imageUrlServiceResultList.clear()
                       imageCounter = 0
//                       delay(3000)
//                       fileNameList.clear()
//                       fileSizeList.clear()

                   }
                   binding.newChatCon.visibility = View.GONE
                   binding.chatRecyclerView.visibility = View.VISIBLE

                   // 完成使用后删除图片
                   //val isDeleted = mPicFileUri?.let { it1 -> DrawableToUriUtil.deleteImageUri(this, it1) }
                   binding.imageLineHorScroll.visibility = View.GONE
                   binding.imageLine.removeAllViews()

                   // 通知适配器数据已更改
                   messageAdapter.notifyDataSetChanged()
                   //adapter.updateData(messageList)
                   // 清空输入框
                   binding.messageEditText.text?.clear()
               }else{
                   Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.can_not_send_empty_toast_message), Toast.LENGTH_SHORT).show()
               }
           }else{
               toLogin()
           }
        }

        binding.historyImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                binding.userConst.visibility = View.VISIBLE
                binding.moreSelectConst.visibility = View.GONE
                binding.todayTv.visibility = View.GONE
                val job = lifecycleScope.launch(Dispatchers.IO) {
                    //chatId = chatDatabase.chatDao().getAllChats().reversed().toMutableList().size
                    Log.e("ceshi","0返回的模型类型$modelType,,$chatTitle,,${messageList.size},,$isPrivate")
                    //chatTime = TimeUtils.getCurrentDateTime()
                    val allChatList = chatDatabase.chatDao().getAllChats()
                    for (chat in allChatList){
                        if (TimeUtils.getTimeTag(chat.time,TimeUtils.getCurrentDateTime())=="今日"){
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.todayTv.visibility = View.VISIBLE
                            }
                            break
                        }
                    }

                    if (!isPrivate){
                        if(chatTitle != ContextCompat.getString(this@MainActivity, R.string.chat_title) && isHaveTitle){
                            //直接插入，做了title唯一性，如果有了就替换成最新的
                            chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, chatTime,modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
                            //chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, "2025-10-30 16:54:59",modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
                        }
                        else if (messageList.size>1){
                            chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, chatTime,modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
                        }
                    }

                }
                lifecycleScope.launch {
                    job.join()
                    delay(500)
                    refreshChatList(true)
                    binding.drawerLayout.openDrawer(Gravity.LEFT)
                }
            }else{
                toLogin()
            }


        }

        // 监听抽屉状态
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {
                // 抽屉滑动时的回调
            }

            override fun onDrawerOpened(drawerView: android.view.View) {
                // 抽屉打开时，为空白区域添加点击事件
                binding.drawerLayout.setOnClickListener {
                    if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        binding.drawerLayout.closeDrawer(Gravity.LEFT)
                    }
                }
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                // 抽屉关闭时，移除点击事件
                binding.drawerLayout.setOnClickListener(null)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 抽屉状态改变时的回调
            }
        })

        // 处理返回键
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
                finish()
            }
        })

        binding.newChatImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                buildNewChat(true)
            }else{
                toLogin()
            }
        }

        binding.settingImage.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            isTemporary = false
            startActivity(intent)
        }

        binding.userConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(this, SettingActivity::class.java)
            if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
            }
            if (messageList.isEmpty()){
                intent.putExtra("chat_new",true)
            }else{
                intent.putExtra("chat_new",false)
            }
            startActivity(intent)
        }

        binding.upLoadImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                showBottomSheetMoreDialog()
            }else{
                toLogin()
            }

        }

        binding.moreImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                showBottomSheetMoreFunctionDialog()
            }else{
                toLogin()
            }

        }

        binding.chatTitleLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            performVibration()
            if (isTrueApiKey){
                showBottomSheetChatEditDialog(){
                    selectedChatTitle ->
                    binding.chatTitleTv.text = selectedChatTitle.chatTitle
                    chatTitle = selectedChatTitle.chatTitle
                }
            }else{
                toLogin()
            }

        }
        binding.noSendImage.setOnClickListener {
            Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.plase_first_enter_question_toast_message), Toast.LENGTH_SHORT).show()
        }

        binding.voiceImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                //TtsManagerUtils.TtsSpeak("你好hi")
                binding.inputWordsLine.visibility = View.GONE
                binding.voiceCon.visibility = View.VISIBLE
            }else{
                toLogin()
            }
        }
        binding.keyBoardImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            binding.inputWordsLine.visibility = View.VISIBLE
            binding.voiceCon.visibility = View.GONE
        }

        //doVoice()
        doNewVoice()

        binding.leftCancelImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            binding.userConst.visibility = View.VISIBLE
            binding.moreSelectConst.visibility = View.GONE
            adapterHistory.upDataMoreSelect(false)
            adapterHistory.notifyDataSetChanged()

        }

        /*binding.leftDeleteImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            // 关键：将待删除位置按降序排序（从大到小）
            val sortedList = selectedList.sortedDescending() // 排序后为 [3,2,1] 形式
            Log.e("ceshi","删除的列表$chatListReversed")
            for (position in sortedList){
                Log.e("ceshi","删除位置$position")
                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                    if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                        //先删除后添加
                        chatDatabase.chatDao().deleteChatByTitle(chatListReversed[position].title)
                    }
                }
                lifecycleScope.launch {
                    job1.join()
                    chatListReversed.removeAt(position)
                    adapterHistory.notifyItemRemoved(position)
                    adapterHistory.notifyItemRangeChanged(position, chatListReversed.size - position)
                }
            }
        }*/
        binding.leftDeleteImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isSendMessageAll.get()){
                //adapterHistory.upDataMoreSelect(false)
                Log.e("ceshi","要删除的多个位置${selectedList}")
                // 1. 降序排序选中的位置（保证从后往前删，减少位置偏移影响，但仍需后续优化）
                val sortedPositions = selectedList.sortedDescending()

                // 2. 收集待删除的 ChatItemRoom 对象（避免后续列表变化导致位置失效）
                val itemsToDelete = mutableListOf<ChatItemRoom>()
                for (pos in sortedPositions) {
                    if (pos < chatListReversed.size) { // 防止越界（理论上 sortedPositions 是合法位置）
                        itemsToDelete.add(chatListReversed[pos])
                    }
                }

                val deleteCounts = itemsToDelete.size

                // 3. 异步删除数据库中对应的记录
                lifecycleScope.launch(Dispatchers.IO) {
                    val countList = chatDatabase.chatDao().getAllChats().reversed().toMutableList().size
                    for (item in itemsToDelete) {
                        if (chatDatabase.chatDao().checkTitleExists(item.title)) {
                            chatDatabase.chatDao().deleteChatByTitle(item.title)
                        }
                    }

                    // 4. 数据库操作完成后，切换到主线程更新内存列表和适配器
                    withContext(Dispatchers.Main) {
                        for (item in itemsToDelete) {
                            val index = chatListReversed.indexOf(item)
                            if (index != -1) { // 确保对象仍在列表中（防止并发修改导致对象消失）
                                chatListReversed.removeAt(index)
                                adapterHistory.notifyItemRemoved(index)
                                adapterHistory.notifyItemRangeChanged(index, chatListReversed.size - index)
                            }
                        }

                        // 关键：删除后调用updateDataTime，传入最新列表并重新计算时间标签
                        adapterHistory.updateDataTime(chatListReversed)

                        if (countList == deleteCounts){
                            buildNewChat(false)
                        }


                        //退出多选模式
                        binding.userConst.visibility = View.VISIBLE
                        binding.moreSelectConst.visibility = View.GONE
                        adapterHistory.upDataMoreSelect(false)
                        adapterHistory.notifyDataSetChanged()


                    }
                }
            }else{
                Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.plase_wait_back_delect_toast_message), Toast.LENGTH_SHORT).show()
            }

        }

        binding.hideImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                if (!isPrivate){
                    isPrivate = true
                    binding.chatTitleTv.text = hideTitle
                    chatTitle = hideTitle
                    binding.hideImage.setImageResource(R.drawable.icon_hide)
                    //binding.hideImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                    binding.hideImage.imageTintList = ContextCompat.getColorStateList(
                        this@MainActivity,
                        R.color.color302AI
                    )
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.open_hide_chat_toast_message), Toast.LENGTH_SHORT).show()
                }else{
                    chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                    binding.chatTitleTv.text = chatTitle
                    isPrivate = false
                    binding.hideImage.setImageResource(R.drawable.icon_hide)
                    //binding.hideImage.clearColorFilter()
                    binding.hideImage.imageTintList = null
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.close_hide_chat_toast_message), Toast.LENGTH_SHORT).show()

                }
            }else{
                toLogin()
            }

        }

        binding.cons2.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
            buildNewChat(true)
        }

        //侧边框搜索
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            // 防抖：延迟 300ms 执行查询
            private var searchDebounceJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                val searchText = s?.toString() ?: ""
                if (searchText != ""){
                    binding.searchCloseBtn.visibility = View.VISIBLE
                }

                // 取消上一次未执行的任务
                searchDebounceJob?.cancel()

                searchDebounceJob  = lifecycleScope.launch(Dispatchers.IO) {
                    chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(s.toString()).toMutableList()

                    chatListTitleSearch = chatDatabase.chatDao().getChatsWithTitleContaining(s.toString()).toMutableList()

                }



                lifecycleScope.launch(Dispatchers.Main) {
                    searchDebounceJob?.join() // 等待数据库操作完成
//                    delay(500)
                    if (chatListTitleSearch != chatListSearch){
                        chatListSearch.addAll(chatListTitleSearch)
                    }

                    Log.e("ceshi","1这里的数量${chatListSearch.size}")
                    // 根据 id 去重（保留第一个出现的元素）
                    chatListSearch = chatListSearch.distinctBy { it.time }.toMutableList()

                    adapterHistorySearch = HomeMessageAdapter(this@MainActivity, chatListSearch, this@MainActivity,  onDeleteClickListener = { position,type ->
                        Log.e("ceshi","位置:${position},类型：$type")
                        when (type) {
                            "delete" -> {
                                Log.e("ceshi","是否可以删除${isSendMessageAll.get()}")
                                if (isSendMessageAll.get()){
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        //job1.join()
                                        if (chatListSearch[position].title == chatTitle){
                                            buildNewChat(false)
                                        }
                                        val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                            if (chatDatabase.chatDao().checkTitleExists(chatListSearch[position].title)){
                                                //先删除后添加
                                                chatDatabase.chatDao().deleteChatByTitle(chatListSearch[position].title)
                                            }
                                            chatListSearch.removeAt(position)
//                                    chatList = chatDatabase.chatDao().getChatsByUserId(userId).toMutableList()
//                                    //chatListReversed = chatList.reversed().toMutableList()
//                                    chatListReversed = chatList.sortedByDescending { it.time }.toMutableList()

                                        }
//                                adapterHistory.notifyItemRemoved(position)
//                                adapterHistory.notifyItemRangeChanged(position, chatListReversed.size - position)
                                        job1.join()
                                        adapterHistorySearch.updateDataTime(chatListSearch)
                                        adapterHistorySearch.notifyDataSetChanged()
                                    }
                                }else{
                                    Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.plase_wait_back_delect_toast_message), Toast.LENGTH_SHORT).show()
                                }

                            }
                            "delete1" -> {
                                Log.e("ceshi","是否可以删除${isSendMessageAll.get()}")
                                if (isSendMessageAll.get()){
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        //job1.join()
                                        if (chatListSearch[position].title == chatTitle){
                                            buildNewChat(false)
                                        }
                                        val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                            if (chatDatabase.chatDao().checkTitleExists(chatListSearch[position].title)){
                                                //先删除后添加
                                                chatDatabase.chatDao().deleteChatByTitle(chatListSearch[position].title)
                                            }
                                            chatListSearch.removeAt(position)
//                                    chatList = chatDatabase.chatDao().getChatsByUserId(userId).toMutableList()
//                                    //chatListReversed = chatList.reversed().toMutableList()
//                                    chatListReversed = chatList.sortedByDescending { it.time }.toMutableList()

                                        }
                                        job1.join()
                                        adapterHistorySearch.updateDataTime(chatListSearch)
                                        adapterHistorySearch.notifyItemRemoved(position)
                                        adapterHistorySearch.notifyItemRangeChanged(position, chatListReversed.size - position)
                                    }
                                }else{
                                    Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.plase_wait_back_delect_toast_message), Toast.LENGTH_SHORT).show()
                                }

                            }
                            "edit" -> {
                                //showEditPickerDialog(position)
                                showRenameDialog(position,chatListSearch[position].title)
                            }
                            "longPressed" -> {
                                Log.e("ceshi","longPressed")

                            }
                            "collect" -> {
                                Log.e("ceshi","0collect")
                                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                    if (chatDatabase.chatDao().checkTitleExists(chatListSearch[position].title)){
                                        //
                                        Log.e("ceshi","1collect")
                                        val updatedItem  = ChatItemRoom(chatListSearch[position].id,chatListSearch[position].title,
                                            chatListSearch[position].messages,chatListSearch[position].time,chatListSearch[position].modelType,chatListSearch[position].isDeepThink,
                                            chatListSearch[position].isNetWorkThink,chatListSearch[position].userId,chatListSearch[position].isMe,true,chatListReversed[position].isR1Fusion)
                                        chatDatabase.chatDao().updateChat(updatedItem)
                                        // 2. 同步更新内存中的数据源（关键！）
                                        launch(Dispatchers.Main) {
                                            chatListSearch[position] = updatedItem // 修改列表中的 item
                                        }
                                    }
                                }
                                lifecycleScope.launch {
                                    job1.join()
                                    //chatList.removeAt(position)
                                    adapterHistory.notifyItemChanged(position)
                                }

                            }
                            "unCollect" -> {
                                Log.e("ceshi","0unCollect")
                                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                    if (chatDatabase.chatDao().checkTitleExists(chatListSearch[position].title)){
                                        //
                                        Log.e("ceshi","1unCollect")
                                        val updatedItem  = ChatItemRoom(chatListSearch[position].id,chatListSearch[position].title,
                                            chatListSearch[position].messages,chatListSearch[position].time,chatListSearch[position].modelType,chatListSearch[position].isDeepThink,
                                            chatListSearch[position].isNetWorkThink,chatListSearch[position].userId,chatListSearch[position].isMe,false,chatListReversed[position].isR1Fusion)
                                        chatDatabase.chatDao().updateChat(updatedItem)
                                        // 2. 同步更新内存中的数据源（关键！）
                                        launch(Dispatchers.Main) {
                                            chatListSearch[position] = updatedItem // 修改列表中的 item
                                        }
                                    }
                                }
                                lifecycleScope.launch {
                                    job1.join()
                                    //chatList.removeAt(position)
                                    adapterHistory.notifyItemChanged(position)
                                }

                            }
                            "moreSelect" -> {
                                binding.userConst.visibility = View.GONE
                                binding.moreSelectConst.visibility = View.VISIBLE
                                adapterHistorySearch.notifyDataSetChanged()
                            }
                        }

                    })
                    //adapterHistorySearch.updateSearchMessage(s.toString())
                    // 可以在这里进行 RecyclerView 的设置等操作
//                    binding.historyLeftSearchListRecycle.adapter = adapterHistorySearch
//                    binding.historyLeftSearchListRecycle.layoutManager = LinearLayoutManager(this@MainActivity)
                    // 初始化 chatRecyclerView 时添加
                    binding.historyLeftSearchListRecycle.apply {
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        setHasFixedSize(true) // 固定尺寸，减少测量
                        adapter = adapterHistorySearch
                        // 可选：添加 Item 缓存（默认是 2，可根据需求增加）
                        recycledViewPool.setMaxRecycledViews(0, 5)
                    }

                    // 通知适配器数据已更改
                    adapterHistorySearch.notifyDataSetChanged()
                    binding.historyLeftListRecycle.visibility = View.GONE
                    binding.historyLeftSearchListRecycle.visibility = View.VISIBLE

                }
                Log.e("ceshi","搜索文字：${s.toString() == ""}")

            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("ceshi","afterTextChanged搜索文字：${s.toString()}")
                if (s?.isEmpty() == true) {
                    lifecycleScope.launch(Dispatchers.Main){
                        Log.e("ceshi","搜索文字：执行")
                        //不做延迟处理，recycle1数据还没有更新完，就是导致下面的visibility不起作用
                        delay(500)
                        binding.historyLeftSearchListRecycle.visibility = View.GONE
                        binding.historyLeftListRecycle.visibility = View.VISIBLE
                        Log.e("ceshi","搜索文字：执行1")
                        binding.root.requestLayout() // 强制更新布局
                    }
                }
            }
        })

        binding.searchCloseBtn.setOnClickListener {
            // 清空输入框
            binding.editSearch.text?.clear()
            binding.searchCloseBtn.visibility = View.GONE
            binding.historyLeftSearchListRecycle.visibility = View.GONE
            binding.historyLeftListRecycle.visibility = View.VISIBLE
        }

        binding.const3.setOnClickListener {
            val intent = Intent(this,ResourceActivity::class.java)
            startActivity(intent)
        }


        // 设置RecyclerView的触摸事件
        binding.chatRecyclerView.setOnTouchListener { _, event ->
            // 当触摸事件为按下或移动时
            if (event.action == MotionEvent.ACTION_DOWN ||
                event.action == MotionEvent.ACTION_MOVE) {
                hideKeyboard()
            }
            false // 返回false，不消费事件，确保RecyclerView的滚动等事件正常工作
        }

        // 可选：设置RecyclerView的子项点击事件也收起键盘
        binding.chatRecyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e:MotionEvent):Boolean {
                if (e.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard()
                }
                return false
            }
        })


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(35)
    private fun initData(){
        lifecycleScope.launch(Dispatchers.IO) {
            mReadImageUrl = dataStoreManager.readImageUrl.first()?:""
            lifecycleScope.launch(Dispatchers.Main) {
                Glide.with(this@MainActivity)
                    .load(mReadImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.userImage)
            }
        }
        refreshChatList(false)

        val chatItem = intent.getSerializableExtra("chat_item") as? ChatItemRoom
        val position = intent.getSerializableExtra("chat_position") as? Int
        val comeFrom = intent.getSerializableExtra("come_from") as? String
        val chatItemHistory = intent.getSerializableExtra("setting_chat_item") as? ChatItemRoom
        if (position != null){
            nowChatPositon = position
        }
        if (comeFrom != null){
            if (comeFrom == "setting"){
                buildNewChat(false)
            }
        }
        if (chatItem != null) {
            Log.e("ceshi","Received chat item: ${chatItem}")
            binding.newChatCon.visibility = View.GONE
            binding.hideImage.visibility = View.GONE
            messageList = chatItem.messages
            chatTime = chatItem.time
            modelType = chatItem.modelType
            isDeepThink = chatItem.isDeepThink
            isNetWorkThink = chatItem.isNetWorkThink
            isR1Fusion = chatItem.isR1Fusion
            userId = chatItem.userId
            chatId = chatItem.id
            chatTitle = chatItem.title
            mMessageList.clear()
            for (message in messageList){
                mMessageList.add(message.message)
            }
            if (moreFunctionQuantity>0){
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
                binding.moreFunctionLine.visibility = View.VISIBLE
                binding.moreIdTv.text = moreFunctionQuantity.toString()
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.setColorFilter(ContextCompat.getColor(this, R.color.color302AI), PorterDuff.Mode.SRC_IN)
            }else{
                binding.moreFunctionLine.visibility = View.GONE
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.clearColorFilter()
            }

            if (chatItem.messages.last().message == "file:///android_asset/loading.html"){
                val send = messageList[messageList.size-2].message
                againSendQuestion(send)
            }

        }else{


        }

        if (chatItemHistory != null) {
            Log.e("ceshi","Received chatItemHistory item: ${chatItemHistory}")
            binding.newChatCon.visibility = View.GONE
            binding.hideImage.visibility = View.GONE
            messageList = chatItemHistory.messages
            chatTime = chatItemHistory.time
            modelType = chatItemHistory.modelType
            modelTypeHistory = chatItemHistory.modelType
            isDeepThink = chatItemHistory.isDeepThink
            isNetWorkThink = chatItemHistory.isNetWorkThink
            isR1Fusion = chatItemHistory.isR1Fusion
            userId = chatItemHistory.userId
            chatId = chatItemHistory.id
            chatTitle = chatItemHistory.title
            mMessageList.clear()
            for (message in messageList){
                mMessageList.add(message.message)
            }
            if (moreFunctionQuantity>0){
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
                binding.moreFunctionLine.visibility = View.VISIBLE
                binding.moreIdTv.text = moreFunctionQuantity.toString()
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.setColorFilter(ContextCompat.getColor(this, R.color.color302AI), PorterDuff.Mode.SRC_IN)
            }else{
                binding.moreFunctionLine.visibility = View.GONE
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.clearColorFilter()
            }

            if (chatItemHistory.messages.last().message == "file:///android_asset/loading.html"){
                val send = messageList[messageList.size-2].message
                againSendQuestion(send)
            }

        }

        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            chatViewModel.getUserInfo( WearData.getInstance().token,apiService)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(35)
    private fun initObserver(){
        chatViewModel.questionResult.observeForever { result ->
            Log.e("ceshi","0机器人有回复：$result,,${isSendMessage.get()}")
            isSendMessageAgain.set(true)
            chatTime = TimeUtils.getCurrentDateTime()
            if (isSendMessage.get()){
                //isSendMessage.set(false)
                result?.let {
                    Log.e("ceshi","机器人有回复：$it,,")
                    messageAdapter.upDateIsNewChat(true)
                    messageList.removeLast()
                    // 模拟机器人回复
                    it?.let {
                        messageList.add(ChatMessage(it,false,"chat",false,false))
                        //adapter.upDatePosition(messageList.size-1)
                        Log.e("ceshi","模拟机器人回复，添加链表")
                        // 通知适配器数据已更改
                        messageAdapter.notifyDataSetChanged()
                        //adapter.updateData(messageList)
                        messageAdapter.notifyItemInserted(messageAdapter.itemCount - 1)
                        binding.chatRecyclerView.layoutManager?.scrollToPosition(messageList.size-1)
                        binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            private var lastHeight = 0

                            override fun onGlobalLayout() {
                                /*val currentHeight = getRecyclerViewContentHeight(binding.chatRecyclerView)
                                //val currentHeightScroll = binding.scroll3.height
                                val phoneHeight = ScreenUtils.getScreenHeight(
                                    this@MainActivity
                                )

                                Log.e("ceshi","显示的recycleView屏幕：$currentHeight")
                                Log.e("ceshi","屏幕的整体高度：${
                                    ScreenUtils.getScreenHeight(
                                        this@MainActivity
                                    )
                                }")//1097/1640
                                // 当 RecyclerView 高度发生变化时触发滚动
                                //Log.e("ceshi","是否滚动：${currentHeight/phoneHeight > 1000/1640}")
                                // 转成浮点运算，确保结果正确
                                val condition = (currentHeight.toDouble() / phoneHeight) > (1000.0 / 1640)
                                Log.e("ceshi","是否滚动：$condition")
                                if (condition && isSlideBottomSwitch) {
                                    (binding.chatRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
                                }

                                lastHeight = currentHeight*/
                                binding.chatRecyclerView.layoutManager?.scrollToPosition(messageList.size-1)
                                // 2. 移除监听（只执行一次，或在合适时机移除）
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    binding.chatRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                } else {
                                    binding.chatRecyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                                }
                            }
                        })



                    }
                }
            }else{
                result?.let {
                    initGetLastMessage(it)
                }
            }

    }

        chatViewModel.questionTitleResult.observeForever { result ->
            if (isSendMessage.get()){
                result?.let {
                    Log.e("ceshi", "机器人有回复标题：$it,,")
                    var chatTitleId = 0
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (chatDatabase.chatDao().checkTitleExists(it)){
                            if (chatDatabase.chatDao().checkTitleExists(chatTitle)){
                                chatTitle = it+"${chatTitleId++}"
                            }else{
                                chatTitle = it+"${chatTitleId}"
                            }
                        }else{
                            chatTitle = it
                        }
                        isHaveTitle = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (!isPrivate) {
                                binding.chatTitleTv.text = chatTitle
                            }
                        }

                    }


                }
            }

        }

        chatViewModel.questionAllResult.observeForever { result ->
            result?.let {
                //震动反馈
                performVibration()
                isSendMessageAll.set(true)
                Log.e("ceshi", "机器人有回复完全：$it,,")
                mMessageList.add(it)
            }
        }

        chatViewModel.questionDeepAllResult.observeForever { result ->
            result?.let {
                Log.e("ceshi", "机器人有回复深度完全：$it,,")
                //mMessageList.add(it)
            }
        }

        chatViewModel.questionDeepResult.observeForever { result ->
            result?.let {
                Log.e("ceshi", "机器人有回复深度：$it,,")
                //mMessageList.add(it)
                messageAdapter.upDateDeepMessage(it)
                messageAdapter.notifyDataSetChanged()
                //adapter.updateData(messageList)
            }
        }

        chatViewModel.imageUrlServiceResult.observe(this){
            Log.e("ceshi","返回的图片地址回复：$it")
            it?.let {
                imageUrlServiceResultList.add(it)
                mImageUrlServiceResultList.add(it)
                CoroutineScope(Dispatchers.IO).launch {
                    urlMapper.saveUrlMapping(it, urlLocal)
                }
            }
        }

        chatViewModel.voiceToTextResult.observe(this) {
            Log.e("ceshi", "语音识别有回复：$it")
            // 模拟机器人回复
            it?.let {
                /*messageList.add(ChatMessage(it,true,"",false,false))
                messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false))
                mMessageList.add(it)

                lifecycleScope.launch(Dispatchers.IO) {
                    isSendMessage.set(true)
                    chatViewModel.sendQuestion(it,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,
                        false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)

                    //标题生成
                    if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title))){
                        chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(it),buildTitleModelType,apiKey,apiService)
                    }else if (!isBuildTitleFirstTime){
                        chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(it),buildTitleModelType,apiKey,apiService)
                    }
                }
                binding.newChatCon.visibility = View.GONE
                binding.chatRecyclerView.visibility = View.VISIBLE

                // 通知适配器数据已更改
                messageAdapter.notifyDataSetChanged()*/
                //adapter.updateData(messageList)
                /*// 通知适配器数据已更改
                adapter.notifyDataSetChanged()
                adapter.notifyItemInserted(adapter.itemCount - 1)
                // 监听 RecyclerView 的布局变化
                binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    private var lastHeight = 0
                    override fun onGlobalLayout() {
                        val currentHeight = binding.chatRecyclerView.height
                        lastHeight = currentHeight
                    }
                })*/
                if (it==""){
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.voice_short_can_not_message), Toast.LENGTH_SHORT).show()//voice_short_can_not_message
                }else{
                    binding.messageEditText.setText(it.toString())
                    binding.messageEditText.setSelection(it.length)
                }

            }


        }

        // 观察网络错误 LiveData
        NetworkModule.NetworkErrorLiveData.errorLiveData.observe(this) { (message, _) ->
            Toast.makeText(this, "${ContextCompat.getString(this@MainActivity, R.string.network_error_toast_message)}$message", Toast.LENGTH_SHORT).show()
            messageList.removeLast()
            messageList.add(ChatMessage("网络超时",false,"chat",false,false))
            mMessageList.add("网络超时")
            //adapter.upDatePosition(messageList.size-1)
            Log.e("ceshi","模拟机器人回复，添加链表")
            // 通知适配器数据已更改
            messageAdapter.notifyDataSetChanged()
            //adapter.updateData(messageList)
            messageAdapter.notifyItemInserted(messageAdapter.itemCount - 1)
        }

        chatViewModel.modelListResult.observe(this){
            Log.e("ceshi","模型列表回复：$it")
            if (it.isEmpty()){
                isTrueApiKey = false
            }else{
                isTrueApiKey = true
                binding.modeTypeTv.visibility = View.VISIBLE
            }

            it?.let {
                //modelList = it


                lifecycleScope.launch(Dispatchers.IO) {

                    var mModelList = dataStoreManager.modelListFlow.first()
                    Log.e("ceshi","0这里的数据库模型列表:${mModelList}")
                    if (mModelList.isNotEmpty()){
                        // 1. 先找到当前列表中 isCustomize == false 的最后一个元素的索引
                        var lastFalseIndex = -1 // 初始值：-1 表示没有找到符合条件的元素
                        for (i in mModelList.indices) {
                            val existingModel = mModelList[i]
                            val existingModelData = chatDatabase.chatDao().getModelById(existingModel)
                            if (existingModelData?.isCustomize == false) {
                                lastFalseIndex = i // 更新最后一个符合条件的索引
                            }
                        }

                        // 2. 遍历需要插入的 model，按规则插入到目标位置
                        for (model in it) {
                            val modelData = chatDatabase.chatDao().getModelById(model)
                            if (!mModelList.contains(model)) { // 仅插入不存在的 model
                                // 计算插入位置：
                                // - 如果存在 isCustomize == false 的元素，插入到其最后一位的后面（lastFalseIndex + 1）
                                // - 如果不存在，插入到列表开头（0）
                                val insertIndex = if (lastFalseIndex != -1) lastFalseIndex + 1 else 0

                                // 插入元素（该方法会自动将 insertIndex 及之后的元素后移）
                                mModelList.add(insertIndex, model)

                                // 3. 如果新插入的 model 本身 isCustomize == false，更新 lastFalseIndex（它成为新的“最后一位”）
                                if (modelData?.isCustomize == false) {
                                    lastFalseIndex = insertIndex // 插入位置就是新的最后一位索引
                                }
                            }
                        }

                        // 4. 保留原逻辑：添加固定元素
                        mModelList.add("gemini-2.5-flash-nothink")

                        // 5. 转换为 CopyOnWriteArrayList
                        modelList = CopyOnWriteArrayList(mModelList)
                    }else{
                        modelList = it
                        if (it.isNotEmpty()){
                            modelList.add("gemini-2.5-flash-nothink")
                        }
                    }
                    Log.e("ceshi","这里的数据库模型列表:${modelList}")
                    removeDuplicates(modelList)

                    dataStoreManager.saveModelList(modelList)

                    val readUserEmailData = dataStoreManager.readUserEmailData.first()?:""
                    Log.e("ceshi","获取账号$readUserEmailData")
                    if (readUserEmailData != ""){
                        isTrueApiKey = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.modeTypeTv.visibility = View.VISIBLE
                        }
                    }


                }
            }
        }

        chatViewModel.userInfoResult.observe(this){
            it.let {
                apiKey = it.api_key
                binding.userName.text = it.user_name
                // 方法1：使用内置的CircleCrop变换
                Glide.with(this@MainActivity)
                    .load(it.avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.userImage)
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveData(apiKey)
                    dataStoreManager.saveUserName(it.user_name)
                    dataStoreManager.saveUserBalance(it.balance)
                    dataStoreManager.saveImageUrl(it.avatar)
                    if (it.email == ""){
                        userId = it.phone
                        insertUserConfiguration(it.phone)
                    }else{
                        userId = it.email
                        insertUserConfiguration(it.email)
                    }

                    chatViewModel.get302AiModelList(it.api_key,apiService)

                }
            }
        }


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isToPicture = true
        //isFile = false
        // 处理长截图权限请求结果
        //screenshotManager.onActivityResult(requestCode, resultCode, data)

        /*if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                // 启动前台服务处理截图
                val serviceIntent = Intent(this, ScreenshotService::class.java).apply {
                    putExtra(ScreenshotService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(ScreenshotService.EXTRA_RESULT_DATA, data)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                //screenshotManager.onActivityResult(requestCode, resultCode, data)
            }
        }*/
        Log.e("ceshi","回调返回值：$requestCode,,$resultCode,,$data")
        if (requestCode == 1002 && resultCode == 0 && data == null){
            if (imageUrlLocalList.isNotEmpty()){
                for (url in imageUrlLocalList){
                    if (url.contains("documents")){
                        showFileImageView(url,true)
                    }
                }

            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            isPicture = true
            imageUrlLocal = "$selectedImageUri"
            urlLocal = "$selectedImageUri"
            //上传图片到服务器
            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.upLoadImage(this@MainActivity,
                    SystemUtils.uriToTempFile(this@MainActivity, selectedImageUri),"imgs",false,apiService)
            }

            addNewImageView(imageUrlLocal,false)

            Log.e("ceshi","1图片地址$imageUrlLocal")

        }else if(requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK){

            currentPhotoPath?.let { path ->
                isPicture = true
                isFile = false
                //imageUrlLocal = "$path"
                val imageFile = File(path)
                //Log.e("ceshi","3图片地址$imageFile")
                if (imageFile.exists()) {
                    val contentUri = Uri.fromFile(imageFile)
                    imageUrlLocal = "$contentUri"
                    urlLocal = "$contentUri"
                    //上传图片到服务器
                    lifecycleScope.launch(Dispatchers.IO) {
                        chatViewModel.upLoadImage(this@MainActivity,
                            SystemUtils.uriToTempFile(this@MainActivity, contentUri),"imgs",false,apiService)
                    }

                    galleryAddPic()
                    //Log.e("ceshi","2图片地址$imageUrlLocal")
                    addNewImageView(imageUrlLocal,false)
                } else {
                    Log.e("Camera", "图片文件不存在: $path")
                }




            } ?: run {
                Log.e("Camera", "未找到保存的图片路径")
            }

        }else if (requestCode == FILE_IMAGE_REQUEST && resultCode == RESULT_OK){
            isPicture = false
            isFile = true
            isOpenFile = false
            data?.data?.let { uri ->
                selectedFileUri = uri
                urlLocal = "$uri"
                // 获取文件信息
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        // 获取文件名
                        val fileNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val fileName = it.getString(fileNameIndex) ?: "未知文件名"

                        // 获取文件大小（字节）
                        val fileSizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        val fileSize = if (!it.isNull(fileSizeIndex)) {
                            //it.getLong(fileSizeIndex).toString() + " bytes"
                            //(it.getFloat(fileSizeIndex)/1024f).toString() + " kb"
                            // 保留两位小数（四舍五入）
                            //String.format("%.2f", it.getFloat(fileSizeIndex) / 1024f) + " kb"
                            formatFileSize(it.getFloat(fileSizeIndex))
                        } else {
                            "未知大小"
                        }

                        // 获取文件格式（从文件名解析）
                        val fileExtension = if (fileName.contains(".")) {
                            fileName.substring(fileName.lastIndexOf(".") + 1).uppercase()
                        } else {
                            "无格式"
                        }

                        // 显示文件信息
                        //filePathTv.text = "文件名: $fileName\n大小: $fileSize\n格式: $fileExtension"
                        //uploadBtn.isEnabled = true
//                        val imageUri = DrawableToUriUtil.getDrawableUri(
//                            context = this,
//                            drawableResId = R.drawable.icon_file, //资源ID
//                            displayName = "my_image" // 图片显示名称
//                        )
                        //mPicFileUri = imageUri
                        this.fileName = fileName
                        this.fileSize = fileSize
                        fileNameList.add(fileName)
                        fileSizeList.add(fileSize)
                        Log.e("ceshi","文件的URL：${selectedFileUri}")
                        addNewImageView(selectedFileUri.toString(),true)
                        Log.e("ceshi","文件名: $fileName\\n大小: $fileSize\\n格式: $fileExtension")
                    }
                }

                //上传文件
                isPicture = true
                //isFile = false
                //上传图片到服务器
                lifecycleScope.launch(Dispatchers.IO) {
                    chatViewModel.upLoadImage(this@MainActivity,
                        SystemUtils.uriToTempFile(this@MainActivity, selectedFileUri!!),"imgs",false,apiService)
                }
            }
        }else if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK){
            // 用户选择的文件URI
            val uri = data?.data
            if (uri != null) {
                selectedDocumentUri = uri
                // 申请持久权限（关键：确保应用重启后仍能访问该URI）
                takePersistableUriPermission(uri)
                // 自动打开该文件
                openDocument(uri)
            }
        }
        else{
//            isPicture = false
//            isFile = false
        }

    }
    // 保存用户选择的文件URI
    private var selectedDocumentUri: Uri? = null

    // 请求媒体投影权限
    private fun requestMediaProjectionPermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }

    /**
     * 格式化文件大小（自动转换单位：B、KB、MB、GB）
     * @param bytes 原始字节数
     * @return 格式化后的字符串（如 "2.56 KB"、"1.45 MB"）
     */
    fun formatFileSize(bytes: Float): String {
        // 定义单位数组（按从小到大顺序）
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes // 初始值为字节数
        var unitIndex = 0 // 初始单位索引（0对应B）

        // 循环判断是否需要进位到更大单位（1024倍递进）
        while (size >= 1024 && unitIndex < units.lastIndex) {
            size /= 1024f // 转换到更大单位
            unitIndex++ // 单位索引+1（切换到下一个单位）
        }

        // 保留两位小数并拼接单位（自动四舍五入进位）
        return String.format("%.2f %s", size, units[unitIndex])
    }

    private fun buildNewChat(insert:Boolean){
        lifecycleScope.launch(Dispatchers.Main) {
            binding.floatingButton.visibility = View.GONE
        }
        isSendMessage.set(false)
        restoreInitialHeight()
        if (messageList.isEmpty()){
            if (!isComeFromSetting){
                Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.already_new_session_toast_message), Toast.LENGTH_SHORT).show()
            }else{
                isComeFromSetting = false
            }

        }else{
            performVibration()
            Log.e("ceshi","创建新的会话$isUseTracelessSwitch")

            lifecycleScope.launch(Dispatchers.IO) {

                Log.e("ceshi","侧边栏删除：${leftDelect.get()}")
                if (!isPrivate && insert){
                    chatId = chatDatabase.chatDao().getAllChats().toMutableList().size
                    Log.e("ceshi","返回的模型类型$modelType,,$chatTitle")
                    Log.e("ceshi","插入最新的${chatId}")
                    if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title))){
                        chatTitle = chatTitle+chatId
                    }
                    //直接插入，做了title唯一性，如果有了就替换成最新的
                    chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, chatTime,modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
                    //chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatTitle, messageList, "2025-10-30 16:54:59",modelType,isDeepThink,isNetWorkThink,userId,isMe,false,isR1Fusion))
                    //Log.e("ceshi", "B插入完成时间：${System.currentTimeMillis()}") // 添加日志
                }else{
                    //leftDelect.set(false)
                }

                val readModelType = dataStoreManager.readModelType.first()?:"gemini-2.5-flash-nothink"
                readModelType?.let {
                    Log.e("ceshi","模型是多少：$it")
                    modelType = it
                }
//                Log.e("ceshi","现在的模型：$mModelTypeHistory")
//                if (mModelTypeHistory!=""){
//                    modelType = mModelTypeHistory
//                }

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.newChatCon.visibility = View.VISIBLE
                    //adapter.notifyDataSetChanged()
                    binding.chatRecyclerView.visibility = View.GONE
                    //chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                    messageList.clear()
                    mMessageList.clear()
                    isHaveTitle = false
                    imageCounter = 0

                    binding.hideImage.visibility = View.VISIBLE
                    //isPrivate = false
//                    binding.hideImage.setImageResource(R.drawable.icon_hide)
//                    binding.hideImage.clearColorFilter()

                    //isDeepThink = false
                    //isNetWorkThink = false
                    //isR1Fusion = false
                    //binding.chatTitleTv.text = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                    chatTime = TimeUtils.getCurrentDateTime()

                    //moreFunctionQuantity = 0
                    binding.moreFunctionLine.visibility = View.GONE
                    binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
                    binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                    binding.moreImage.clearColorFilter()

                    binding.modeTypeTv.text = modelType
                    //不清空功能状态集成上一会话
                    if (moreFunctionQuantity>0){
                        binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
                        binding.moreFunctionLine.visibility = View.VISIBLE
                        binding.moreIdTv.text = moreFunctionQuantity.toString()
                        binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                        binding.moreImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                    }else{
                        binding.moreFunctionLine.visibility = View.GONE
                        binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
                        binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                        binding.moreImage.clearColorFilter()
                    }

                    if (isUseTracelessSwitch){
                        isPrivate = true
                        binding.hideImage.setImageResource(R.drawable.icon_hide)
                        //binding.hideImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                        binding.hideImage.imageTintList = ContextCompat.getColorStateList(
                            this@MainActivity,
                            R.color.color302AI
                        )
                        binding.hideImage.invalidate() // 强制重绘
                        chatTitle = hideTitle
                        binding.chatTitleTv.text = hideTitle
                        binding.chatTitleTv.invalidate() // 强制重绘
                    }else{
                        isPrivate = false
                        binding.hideImage.setImageResource(R.drawable.icon_hide)
                        binding.hideImage.imageTintList = null
                        binding.hideImage.invalidate() // 强制重绘
                        chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                        binding.chatTitleTv.text = chatTitle
                    }
                }



            }

            /*if (isUseTracelessSwitch){
                lifecycleScope.launch(Dispatchers.Main) {
                    isPrivate = true
                    binding.hideImage.setImageResource(R.drawable.icon_hide)
                    binding.hideImage.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.color302AI), PorterDuff.Mode.SRC_IN)
                }
            }*/

        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun onLineBreak(str:String){
        showBottomSheetDialog(str)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun showBottomSheetDialog(str:String) {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)
        // Activity 使用this
        //  val bottomSheetDialog = BottomSheetDialog(this)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
        // 关闭 BottomSheetDialog
            bottomSheetDialog.dismiss()
            binding.messageEditText.setText(inputStr)
        }
        val edit = view.findViewById<EditText>(R.id.messageEditText1)
        val sendMessage = view.findViewById<ImageView>(R.id.sendImage1)
        sendMessage.setOnClickListener {
            if (isTrueApiKey){
                val message = binding.messageEditText.text.toString().trim()
                Log.e("ceshi","点击")
                if (message.isNotEmpty()) {
                    //发送消息后隐私按钮消失
                    binding.hideImage.visibility = View.GONE
                    if (isPicture){
                        if (imageCounter != imageUrlServiceResultList.size && imageUrlServiceResultList.isEmpty()){//imageUrlServiceResult == "" || imageUrlServiceResultList.isEmpty() || (imageUrlServiceResultList.size-1) != imageCounter
                            Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.parsing_image_toast_message), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        isPicture = false
                        var mImageUrlLocalStrBulder = StringBuilder()
                        for (imagelocal in imageUrlLocalList){
                            mImageUrlLocalStrBulder.append("<img src=\"$imagelocal\" width=\"150\" height=\"150\">")
                            //mImageUrlLocalStrBulder.append("![示例图片](content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20250725_153926.jpg)")
                            // ![示例图片](content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20250725_153926.jpg)

                        }
                        //Log.e("ceshi","返回的URL${imageUrlServiceResultList.size},,$imageCounter")
                        messageList.add(ChatMessage("${mImageUrlLocalStrBulder.toString()}<br>$message",true,"chat",false,false))
                        messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false))
                        mMessageList.add(message)
                        mImageUrlLocalStrBulder.clear()

                    }else{
                        messageList.add(ChatMessage(message,true,"",false,false))
                        messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false))
                        mMessageList.add(message)
                    }

                    messageAdapter.upDateIsNewChat(false)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val model = "gpt-4o-image-generation"
                        isSendMessage.set(true)
                        chatViewModel.sendQuestion(message,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,false,apiKey,false,
                            apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)

                        //标题生成
                        if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                            chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(message),buildTitleModelType,apiKey,apiService)
                        }else if (!isBuildTitleFirstTime && !isPrivate){
                            chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(message),buildTitleModelType,apiKey,apiService)
                        }

                        mImageUrlServiceResultList.clear()
                        mImageUrlLocalList.clear()
                        imageUrlLocalList.clear()
                        imageUrlServiceResultList.clear()
                        imageCounter = 0

                    }
                    binding.newChatCon.visibility = View.GONE
                    binding.chatRecyclerView.visibility = View.VISIBLE

                    // 完成使用后删除图片
                    //val isDeleted = mPicFileUri?.let { it1 -> DrawableToUriUtil.deleteImageUri(this, it1) }
                    binding.imageLineHorScroll.visibility = View.GONE
                    binding.imageLine.removeAllViews()

                    // 通知适配器数据已更改
                    messageAdapter.notifyDataSetChanged()
                    //adapter.updateData(messageList)
                    // 清空输入框
                    edit.text?.clear()
                    binding.messageEditText.text?.clear()
                    bottomSheetDialog.dismiss()
                }else{
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.can_not_send_empty_toast_message), Toast.LENGTH_SHORT).show()
                }
            }else{
                toLogin()
            }
        }


        edit.setText(str)
        edit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputStr = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // 可在这里处理其他逻辑
            }
        })

        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }


    fun refreshChatList(isLeft:Boolean){
        val job = lifecycleScope.launch(Dispatchers.IO) {
            /*if (isLeft){
                chatList = chatDatabase.chatDao().getAllChats().reversed().toMutableList()
            }else{
                chatList = chatDatabase.chatDao().getAllChats().toMutableList()
            }*/

            //chatList = chatDatabase.chatDao().getAllChats().toMutableList()
            chatList = chatDatabase.chatDao().getChatsByUserId(userId).toMutableList()
            //chatListReversed = chatList.reversed().toMutableList()
            chatListReversed = chatList.sortedByDescending { it.time }.toMutableList()

            if (chatListReversed != null){
                chatId = chatListReversed.size
            }else{
                chatId = 0
            }
        }


        lifecycleScope.launch {
            job.join() // 等待数据库操作完成
            Log.e("ceshi","历史列表：${chatListReversed.size}")
            adapterHistory = HomeMessageAdapter(this@MainActivity, chatListReversed, this@MainActivity,  onDeleteClickListener = { position,type ->
                Log.e("ceshi","位置:${position},类型：$type")
                when (type) {
                    "delete" -> {
                        //Log.e("ceshi","是否可以删除${isSendMessageAll.get()}")
                        if (isSendMessageAll.get()){
                            lifecycleScope.launch(Dispatchers.Main) {
                                //job1.join()
                                if (chatListReversed[position].title == chatTitle){
                                    buildNewChat(false)
                                }
                                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                    if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                                        //先删除后添加
                                        chatDatabase.chatDao().deleteChatByTitle(chatListReversed[position].title)
                                    }
                                    chatListReversed.removeAt(position)
//                                    chatList = chatDatabase.chatDao().getChatsByUserId(userId).toMutableList()
//                                    //chatListReversed = chatList.reversed().toMutableList()
//                                    chatListReversed = chatList.sortedByDescending { it.time }.toMutableList()

                                }
//                                adapterHistory.notifyItemRemoved(position)
//                                adapterHistory.notifyItemRangeChanged(position, chatListReversed.size - position)
                                job1.join()
                                adapterHistory.updateDataTime(chatListReversed)
                                adapterHistory.notifyDataSetChanged()
                            }


                        }else{
                            Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.plase_wait_back_delect_toast_message), Toast.LENGTH_SHORT).show()
                        }
                    }
                    "delete1" -> {
                        //Log.e("ceshi","是否可以删除${isSendMessageAll.get()}")
                        if (isSendMessageAll.get()){
                            lifecycleScope.launch(Dispatchers.Main) {
                                //job1.join()
                                if (chatListReversed[position].title == chatTitle){
                                    buildNewChat(false)
                                }
                                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                    if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                                        //先删除后添加
                                        chatDatabase.chatDao().deleteChatByTitle(chatListReversed[position].title)
                                    }
                                    chatListReversed.removeAt(position)
//                                    chatList = chatDatabase.chatDao().getChatsByUserId(userId).toMutableList()
//                                    //chatListReversed = chatList.reversed().toMutableList()
//                                    chatListReversed = chatList.sortedByDescending { it.time }.toMutableList()

                                }
                                job1.join()
                                adapterHistory.updateDataTime(chatListReversed)
                                adapterHistory.notifyItemRemoved(position)
                                adapterHistory.notifyItemRangeChanged(position, chatListReversed.size - position)
                            }


                        }else{
                            Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.plase_wait_back_delect_toast_message), Toast.LENGTH_SHORT).show()
                        }
                    }
                    "edit" -> {
                        //showEditPickerDialog(position)
                        showRenameDialog(position,chatListReversed[position].title)
                    }
                    "longPressed" -> {
                        Log.e("ceshi","longPressed")

                    }
                    "collect" -> {
                        Log.e("ceshi","0collect")
                        val job1 = lifecycleScope.launch(Dispatchers.IO) {
                            if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                                //
                                Log.e("ceshi","1collect")
                                val updatedItem  = ChatItemRoom(chatListReversed[position].id,chatListReversed[position].title,
                                    chatListReversed[position].messages,chatListReversed[position].time,chatListReversed[position].modelType,chatListReversed[position].isDeepThink,
                                    chatListReversed[position].isNetWorkThink,chatListReversed[position].userId,chatListReversed[position].isMe,true,chatListReversed[position].isR1Fusion)
                                chatDatabase.chatDao().updateChat(updatedItem)
                                // 2. 同步更新内存中的数据源（关键！）
                                launch(Dispatchers.Main) {
                                    chatListReversed[position] = updatedItem // 修改列表中的 item
                                }
                            }
                        }
                        lifecycleScope.launch {
                            job1.join()
                            //chatList.removeAt(position)
                            adapterHistory.notifyItemChanged(position)
                        }

                    }
                    "unCollect" -> {
                        Log.e("ceshi","0unCollect")
                        val job1 = lifecycleScope.launch(Dispatchers.IO) {
                            if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                                //
                                Log.e("ceshi","1unCollect")
                                val updatedItem  = ChatItemRoom(chatListReversed[position].id,chatListReversed[position].title,
                                    chatListReversed[position].messages,chatListReversed[position].time,chatListReversed[position].modelType,chatListReversed[position].isDeepThink,
                                    chatListReversed[position].isNetWorkThink,chatListReversed[position].userId,chatListReversed[position].isMe,false,chatListReversed[position].isR1Fusion)
                                chatDatabase.chatDao().updateChat(updatedItem)
                                // 2. 同步更新内存中的数据源（关键！）
                                launch(Dispatchers.Main) {
                                    chatListReversed[position] = updatedItem // 修改列表中的 item
                                }
                            }
                        }
                        lifecycleScope.launch {
                            job1.join()
                            //chatList.removeAt(position)
                            adapterHistory.notifyItemChanged(position)
                        }

                    }
                    "moreSelect" -> {
                        binding.userConst.visibility = View.GONE
                        binding.moreSelectConst.visibility = View.VISIBLE
                        adapterHistory.notifyDataSetChanged()
                    }
                }

            })
            // 可以在这里进行 RecyclerView 的设置等操作
            // 侧边栏的 historyLeftListRecycle 同理
            binding.historyLeftListRecycle.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
                adapter = adapterHistory
            }
//            binding.historyLeftListRecycle.layoutManager = LinearLayoutManager(this@MainActivity)
//            binding.historyLeftListRecycle.adapter = adapterHistory
            // 通知适配器数据已更改
            adapterHistory.notifyDataSetChanged()

        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(35)
    override fun onItemClick(chatItem: ChatItemRoom) {
        Log.e("ceshi","点击聊天历史列表：$chatItem")
        moreFunctionQuantity = 0
        imageCounter = 0
        isSendMessage.set(false)
        binding.hideImage.visibility = View.GONE
        messageList.clear()
        mMessageList.clear()
        messageList = chatItem.messages
        for (message in messageList){
            mMessageList.add(message.message)
        }
        chatTitle = chatItem.title
        modelType = chatItem.modelType
        mModelTypeHistory = chatItem.modelType
        chatTime = chatItem.time
        binding.chatTitleTv.text = chatTitle
        binding.modeTypeTv.text = modelType

        isDeepThink = chatItem.isDeepThink
        isNetWorkThink = chatItem.isNetWorkThink
        isR1Fusion = chatItem.isR1Fusion
        if (isDeepThink){
            moreFunctionQuantity++
        }
        if (isNetWorkThink){
            moreFunctionQuantity++
        }
        if (isR1Fusion){
            moreFunctionQuantity++
        }
        if (messageList.isNotEmpty()){
            Log.e("ceshi","点击聊天历史列表：$messageList")
            if (messageList.last().message == "file:///android_asset/loading.html"){
                val send = messageList[messageList.size-2].message
                againSendQuestion(send)
            }
        }


        reSetChatMessages()
        if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }
        binding.newChatCon.visibility = View.GONE
        binding.chatRecyclerView.visibility = View.VISIBLE
        if (moreFunctionQuantity>0){
            binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
            binding.moreFunctionLine.visibility = View.VISIBLE
            binding.moreIdTv.text = moreFunctionQuantity.toString()
            binding.moreImage.setImageResource(R.drawable.icon_new_more1)
            binding.moreImage.setColorFilter(ContextCompat.getColor(this, R.color.color302AI), PorterDuff.Mode.SRC_IN)
        }else{
            binding.moreFunctionLine.visibility = View.GONE
            binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
            binding.moreImage.setImageResource(R.drawable.icon_new_more1)
            binding.moreImage.clearColorFilter()
        }

        slideBottom()

    }

    override fun onDeleteClick(selectList: MutableList<Int>) {
        //来自HomeMessageAdapter要删除的多个位置
        selectedList = selectList
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onBackFunctionClick(chatFunction: ChatBackMessage) {
        Log.e("ceshi","工具返回类型${chatFunction.doType}")
        when(chatFunction.doType){
            "share" -> {
                SystemUtils.shareContent(this@MainActivity,chatFunction.message)
            }
            "screenshot" -> {
                /*val fullScreenshot = RecyclerViewScreenshotUtils.captureFullRecyclerView(binding.chatRecyclerView)
                Log.e("ceshi","截图返回路径$fullScreenshot")
                fullScreenshot?.let { bitmap ->
                    //resultIv.setImageBitmap(bitmap) // 显示长截屏结果
                    // 可选：保存到本地文件
                    // saveBitmapToFile(bitmap)
                    // 3. 保存到相册
                    val savePath = ScreenshotSaver.saveBitmapToFile(this, bitmap)
                    Log.e("ceshi","截图返回路径$savePath")
                    if (savePath != null) {
                        // 保存成功，提示用户
                        Toast.makeText(this, "长截屏已保存到相册", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }*/
//                requestMediaProjectionPermission()
//                screenshotManager.startLongScreenshot()
                // 合并权限请求，避免重复请求
                /*val permissions = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                requestPermissions(permissions.toTypedArray(), 0)*/
//                // 第一次请求存储权限
//                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1101)
//                // 第一次请求存储权限
//                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1102)
                if (messageList.size == 0){
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.screenshot_can_not_toast_message), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    ScreenShotTools.instance.takeCapture(this, binding.chatRecyclerView, object :
                        IScreenShotCallBack {
                        override fun onResult(screenBitmap: ScreenBitmap?) {
                            screenBitmap?.let {
                                //ShowScreenImageActivity.action(this@MainActivity,it.filePath)
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.screenshot_save_toast_message), Toast.LENGTH_SHORT).show()
                                    binding.chatRecyclerView.layoutManager?.scrollToPosition(chatFunction.position-1)
                                }
                            }
                        }

                    })
                }


            }
            "chooseText" -> {
                CommonDialogUtils.showBottomSheetChooseTextDialog(this@MainActivity,chatFunction.message)
            }
            "uploadArchives" -> {

            }
            "addKb" -> {

            }
            "userCopy" -> {
                performVibration()
                SystemUtils.copyTextToClipboard(this@MainActivity,chatFunction.message)
            }
            "userEdit" -> {
                binding.messageEditText.setText(chatFunction.message)
            }
            "userAgain" -> {
                performVibration()
                val sendMessage = chatFunction.message
                Log.e("ceshi","位置：${chatFunction.position}信息：${sendMessage}")
                messageList.add(ChatMessage(sendMessage,true,"chat",false,false,
                    chatFunction.fileName,chatFunction.fileSize))
                mMessageList.add(sendMessage)
                messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false,chatFunction.fileName,chatFunction.fileSize))
                messageList[chatFunction.position].message = "这是删除过的内容变为空白"
                messageList[chatFunction.position+1].message = "这是删除过的内容变为空白"

                mMessageList[chatFunction.position] = "这是删除过的内容变为空白"
                mMessageList[chatFunction.position+1] = "这是删除过的内容变为空白"
                lifecycleScope.launch(Dispatchers.IO) {
                    isSendMessage.set(true)
                    if (sendMessage.contains("jpg") || sendMessage.contains("png") || sendMessage.contains("documents")){
                        val urlLists = StringObjectUtils.extractAllImageUrlsNew(sendMessage)
                        for (url in urlLists){
                            netUrlResultList.add(urlMapper.getNetworkUrl(url)!!)
                        }
                        chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,netUrlResultList,
                            false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
                        netUrlResultList.clear()
                    }else{
                        chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,
                            false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
                    }


                    //标题生成
                    if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                        chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
                    }else if (!isBuildTitleFirstTime && !isPrivate){
                        chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
                    }
                }
                binding.newChatCon.visibility = View.GONE
                binding.chatRecyclerView.visibility = View.VISIBLE

                // 完成使用后删除图片
                //val isDeleted = mPicFileUri?.let { it1 -> DrawableToUriUtil.deleteImageUri(this, it1) }


                // 通知适配器数据已更改
                messageAdapter.notifyDataSetChanged()
                //adapter.updateData(messageList)
            }
            "robotCopy" -> {
                performVibration()
            }
            "good" -> {
                performVibration()
                messageList[chatFunction.position].isGood = true

            }
            "cancelGood" -> {
                messageList[chatFunction.position].isGood = false
            }
            "bad" -> {
                badPosition = chatFunction.position
                messageList[chatFunction.position].isBad = true
                val dialog = FeedBackDialog(this)
                //dialog.setDefaultName("原来的名字") // 设置输入框默认文本
                dialog.setOnSaveClickListener { feedBack ->


                    Log.e("ceshi","反馈内容是：$feedBack")
                }
                dialog.setOnCancelClickListener {
                    // 点击“取消”后的逻辑
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.cancel_feedback_toast_message), Toast.LENGTH_SHORT).show()

                    messageList[badPosition].isBad = false
                    messageAdapter.notifyDataSetChanged()
                    //adapter.updateData(messageList)
                    //adapter.upDataCancelBad(true)
                }
                dialog.show()
            }
            "againRobot" -> {
                if (messageList.size == mMessageList.size){
                    performVibration()
                    val sendMessage = messageList[chatFunction.position-1].message
                    messageList.add(ChatMessage(sendMessage,true,"chat",false,false,
                        messageList[chatFunction.position-1].fileName,messageList[chatFunction.position-1].fileSize))

                    mMessageList.add(sendMessage)
                    messageList.add(ChatMessage("file:///android_asset/loading.html",false,"chat",false,false,chatFunction.fileName,chatFunction.fileSize))
                    messageList[chatFunction.position].message = "这是删除过的内容变为空白"
                    messageList[chatFunction.position-1].message = "这是删除过的内容变为空白"

                    mMessageList[chatFunction.position] = "这是删除过的内容变为空白"
                    mMessageList[chatFunction.position-1] = "这是删除过的内容变为空白"
                    lifecycleScope.launch(Dispatchers.IO) {
                        isSendMessage.set(true)
                        if (sendMessage.contains("jpg") || sendMessage.contains("png") || sendMessage.contains("documents")){
                            val urlLists = StringObjectUtils.extractAllImageUrlsNew(sendMessage)
                            for (url in urlLists){
                                netUrlResultList.add(urlMapper.getNetworkUrl(url)!!)
                            }
                            chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,netUrlResultList,
                                false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
                            netUrlResultList.clear()
                        }else{
                            chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,
                                false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
                        }


                        //标题生成
                        if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                            chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
                        }else if (!isBuildTitleFirstTime && !isPrivate){
                            chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
                        }
                    }
                    binding.newChatCon.visibility = View.GONE
                    binding.chatRecyclerView.visibility = View.VISIBLE

                    // 完成使用后删除图片
                    //val isDeleted = mPicFileUri?.let { it1 -> DrawableToUriUtil.deleteImageUri(this, it1) }


                    // 通知适配器数据已更改
                    messageAdapter.notifyDataSetChanged()
                    //adapter.updateData(messageList)
                }else{
                    Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.again_later_toast_message), Toast.LENGTH_SHORT).show()
                }


            }

            "codePre" -> {
                // 初始化Markwon（包含常用插件）
                var codeStr = chatFunction.message
                Log.e("ceshi","位置是${chatFunction.position},,字符串：$codeStr")
                Log.e("ceshi","位置载入html字符串：${
                    StringObjectUtils.extractHtmlFromMarkdownCode(
                        codeStr
                    )
                }")
                showBottomSheetCodePreDialog(this@MainActivity,codeStr,chatFunction.name)


            }
            "userImageCopy" -> {
                Log.e("ceshi","用户图片的URL：${chatFunction.message}")
            }
            "userImagePre" -> {
                Log.e("ceshi","预览用户图片的URL：${chatFunction.message}")
                // 合并权限请求，避免重复请求
                val permissions = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                requestPermissions(permissions.toTypedArray(), 0)
                if (chatFunction.message.contains("media.documents/")){
                    //openDocument(chatFunction.message)
                    // 启动文件选择器，等待用户选择
                    openDocumentPicker()

                }else{
                    showImagePreviewDialog(chatFunction.message)
                }

            }

        }
    }

    // 打开系统文件选择器，让用户选择文件
    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // 指定可选择的文件类型（*/* 表示所有类型，可根据需求限制，如 "application/pdf"）
            type = "*/*"
            // 添加类别，确保能被文件选择器处理
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // 启动文件选择器，等待用户选择
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT)
    }

    /**
     * 打开文档的方法
     */
    // 打开文档（使用获得权限的URI）
    private fun openDocument(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            // 授予临时访问权限（给打开文件的应用）
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, ContextCompat.getString(this, R.string.open_file_toast_message)))
        } else {
            Toast.makeText(this,  ContextCompat.getString(this, R.string.open_file_fail_toast_message), Toast.LENGTH_SHORT).show()
        }
    }

    // 申请持久访问权限
    private fun takePersistableUriPermission(uri: Uri) {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        // 向系统申请持久权限
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    private fun getChatTitle(message:String):String{
        return "根据用户输入的内容生成一个会话标题，长度适中，简洁明了，要求是会话标题需要涵盖内容的重点。输入内容：<text>$message</text>始终以${ContextCompat.getString(this, R.string.get_title_message)}纯文本格式直接返回标题，不要添加任何其他内容。将标题控制在${ContextCompat.getString(this, R.string.get_title_message1)}以内，不要超过这个限制。"
    }

    private fun reSetChatMessages(){
        messageAdapter = ChatAdapter(messageList,this,this)
        binding.chatRecyclerView.adapter = messageAdapter
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetMoreDialog() {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_more_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<ImageView>(R.id.btnMoreClose)
        btnClose.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            // 关闭 BottomSheetDialog
            bottomSheetDialog.dismiss()

        }

        view.findViewById<ConstraintLayout>(R.id.cons3).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            bottomSheetDialog.dismiss()

        }
        Log.e("ceshi","是否是鸿蒙${DeviceDetector.isHuaweiDevice()}")
        if (DeviceDetector.isHuaweiDevice()){
            view.findViewById<ConstraintLayout>(R.id.cons2).visibility = View.GONE
        }else{
            view.findViewById<ConstraintLayout>(R.id.cons2).visibility = View.VISIBLE
        }

        view.findViewById<ConstraintLayout>(R.id.cons2).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dispatchTakePictureIntent()
            bottomSheetDialog.dismiss()
        }

        view.findViewById<ConstraintLayout>(R.id.cons4).setOnClickListener {
            // 点击时执行动画效果,上传文件
            ViewAnimationUtils.performClickEffect(it)
            openFilePicker()
            bottomSheetDialog.dismiss()

        }


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    // 打开文件选择器
    private fun openFilePicker() {
        isOpenFile = true
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // 所有类型的文件
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(
                Intent.createChooser(intent, "选择文件"),
                FILE_IMAGE_REQUEST
            )
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.install_file_mangager_toast_message), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetMoreFunctionDialog() {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_more_function_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        val webSelectImage = view.findViewById<ImageView>(R.id.webSelectImage)
        val webSelectedImage = view.findViewById<ImageView>(R.id.webSelectedImage)
        val webCons = view.findViewById<ConstraintLayout>(R.id.webCons)

        val deepThinkSelectImage = view.findViewById<ImageView>(R.id.deepThinkSelectImage)
        val deepThinkSelectedImage = view.findViewById<ImageView>(R.id.deepThinkSelectedImage)
        val deepCons = view.findViewById<ConstraintLayout>(R.id.deepCons)

        val thinkSelectImage = view.findViewById<ImageView>(R.id.thinkSelectImage)
        val thinkSelectedImage = view.findViewById<ImageView>(R.id.thinkSelectedImage)

        val mcpSelectImage = view.findViewById<ImageView>(R.id.mcpSelectImage)
        val mcpSelectedImage = view.findViewById<ImageView>(R.id.mcpSelectedImage)
        val mcpCons = view.findViewById<ConstraintLayout>(R.id.mcpCons)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<ImageView>(R.id.btnMoreClose)
        btnClose.setOnClickListener {
            // 关闭 BottomSheetDialog
            ViewAnimationUtils.performClickEffect(it)
            bottomSheetDialog.dismiss()

        }
        bottomSheetDialog.setOnDismissListener {
            if (moreFunctionQuantity>0){
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_bg_purple_more_function_line)
                binding.moreFunctionLine.visibility = View.VISIBLE
                binding.moreIdTv.text = moreFunctionQuantity.toString()
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.setColorFilter(ContextCompat.getColor(this, R.color.color302AI), PorterDuff.Mode.SRC_IN)
            }else{
                binding.moreFunctionLine.visibility = View.GONE
                binding.moreFrame.setBackgroundResource(R.drawable.shape_select_site_chat_edit_bg_write)
                binding.moreImage.setImageResource(R.drawable.icon_new_more1)
                binding.moreImage.clearColorFilter()
            }
        }

        Log.e("ceshi","功能deep$isDeepThink,net$isNetWorkThink,think$isR1Fusion")
        if (isDeepThink){
            deepThinkSelectImage.visibility = View.GONE
            deepThinkSelectedImage.visibility = View.VISIBLE
        }else{
            deepThinkSelectImage.visibility = View.VISIBLE
            deepThinkSelectedImage.visibility = View.GONE
        }
        if (isNetWorkThink){
            webSelectImage.visibility = View.GONE
            webSelectedImage.visibility = View.VISIBLE
        }else{
            webSelectImage.visibility = View.VISIBLE
            webSelectedImage.visibility = View.GONE
        }
        if (isR1Fusion){
            thinkSelectImage.visibility = View.GONE
            thinkSelectedImage.visibility = View.VISIBLE
        }else{
            thinkSelectImage.visibility = View.VISIBLE
            thinkSelectedImage.visibility = View.GONE
        }

        /*webSelectImage.setOnClickListener {
            webSelectImage.visibility = View.GONE
            webSelectedImage.visibility = View.VISIBLE
            isNetWorkThink = true
            moreFunctionQuantity++
        }
        webSelectedImage.setOnClickListener {
            webSelectImage.visibility = View.VISIBLE
            webSelectedImage.visibility = View.GONE
            isNetWorkThink = false
            moreFunctionQuantity--
        }*/
        webCons.setOnClickListener {
            performVibration()
            if (isNetWorkThink){
                webSelectImage.visibility = View.VISIBLE
                webSelectedImage.visibility = View.GONE
                isNetWorkThink = false
                moreFunctionQuantity--
            }else{
                webSelectImage.visibility = View.GONE
                webSelectedImage.visibility = View.VISIBLE
                isNetWorkThink = true
                moreFunctionQuantity++
            }
        }

        /*deepThinkSelectImage.setOnClickListener {
            deepThinkSelectImage.visibility = View.GONE
            deepThinkSelectedImage.visibility = View.VISIBLE
            isDeepThink = true
            moreFunctionQuantity++
        }
        deepThinkSelectedImage.setOnClickListener {
            deepThinkSelectImage.visibility = View.VISIBLE
            deepThinkSelectedImage.visibility = View.GONE
            isDeepThink = false
            moreFunctionQuantity--
        }*/
        deepCons.setOnClickListener {
            performVibration()
            if (isDeepThink){
                deepThinkSelectImage.visibility = View.VISIBLE
                deepThinkSelectedImage.visibility = View.GONE
                isDeepThink = false
                moreFunctionQuantity--
            }else{
                deepThinkSelectImage.visibility = View.GONE
                deepThinkSelectedImage.visibility = View.VISIBLE
                isDeepThink = true
                moreFunctionQuantity++
            }
        }

        thinkSelectImage.setOnClickListener {
            thinkSelectImage.visibility = View.GONE
            thinkSelectedImage.visibility = View.VISIBLE
            isR1Fusion = true
            moreFunctionQuantity++
        }
        thinkSelectedImage.setOnClickListener {
            thinkSelectImage.visibility = View.VISIBLE
            thinkSelectedImage.visibility = View.GONE
            isR1Fusion = false
            moreFunctionQuantity--
        }

        /*mcpSelectImage.setOnClickListener {
            mcpSelectImage.visibility = View.GONE
            mcpSelectedImage.visibility = View.VISIBLE
            moreFunctionQuantity++
        }
        mcpSelectedImage.setOnClickListener {
            mcpSelectImage.visibility = View.VISIBLE
            mcpSelectedImage.visibility = View.GONE
            moreFunctionQuantity--
        }*/
        mcpCons.setOnClickListener {
            Toast.makeText(this@MainActivity, "开发中，敬请期待", Toast.LENGTH_SHORT).show()
            performVibration()
            /*if (isMcp){
                isMcp = false
                mcpSelectImage.visibility = View.GONE
                mcpSelectedImage.visibility = View.VISIBLE
                moreFunctionQuantity++
            }else{
                isMcp = true
                mcpSelectImage.visibility = View.VISIBLE
                mcpSelectedImage.visibility = View.GONE
                moreFunctionQuantity--
            }*/
        }


//        view.findViewById<ImageView>(R.id.doPictureImage).setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
//            bottomSheetDialog.dismiss()
//
//        }
//
//        view.findViewById<ImageView>(R.id.doCameraImage).setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            dispatchTakePictureIntent()
//            bottomSheetDialog.dismiss()
//        }


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    // 调用相机
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // 确保有相机应用可以处理该Intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // 创建临时文件保存照片
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // 处理创建文件失败的情况
                    ex.printStackTrace()
                    null
                }
                // 继续只有在成功创建文件的情况下
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getFileProviderAuthority(this), // 替换为你的FileProvider authority
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                }
            }
        }
    }

    // 根据当前环境动态生成 authority
    fun getFileProviderAuthority(context: Context): String {
        return "${context.packageName}.fileprovider"
    }

    // 创建临时图片文件
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 创建唯一文件名
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* 前缀 */
            ".jpg", /* 后缀 */
            storageDir /* 目录 */
        ).apply {
            // 保存文件路径用于后续使用
            currentPhotoPath = absolutePath
        }

    }

    // 将图片添加到系统图库
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun addNewImageView(imageUrlLocal:String,mIsfile:Boolean) {
        Log.e("ceshi","添加视图")
        binding.imageLineHorScroll.visibility = View.VISIBLE
        imageUrlLocalList.add(imageUrlLocal)
        mImageUrlLocalList.add(imageUrlLocal)
        val removableLayout = RemovableImageLayout(this,listenerOver=this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        // 设置图片（示例：加载随机图片）
        removableLayout.setImageResource(imageUrlLocal,imageCounter,mIsfile,fileName, fileSize)

        // 添加到容器
        binding.imageLine.addView(removableLayout)
        imageCounter++

    }

    private fun showFileImageView(imageUrlLocal:String,mIsfile:Boolean) {
        Log.e("ceshi","文件添加视图$imageCounter")
        imageCounter--
        binding.imageLineHorScroll.visibility = View.VISIBLE
        val removableLayout = RemovableImageLayout(this,listenerOver=this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        // 设置图片（示例：加载随机图片）
        removableLayout.setImageResource(imageUrlLocal,imageCounter,mIsfile,fileName, fileSize)

        // 添加到容器
        binding.imageLine.addView(removableLayout)

    }

    private fun addNewImageViewShow(imageUrlLocal:String,count:Int) {
        Log.e("ceshi","0添加视图")
        binding.imageLineHorScroll.visibility = View.VISIBLE
        val removableLayout = RemovableImageLayout(this,listenerOver=this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        // 设置图片（示例：加载随机图片）
        removableLayout.setImageResource(imageUrlLocal,count,isFile,fileName, fileSize)

        // 添加到容器
        binding.imageLine.addView(removableLayout)


    }

    private fun rebuildImageContainer() {
        // 清空现有视图
        binding.imageLine.removeAllViews()

        var count = 0
        // 重新添加所有图片
        for (imageUrl in imageUrlLocalList) {
            addNewImageViewShow(imageUrl,count)
            count++
        }

        // 显示容器
        binding.imageLineHorScroll.visibility = View.VISIBLE
    }

    /**
     * 显示图片放大预览对话框
     */
    @SuppressLint("MissingInflatedId")
    private fun showImagePreviewDialog(imageUrl: String) {
        binding.imageLineHorScroll.visibility  = View.GONE
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null)
        val pvPreview = dialogView.findViewById<PhotoView>(R.id.pv_preview)

        // 使用 Glide 加载大图到 PhotoView（支持缩放）
        Glide.with(this)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
            .error(android.R.drawable.stat_notify_error) // 加载失败占位图
            .into(pvPreview)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("关闭") { dialog, _ -> dialog.dismiss()
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("ceshi","图片列表：${imageUrlLocalList.size}")
                    //binding.imageLineHorScroll.visibility  = View.VISIBLE
//                    for (resUrl in imageUrlLocalList.values) {
//                        resUrl.let {
//                            addNewImageView(it!!)
//                        }
//
//                    }
                    //binding.imageLineHorScroll.visibility = View.VISIBLE
                    rebuildImageContainer()
                }

            }
            .show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onOverItemClick(wordPrintOverItem: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onBackChatTool(backChatToolItem: BackChatToolItem) {
        TODO("Not yet implemented")
    }

    override fun onDeleteImagePosition(position: Int) {
        Log.e("ceshi","删除显示：${imageUrlLocalList},,${imageUrlServiceResultList},,${mImageUrlServiceResultList},,$position")
        if (imageUrlLocalList.size == imageUrlServiceResultList.size){
            imageUrlServiceResultList.remove(mImageUrlServiceResultList[position])
        }
        imageUrlLocalList.remove(mImageUrlLocalList[position])
        imageCounter--
        if (imageUrlLocalList.isEmpty()){
            binding.imageLineHorScroll.visibility = View.GONE
            binding.imageLine.removeAllViews()
            mImageUrlServiceResultList.clear()
            mImageUrlLocalList.clear()
            imageCounter = 0
        }
        Log.e("ceshi","删除了图片位置$position,,${imageUrlLocalList.isEmpty()}")
    }

    override fun onPreImageClick(resUrl: String) {
        if (resUrl.contains("media.documents/")){
            //openDocument(chatFunction.message)
            // 启动文件选择器，等待用户选择
            openDocumentPicker()

        }else{
            showImagePreviewDialog(resUrl)
        }
    }

    override fun onImageBackClick(backImage: ImageBack) {

    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetChatEditDialog(listener: OnChatTitleSelectedListener) {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_chat_edit_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

//        // 获取布局中的 Button，并设置点击事件
//        val btnClose = view.findViewById<ImageView>(R.id.btnMoreClose)
//        btnClose.setOnClickListener {
//            // 关闭 BottomSheetDialog
//            bottomSheetDialog.dismiss()
//
//        }

//        view.findViewById<ImageView>(R.id.doPictureImage).setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
//            bottomSheetDialog.dismiss()
//
//        }
//
//        view.findViewById<ImageView>(R.id.doCameraImage).setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            dispatchTakePictureIntent()
//            bottomSheetDialog.dismiss()
//        }
        var textCountsTv = view.findViewById<TextView>(R.id.textCountsTv)
        var editTitle = view.findViewById<EditText>(R.id.edit_title)
        var titleTv = view.findViewById<TextView>(R.id.titleTv)
        var cons1 = view.findViewById<ConstraintLayout>(R.id.cons1)
        val cueWordDialogConst = view.findViewById<ConstraintLayout>(R.id.cons6)
        val botShareLinkConst = view.findViewById<ConstraintLayout>(R.id.botShareLinkConst)
        if (chatTitle != ContextCompat.getString(this@MainActivity, R.string.chat_title) && binding.chatTitleTv.text != hideTitle){
            textCountsTv.text = chatTitle.length.toString()
            editTitle.setText(chatTitle)
            titleTv.visibility = View.VISIBLE
            cons1.visibility = View.VISIBLE
        }else if ( binding.chatTitleTv.text == hideTitle ){
            editTitle.isEnabled = false
            chatTitle = hideTitle
            titleTv.visibility = View.GONE
            cons1.visibility = View.GONE

        }else{
            editTitle.isEnabled = false
            titleTv.visibility = View.GONE
            cons1.visibility = View.GONE
        }

        cueWordDialogConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            CommonDialogUtils.showBottomSheetPromptDialog(this@MainActivity,dataStoreManager,lifecycleScope,prompt){
                selectedPrompt ->
                // 这里处理Dialog关闭后的逻辑
                Log.d("PromptResult", "选中的提示词: $selectedPrompt")
                // 更新UI或执行其他操作
                //updateUIWithPrompt(selectedPrompt)
                Log.e("ceshi","提示词：$selectedPrompt")
                prompt = selectedPrompt
                if (selectedPrompt == "这是删除过的内容变为空白"){
                    view.findViewById<TextView>(R.id.promptTv).text = ""
                }else{
                    view.findViewById<TextView>(R.id.promptTv).text = prompt
                }
            }

        }
        val kbConst = view.findViewById<ConstraintLayout>(R.id.kbConst)

        val modelSelectDialogConst = view.findViewById<ConstraintLayout>(R.id.modelSelectDialogConst)
        modelSelectDialogConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            CommonDialogUtils.showBottomSheetSelectModelDialog(this@MainActivity,modelList,dataStoreManager,lifecycleScope,modelType){
                    SelectModelData ->
                Log.d("PromptResult", "选中的模型信息: $SelectModelData")
                modelType = SelectModelData.model
                view.findViewById<TextView>(R.id.textModeTv).text = modelType
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.modeTypeTv.text = modelType
                }
            }
        }

        kbConst.setOnClickListener {
            Toast.makeText(this@MainActivity, "开发中，敬请期待", Toast.LENGTH_SHORT).show()
        }

        botShareLinkConst.setOnClickListener {
            Toast.makeText(this@MainActivity, "开发中，敬请期待", Toast.LENGTH_SHORT).show()
        }

        //Log.e("ceshi","提示词：$prompt")
        if (prompt == "这是删除过的内容变为空白"){
            view.findViewById<TextView>(R.id.promptTv).text = ""
        }else{
            view.findViewById<TextView>(R.id.promptTv).text = prompt
        }

        view.findViewById<TextView>(R.id.textModeTv).text = modelType


        bottomSheetDialog.setOnDismissListener {
            // 当Dialog关闭时，通过回调返回结果
            listener.onChatTitleSelected(ChatTitle(chatTitle,modelType))
        }




        //editTitle.setText(binding.chatTitleTv.text.toString())
        editTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
                textCountsTv.text = s.toString().length.toString()
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")

                if (s.toString() == ""){
                    if ( binding.chatTitleTv.text == hideTitle ){
                        chatTitle = hideTitle
                    }else{
                        chatTitle = ContextCompat.getString(this@MainActivity, R.string.chat_title)
                    }
                }else{
                    chatTitle = s.toString()
                }

            }
        })


        val seekBarDialog = view.findViewById<SeekBar>(R.id.seekBarDialog)
        lifecycleScope.launch(Dispatchers.IO) {
            val readTemperatureValue = dataStoreManager.readTemperatureValue.first()
            readTemperatureValue?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    seekBarDialog.setProgress((readTemperatureValue*100).toInt())
                }
            }
        }

        seekBarDialog.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // 当进度改变时调用
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 更新显示的数值
                //valueTextView.text = "当前值: $progress"
                Log.e("ceshi","温度当前值:$progress")
                performVibration()
            }

            // 开始滑动时调用
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 可以在这里处理滑动开始的逻辑
            }

            // 停止滑动时调用
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 可以在这里处理滑动结束的逻辑
                seekBar?.let {
                    // 例如：显示一个Toast提示最终值
//                    android.widget.Toast.makeText(
//                        this@MainActivity,
//                        "最终值: ${it.progress}",
//                        android.widget.Toast.LENGTH_SHORT
//                    ).show()
                    Log.e("ceshi","滑动温度当前值:${it.progress}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveTemperatureValue(it.progress/100.0)
                    }
                    temperature = it.progress/100.0

                }
            }
        })

        val screenshotDialogConst = view.findViewById<ConstraintLayout>(R.id.screenshotDialogConst)
        screenshotDialogConst.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            Log.e("ceshi","截图是空${messageList.size}")
            if (messageList.size == 0){
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.screenshot_can_not_toast_message), Toast.LENGTH_SHORT).show()
                }
            }else{
                ScreenShotTools.instance.takeCapture(this, binding.chatRecyclerView, object :
                    IScreenShotCallBack {
                    override fun onResult(screenBitmap: ScreenBitmap?) {
                        screenBitmap?.let {
                            //ShowScreenImageActivity.action(this@MainActivity,it.filePath)
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, ContextCompat.getString(this@MainActivity, R.string.screenshot_save_toast_message), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                })
            }

        }

        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }


    private fun toLogin(){
        val intent = Intent(this, LoginOneActivity::class.java)
        startActivity(intent)
    }

    private fun showRenameDialog(position: Int,oldName:String) {
        val dialog = RenameDialog(this,oldName)
        //dialog.setDefaultName("原来的名字") // 设置输入框默认文本
        dialog.setOnSaveClickListener { newName ->
            // 点击“保存”后的逻辑，newName 是输入框内容
            //Toast.makeText(this, "新名称：$newName", Toast.LENGTH_SHORT).show()
            // 这里可执行真正的重命名操作，比如更新数据、刷新 UI 等
            val job1 = lifecycleScope.launch(Dispatchers.IO) {
                if (chatDatabase.chatDao().checkTitleExists(chatListReversed[position].title)){
                    //
                    Log.e("ceshi","1collect")
                    val updatedItem  = ChatItemRoom(chatListReversed[position].id,newName,
                        chatListReversed[position].messages,chatListReversed[position].time,chatListReversed[position].modelType,chatListReversed[position].isDeepThink,
                        chatListReversed[position].isNetWorkThink,chatListReversed[position].userId,chatListReversed[position].isMe,chatListReversed[position].isCollected,chatListReversed[position].isR1Fusion)
                    chatDatabase.chatDao().updateChat(updatedItem)
                    // 2. 同步更新内存中的数据源（关键！）
                    launch(Dispatchers.Main) {

                        if (binding.chatTitleTv.text == chatListReversed[position].title){
                            chatTitle = newName
                            binding.chatTitleTv.text = chatTitle
                        }
                        chatListReversed[position] = updatedItem // 修改列表中的 item

                    }
                }
            }
            lifecycleScope.launch {
                job1.join()
                //chatList.removeAt(position)
                adapterHistory.notifyItemChanged(position)
            }

        }
        dialog.setOnCancelClickListener {
            // 点击“取消”后的逻辑
            Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.cancel_rename_toast_message), Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @SuppressLint("ClickableViewAccessibility")
    private fun doNewVoice(){
        binding.mikeImage.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "语音按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    touchDownY = event.rawY
                    Log.e("ceshi", "WebView按下事件")
                    // 手指按下时重置状态（避免上一次操作影响）
                    //isLongPressed = false
                    longPressStartTime = 0L
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_MOVE -> {
                    // 计算Y轴位移（负值表示上滑）
                    val deltaY = event.rawY - touchDownY
                    Log.e("ceshi", "0检测到上滑动作！$deltaY,,${-SWIPE_THRESHOLD},,${isLongPressed}")
                    false
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    if (isLongPressed){
                        isLongPressed = false
                        handleMoveNewEvent(true)
                    }
                    Log.e("ceshi", "WebView抬起事件")
                    false  // 让事件继续传递给WebView内部处理
                }



                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })


        binding.mikeImage.setOnLongClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                //录音权限检测
                val permission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    Log.d(PermissionUtils.TAG, "Record permission is granted")
                    binding.voiceWaveNewView.startAnim()
                    binding.voiceNewCon.visibility = View.VISIBLE

                    //binding.startVoiceTv.text = "抬起发送，上滑取消发送"
                    // 长按触发时记录时间和状态
                    longPressStartTime = SystemClock.elapsedRealtime()  // 系统启动至今的时间（毫秒）
                    isLongPressed = true

                    //开始录音
                    VoiceToTextUtils.startRecording(audioFilePath,this@MainActivity)
                } else {
                    Log.d(PermissionUtils.TAG, "Requesting record permission")
                    checkRecordPermission(this@MainActivity)
                }


                // 返回值说明：
                // true 表示消费该长按事件（后续不会触发其他长按相关事件）
                // false 表示不消费，可能导致父布局或其他监听器处理
            }else{
                toLogin()
            }
            true
        }

        binding.mikeImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isTrueApiKey){
                Toast.makeText(this, ContextCompat.getString(this@MainActivity, R.string.voice_short_can_not_message), Toast.LENGTH_SHORT).show()
            }else{
                toLogin()
            }
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @SuppressLint("ClickableViewAccessibility")
    private fun doVoice(){
        binding.startVoiceTv.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "语音按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    touchDownY = event.rawY
                    Log.e("ceshi", "WebView按下事件")
                    // 手指按下时重置状态（避免上一次操作影响）
                    //isLongPressed = false
                    longPressStartTime = 0L
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_MOVE -> {
                    // 计算Y轴位移（负值表示上滑）
                    val deltaY = event.rawY - touchDownY
                    Log.e("ceshi", "0检测到上滑动作！$deltaY,,${-SWIPE_THRESHOLD},,${isLongPressed}")
                    if (isLongPressed){
                        handleMoveEvent(event)
                    }
//                    // 判断是否为上滑动作（位移超过阈值且已触发长按）
//                    if (deltaY < -SWIPE_THRESHOLD && isLongPressed) {
//
//                        binding.voiceWaveView.visibility = View.GONE
//                        binding.voiceWaveView.stopAnim()  // 启动波浪动画
//
//                        // 消费事件，阻止继续传递
//                        isLongPressed = false
//                        VoiceToTextUtils.stopRecording()
//                    }else if(deltaY < -10 && isLongPressed){
//
//                    }
                    false
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    isLongPressed = false
                    Log.e("ceshi", "WebView抬起事件")
                    false  // 让事件继续传递给WebView内部处理
                }



                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })

        // 设置长按事件监听器
        binding.startVoiceTv.setOnLongClickListener { view ->

            binding.doVoiceLine.visibility = View.VISIBLE
            binding.voiceWaveView.visibility = View.VISIBLE
            binding.voiceWaveView.startAnim()  // 启动波浪动画

            //binding.startVoiceTv.text = "抬起发送，上滑取消发送"
            // 长按触发时记录时间和状态
            longPressStartTime = SystemClock.elapsedRealtime()  // 系统启动至今的时间（毫秒）
            isLongPressed = true

            //开始录音
            VoiceToTextUtils.startRecording(audioFilePath,this@MainActivity)

            // 返回值说明：
            // true 表示消费该长按事件（后续不会触发其他长按相关事件）
            // false 表示不消费，可能导致父布局或其他监听器处理
            true
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun handleMoveEvent(event: MotionEvent) {
        // 获取发送按钮和取消按钮的位置信息
        val sendBtnLocation = IntArray(2)
        binding.voiceSendBtn.getLocationOnScreen(sendBtnLocation)
        val sendBtnRect = android.graphics.Rect(
            sendBtnLocation[0],
            sendBtnLocation[1],
            sendBtnLocation[0] + binding.voiceSendBtn.width,
            sendBtnLocation[1] + binding.voiceSendBtn.height
        )

        val cancelBtnLocation = IntArray(2)
        binding.voiceCancelBtn.getLocationOnScreen(cancelBtnLocation)
        val cancelBtnRect = android.graphics.Rect(
            cancelBtnLocation[0],
            cancelBtnLocation[1],
            cancelBtnLocation[0] + binding.voiceCancelBtn.width,
            cancelBtnLocation[1] + binding.voiceCancelBtn.height
        )

        // 检查手指是否在发送按钮区域
        val isOverSendBtn = sendBtnRect.contains(event.rawX.toInt(), event.rawY.toInt())
        // 检查手指是否在取消按钮区域
        val isOverCancelBtn = cancelBtnRect.contains(event.rawX.toInt(), event.rawY.toInt())

        // 更新UI状态
        if (isOverSendBtn) {
            //tipText.text = "松开发送"
            //停止录音并发送录音文件
            VoiceToTextUtils.stopRecording()
            val job = lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val requestFile = RequestBody.create(
                        "audio/mpeg".toMediaTypeOrNull(),  // MP3文件的MIME类型
                        File(audioFilePath)  // 待上传的文件对象
                    )

                    val filePart = MultipartBody.Part.createFormData(
                        "file",  // API接口要求的文件参数名（需与后端约定）
                        "temp_audio.mp3",  // 上传时的文件名（可选，后端可自定义）
                        requestFile  // 前面生成的RequestBody
                    )
                    chatViewModel.audioToText(filePart,apiKey,apiService)
                }catch (e:IOException){
                    Log.e("ceshi","文件创建错误：${e.toString()}")
                }


            }
            //VoiceToTextUtils.stopRecording()
            ViewAnimationUtils.performClickEffect(binding.voiceSendBtn)
            binding.doVoiceLine.visibility = View.GONE
            binding.voiceWaveView.visibility = View.GONE
            binding.voiceWaveView.stopAnim()  // 启动波浪动画

        } else if (isOverCancelBtn) {
            //tipText.text = "松开取消"
            VoiceToTextUtils.stopRecording()
            ViewAnimationUtils.performClickEffect(binding.voiceCancelBtn)
            binding.doVoiceLine.visibility = View.GONE
            binding.voiceWaveView.visibility = View.GONE
            binding.voiceWaveView.stopAnim()  // 启动波浪动画
        } else {
            //按住说话
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun handleMoveNewEvent(isUp: Boolean) {
        // 更新UI状态
        if (isUp) {
            //tipText.text = "松开发送"
            //停止录音并发送录音文件
            VoiceToTextUtils.stopRecording()
            val job = lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val requestFile = RequestBody.create(
                        "audio/mpeg".toMediaTypeOrNull(),  // MP3文件的MIME类型
                        File(audioFilePath)  // 待上传的文件对象
                    )

                    val filePart = MultipartBody.Part.createFormData(
                        "file",  // API接口要求的文件参数名（需与后端约定）
                        "temp_audio.mp3",  // 上传时的文件名（可选，后端可自定义）
                        requestFile  // 前面生成的RequestBody
                    )
                    chatViewModel.audioToText(filePart,apiKey,apiService)
                }catch (e:IOException){
                    Log.e("ceshi","文件创建错误：${e.toString()}")
                }


            }
            //VoiceToTextUtils.stopRecording()
            ViewAnimationUtils.performClickEffect(binding.voiceSendBtn)
            binding.voiceNewCon.visibility = View.GONE
            binding.voiceWaveNewView.stopAnim()  // 停止波浪动画

        }
    }


    @RequiresApi(35)
    private fun initGetLastMessage(message: String){

        val job2 = lifecycleScope.launch(Dispatchers.IO) {
            val chatItemHistory = chatDatabase.chatDao().getLastChatItem()

            if (chatItemHistory != null){
                Log.e("ceshi","Received chatItemHistory item: ${chatItemHistory.messages}")
                if (Build.VERSION.SDK_INT >= 35) {

                }
                var mChatItemHistory = chatItemHistory.messages
                mChatItemHistory.removeLast()
                mChatItemHistory.add(ChatMessage(message,false,"chat",false,false))
                //直接插入，做了title唯一性，如果有了就替换成最新的
                chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatItemHistory.title, mChatItemHistory, chatItemHistory.time,
                    chatItemHistory.modelType,chatItemHistory.isDeepThink,chatItemHistory.isNetWorkThink,
                    chatItemHistory.userId,chatItemHistory.isMe,chatItemHistory.isCollected,chatItemHistory.isR1Fusion))
//                messageList.removeLast()
//                messageList.add(ChatMessage(it,false,"chat",false,false))

            }


        }



    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun againSendQuestion(sendMessage:String){
        lifecycleScope.launch(Dispatchers.IO) {
            isSendMessage.set(true)

            if (sendMessage.contains("jpg") || sendMessage.contains("png") || sendMessage.contains("documents")){
                val urlLists = StringObjectUtils.extractAllImageUrlsNew(sendMessage)
                for (url in urlLists){
                    netUrlResultList.add(urlMapper.getNetworkUrl(url)!!)
                }
                chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,netUrlResultList,
                    false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
                netUrlResultList.clear()
            }else{
                chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,
                    false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)
            }


            chatViewModel.sendQuestion(sendMessage,modelType,isNetWorkThink,isDeepThink,this@MainActivity,userId,imageUrlServiceResultList,
                false,apiKey,false,apiService,false,mMessageList,"302.AI",prompt,temperature,searchServiceType,isDeepThink)

            //标题生成
            if (chatTitle.contains(ContextCompat.getString(this@MainActivity, R.string.chat_title)) && !isPrivate){
                chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
            }else if (!isBuildTitleFirstTime && !isPrivate){
                chatViewModel.sendQuestionGetTitle(this@MainActivity,getChatTitle(sendMessage),buildTitleModelType,apiKey,apiService)
            }
        }
    }

    private fun slideBottom(){
        /*val currentHeight = getRecyclerViewContentHeight(binding.chatRecyclerView)
        //val currentHeightScroll = binding.scroll3.height
        val phoneHeight = ScreenUtils.getScreenHeight(
            this@MainActivity
        )

        Log.e("ceshi","0显示的recycleView屏幕：$currentHeight")
        Log.e("ceshi","0屏幕的整体高度：${
            ScreenUtils.getScreenHeight(
                this@MainActivity
            )
        }")//1097/1640
        // 当 RecyclerView 高度发生变化时触发滚动
        //Log.e("ceshi","是否滚动：${currentHeight/phoneHeight > 1000/1640}")
        // 转成浮点运算，确保结果正确
        val condition = (currentHeight.toDouble() / phoneHeight) > (1000.0 / 1640)
        Log.e("ceshi","是否滚动：$condition")
        if (condition && isSlideBottomSwitch) {
            (binding.chatRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
        }*/
        if (isSlideBottomSwitch){
            if (messageList.size > 0){
                binding.chatRecyclerView.layoutManager?.scrollToPosition(messageList.size-1)
            }
        }



    }

    private fun applyLanguage() {
        val language = LanguageUtil.getSavedLanguage(this)
        val context = LanguageUtil.applyLanguage(this, language)
        val resources = context.resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics
        // 更新配置
        resources.updateConfiguration(configuration, displayMetrics)
    }

    private fun refreshUI(setMsg:MainMessage) {
//        val language = LanguageUtil.getSavedLanguage(this)
//        // 1. 应用新语言配置到当前 Activity
//        LanguageUtil.applyLanguageToActivity(this, language)

        Log.e("ceshi","获取的引文：${ setMsg}")
        binding.bottomTv.text = setMsg.bottomMsg
        binding.welcomeTv.text = setMsg.welcomeMsg
        binding.messageEditText.hint = setMsg.senMsg
        mReadImageUrl = setMsg.imageUrl
        // 方法1：使用内置的CircleCrop变换
        Glide.with(this@MainActivity)
            .load(setMsg.imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.userImage)

    }

    // 隐藏软键盘的方法
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // 检查是否有焦点视图
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            // 可选：清除焦点
            it.clearFocus()
        }
    }

    /**
     * 执行震动反馈
     * 适配不同Android版本
     */
    private fun performVibration() {
        // 检查设备是否支持震动
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android O及以上版本
                // 创建震动效果：震动30毫秒
                val vibrationEffect = VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                // Android O以下版本
                @Suppress("DEPRECATION")
                vibrator.vibrate(60) // 震动30毫秒
            }
        }
    }

    private fun insertUserConfiguration(userId:String){
        lifecycleScope.launch(Dispatchers.IO) {
            val mUserId = dataStoreManager.readUserEmailData.first()?:""
            if (mUserId == ""){
                dataStoreManager.saveUserEmail(userId)
                val userConfigurationRoom = chatDatabase.chatDao().getUserConfigByUserId(userId)
                Log.e("ceshi","获取的配置信息:$userConfigurationRoom")
                if (userConfigurationRoom != null){
                    userConfigurationRoom?.let {
                        //val readAppEmojisData = userConfigurationRoom.appEmojisData
                        val readImageUrl = userConfigurationRoom.appEmojisData
                        mReadImageUrl = readImageUrl
                        val readBuildTitleModelType = userConfigurationRoom.defaultBuildTitleModelType
                        val readModelType = userConfigurationRoom.defaultChatModelType
                        val readUserEmailData = userConfigurationRoom.userId
                        val systemLanguage = userConfigurationRoom.systemLanguage
                        val systemTheme = userConfigurationRoom.systemTheme
                        val readSlideBottomSwitch = userConfigurationRoom.slideBottomSwitch
                        val readUseTracelessSwitch = userConfigurationRoom.useTracelessSwitch
                        val modelList = userConfigurationRoom.modelList
                        val readSearchServiceType = userConfigurationRoom.searchServiceType
                        val readBuildTitleTime = userConfigurationRoom.buildTitleTime
                        //插入相对应数据库
                        //dataStoreManager.saveAppEmojisData(readAppEmojisData)
                        dataStoreManager.saveImageUrl(readImageUrl)
                        dataStoreManager.saveBuildTitleModeTypeData(readBuildTitleModelType)
                        dataStoreManager.saveModelType(readModelType)
                        dataStoreManager.saveModelList(modelList)
                        dataStoreManager.saveSearchServiceTypeData(readSearchServiceType)
                        dataStoreManager.saveSlideBottomSwitch(readSlideBottomSwitch)
                        dataStoreManager.saveUseTracelessSwitch(readUseTracelessSwitch)
                        dataStoreManager.saveBuildTitleTimeData(readBuildTitleTime)



                        /*if (modelList.isNullOrEmpty()){
                            dataStoreManager.saveModelList(modelList)
                        }*/

                        LanguageUtil.saveLanguageSetting(this@MainActivity, systemLanguage)
                    }
            }

            }


        }
    }

    // 假设 modelList 是 CopyOnWriteArrayList<String> 类型
    fun removeDuplicates(modelList: CopyOnWriteArrayList<String>) {
        // 1. 将列表转换为 LinkedHashSet（去重且保留顺序）
        val uniqueSet = LinkedHashSet(modelList)
        // 2. 清空原列表
        modelList.clear()
        // 3. 将去重后的元素添加回原列表（或创建新的 CopyOnWriteArrayList）
        modelList.addAll(uniqueSet)
    }

    /**
     * 保存 chatRecyclerView 的初始布局参数
     * 需在视图布局完成后获取（否则参数可能未初始化）
     */
    private fun saveInitialRecyclerLayoutParams() {
        // 监听视图布局完成事件
        binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 移除监听器，避免重复调用
                binding.chatRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // 获取初始布局参数（父布局是RelativeLayout，所以强转为RelativeLayout.LayoutParams）
                initialRecyclerLayoutParams = binding.chatRecyclerView.layoutParams as RelativeLayout.LayoutParams
                Log.e("ceshi","初始高度：${initialRecyclerLayoutParams?.height}")
            }
        })
    }

    /**
     * 恢复 chatRecyclerView 到初始高度
     */
    private fun restoreInitialHeight() {
        initialRecyclerLayoutParams?.let { initialParams ->
            // 重新设置初始布局参数（包含初始高度）
            binding.chatRecyclerView.layoutParams.height = -1
            // 强制刷新布局，确保高度立即生效
            binding.chatRecyclerView.requestLayout()
        }
    }


}