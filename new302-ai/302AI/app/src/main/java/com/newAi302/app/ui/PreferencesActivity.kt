package com.newAi302.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.databinding.ActivityPreferencesBinding
import com.newAi302.app.databinding.ActivitySettingBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.UserConfigurationRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreferencesActivity : BaseActivity() {
    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var dialogUtils: DialogUtils
    private lateinit var dataStoreManager: DataStoreManager
    private var searchServiceType = "search1api"
    private lateinit var chatDatabase: ChatDatabase
    private var isSlideBottomSwitch = false
    private var isUseTracelessSwitch = false
    private var searchService = "search1api"
    private var buildTitleTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        buildTitleTime = ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_chat_time_one_message)
        initView()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch((Dispatchers.IO)) {
            val readUseTracelessSwitch = dataStoreManager.readUseTracelessSwitch.first()?:false
            val readSlideBottomSwitch = dataStoreManager.readSlideBottomSwitch.first()?:false
            val readSearchServiceType = dataStoreManager.readSearchServiceType.first()?:"search1api"
            val readBuildTitleTime = dataStoreManager.readBuildTitleTime.first()?:buildTitleTime

            val readModelType = dataStoreManager.readModelType.first()?:"gemini-2.5-flash-nothink"


            val readChatDefaultModelType = dataStoreManager.readChatDefaultModelType.first()?:"gemini-2.5-flash-nothink"
            val readBuildTitleModelType = dataStoreManager.readBuildTitleModelType.first()?:"gemini-2.5-flash-nothink"

//            isSlideBottomSwitch = readSlideBottomSwitch
//            isUseTracelessSwitch = readUseTracelessSwitch
//            defaultModelType = readModelType
//            defaultBuildTitleModelType = readBuildTitleModelType
//            searchService = readSearchServiceType
//            buildTitleTime = readBuildTitleTime

            lifecycleScope.launch(Dispatchers.Main) {
                searchServiceType = readSearchServiceType
                binding.getUseTracelessSwitch.isChecked = readUseTracelessSwitch
                binding.slideBottomSwitch.isChecked = readSlideBottomSwitch

                binding.settingSearchTV.text = readSearchServiceType

                binding.settingGetTitleTV.text = readBuildTitleTime

                binding.settingModelTypeTV.text = readModelType
                binding.settingGetTitleModelTypeTV.text = readBuildTitleModelType

            }
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = dataStoreManager.readUserEmailData.first()?:""
            var userConfigurationRoom = chatDatabase.chatDao().getUserConfigByUserId(userId)
            userConfigurationRoom?.slideBottomSwitch = isSlideBottomSwitch
            userConfigurationRoom?.useTracelessSwitch = isUseTracelessSwitch
            userConfigurationRoom?.searchServiceType = searchService
            userConfigurationRoom?.buildTitleTime = buildTitleTime

//            userConfigurationRoom?.let {
//                chatDatabase.chatDao().insertUserConfig(
//                    UserConfigurationRoom(0,userConfigurationRoom.userId,userConfigurationRoom.systemLanguage,"light",readUseTracelessSwitch,
//                        readSlideBottomSwitch,readAppEmojisData,readSearchServiceType,readModelType,readBuildTitleModelType,modelList,readBuildTitleTime)
//                )
//            }



        }
    }


    private fun initView(){
        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_build_title_model_time_one_message) -> {
                    buildTitleTime = ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_chat_time_one_message)
                    binding.settingGetTitleTV.text = buildTitleTime
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveBuildTitleTimeData(buildTitleTime)
                    }
                }
                ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_build_title_model_time_every_message) -> {
                    buildTitleTime = ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_chat_time_every_message)
                    binding.settingGetTitleTV.text = buildTitleTime
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveBuildTitleTimeData(buildTitleTime)
                    }
                }


            }
        }

        binding.backPreferencesImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            finish()
        }
        binding.cons5.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            showBottomSheetMoreSearchDialog()
        }
        binding.cons1.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(this,PreferencesDefaultChatActivity::class.java)
            intent.putExtra("setting_type","chatModel")
            startActivity(intent)
        }
        binding.cons3.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(this,PreferencesDefaultChatActivity::class.java)
            intent.putExtra("setting_type","titleModel")
            startActivity(intent)
        }

        binding.cons2.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val options = mutableListOf(ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_build_title_model_time_one_message),
                ContextCompat.getString(this@PreferencesActivity, R.string.setting_preferences_build_title_model_time_every_message))
            dialogUtils.setupPopupWindow(options,"settingTitleGetModelTypeList",this)
            dialogUtils.showPopup(binding.settingGetTitleLine)
        }

        binding.getUseTracelessSwitch.setOnCheckedChangeListener { _, isChecked ->
            isUseTracelessSwitch = isChecked

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入是否使用无痕对话：${isChecked}")
                dataStoreManager.saveUseTracelessSwitch(isChecked)
            }

        }

        binding.slideBottomSwitch.setOnCheckedChangeListener { _, isChecked ->

            isSlideBottomSwitch = isChecked
            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入是否自动滑动底部开关：${isChecked}")
                dataStoreManager.saveSlideBottomSwitch(isChecked)
            }

        }

    }


    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetMoreSearchDialog() {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(this@PreferencesActivity)

        // 为 BottomSheetDialog 设置布局
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_more_search_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        val search1apiConst = view.findViewById<ConstraintLayout>(R.id.cons1)
        val tavilyConst = view.findViewById<ConstraintLayout>(R.id.cons2)
        val exaConst = view.findViewById<ConstraintLayout>(R.id.cons3)
        val bochaaiConst = view.findViewById<ConstraintLayout>(R.id.cons4)

        val search1apiSelectImage = view.findViewById<ImageView>(R.id.search1apiSelectImage)
        val search1apiSelectedImage = view.findViewById<ImageView>(R.id.search1apiSelectedImage)

        val tavilySelectImage = view.findViewById<ImageView>(R.id.tavilySelectImage)
        val tavilySelectedImage = view.findViewById<ImageView>(R.id.tavilySelectedImage)

        val exaSelectImage = view.findViewById<ImageView>(R.id.exaSelectImage)
        val exaSelectedImage = view.findViewById<ImageView>(R.id.exaSelectedImage)

        val bochaaiSelectImage = view.findViewById<ImageView>(R.id.bochaaiSelectImage)
        val bochaaiSelectedImage = view.findViewById<ImageView>(R.id.bochaaiSelectedImage)

        lifecycleScope.launch(Dispatchers.IO) {
            val readSearchServiceType = dataStoreManager.readSearchServiceType.first()?:"search1api"
            searchServiceType = readSearchServiceType
            lifecycleScope.launch(Dispatchers.Main) {
                when(searchServiceType){
                    "search1api" -> {
                        search1apiSelectImage.visibility = View.GONE
                        search1apiSelectedImage.visibility = View.VISIBLE

                        tavilySelectImage.visibility = View.VISIBLE
                        tavilySelectedImage.visibility = View.GONE
                        exaSelectImage.visibility = View.VISIBLE
                        exaSelectedImage.visibility = View.GONE
                        bochaaiSelectImage.visibility = View.VISIBLE
                        bochaaiSelectedImage.visibility = View.GONE
                    }
                    "tavily" -> {
                        tavilySelectImage.visibility = View.GONE
                        tavilySelectedImage.visibility = View.VISIBLE

                        search1apiSelectImage.visibility = View.VISIBLE
                        search1apiSelectedImage.visibility = View.GONE
                        exaSelectImage.visibility = View.VISIBLE
                        exaSelectedImage.visibility = View.GONE
                        bochaaiSelectImage.visibility = View.VISIBLE
                        bochaaiSelectedImage.visibility = View.GONE
                    }
                    "exa" -> {
                        exaSelectImage.visibility = View.GONE
                        exaSelectedImage.visibility = View.VISIBLE

                        search1apiSelectImage.visibility = View.VISIBLE
                        search1apiSelectedImage.visibility = View.GONE
                        tavilySelectImage.visibility = View.VISIBLE
                        tavilySelectedImage.visibility = View.GONE
                        bochaaiSelectImage.visibility = View.VISIBLE
                        bochaaiSelectedImage.visibility = View.GONE
                    }
                    "bochaai" -> {
                        bochaaiSelectImage.visibility = View.GONE
                        bochaaiSelectedImage.visibility = View.VISIBLE

                        search1apiSelectImage.visibility = View.VISIBLE
                        search1apiSelectedImage.visibility = View.GONE
                        tavilySelectImage.visibility = View.VISIBLE
                        tavilySelectedImage.visibility = View.GONE
                        exaSelectImage.visibility = View.VISIBLE
                        exaSelectedImage.visibility = View.GONE
                    }



                }
            }
        }



        search1apiConst.setOnClickListener {
            if (search1apiSelectImage.visibility == View.VISIBLE){
                binding.settingSearchTV.text = "search1api"
                searchService = "search1api"
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveSearchServiceTypeData("search1api")
                }
                search1apiSelectImage.visibility = View.GONE
                search1apiSelectedImage.visibility = View.VISIBLE

                tavilySelectImage.visibility = View.VISIBLE
                tavilySelectedImage.visibility = View.GONE
                exaSelectImage.visibility = View.VISIBLE
                exaSelectedImage.visibility = View.GONE
                bochaaiSelectImage.visibility = View.VISIBLE
                bochaaiSelectedImage.visibility = View.GONE
            }
        }


        /*search1apiSelectImage.setOnClickListener {
            binding.settingSearchTV.text = "search1api"
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveSearchServiceTypeData("search1api")
            }
            search1apiSelectImage.visibility = View.GONE
            search1apiSelectedImage.visibility = View.VISIBLE

            tavilySelectImage.visibility = View.VISIBLE
            tavilySelectedImage.visibility = View.GONE
            exaSelectImage.visibility = View.VISIBLE
            exaSelectedImage.visibility = View.GONE
            bochaaiSelectImage.visibility = View.VISIBLE
            bochaaiSelectedImage.visibility = View.GONE
        }*/
        tavilyConst.setOnClickListener {
            if (tavilySelectImage.visibility == View.VISIBLE){
                binding.settingSearchTV.text = "tavily"
                searchService = "tavily"
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveSearchServiceTypeData("tavily")
                }
                tavilySelectImage.visibility = View.GONE
                tavilySelectedImage.visibility = View.VISIBLE

                search1apiSelectImage.visibility = View.VISIBLE
                search1apiSelectedImage.visibility = View.GONE
                exaSelectImage.visibility = View.VISIBLE
                exaSelectedImage.visibility = View.GONE
                bochaaiSelectImage.visibility = View.VISIBLE
                bochaaiSelectedImage.visibility = View.GONE
            }
        }

        /*tavilySelectImage.setOnClickListener {
            binding.settingSearchTV.text = "tavily"
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveSearchServiceTypeData("tavily")
            }
            tavilySelectImage.visibility = View.GONE
            tavilySelectedImage.visibility = View.VISIBLE

            search1apiSelectImage.visibility = View.VISIBLE
            search1apiSelectedImage.visibility = View.GONE
            exaSelectImage.visibility = View.VISIBLE
            exaSelectedImage.visibility = View.GONE
            bochaaiSelectImage.visibility = View.VISIBLE
            bochaaiSelectedImage.visibility = View.GONE
        }*/

        /*exaSelectImage.setOnClickListener {
            binding.settingSearchTV.text = "exa"
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveSearchServiceTypeData("exa")
            }
            exaSelectImage.visibility = View.GONE
            exaSelectedImage.visibility = View.VISIBLE

            search1apiSelectImage.visibility = View.VISIBLE
            search1apiSelectedImage.visibility = View.GONE
            tavilySelectImage.visibility = View.VISIBLE
            tavilySelectedImage.visibility = View.GONE
            bochaaiSelectImage.visibility = View.VISIBLE
            bochaaiSelectedImage.visibility = View.GONE
        }*/

        exaConst.setOnClickListener {
            if (exaSelectImage.visibility == View.VISIBLE){
                binding.settingSearchTV.text = "exa"
                searchService = "exa"
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveSearchServiceTypeData("exa")
                }
                exaSelectImage.visibility = View.GONE
                exaSelectedImage.visibility = View.VISIBLE

                search1apiSelectImage.visibility = View.VISIBLE
                search1apiSelectedImage.visibility = View.GONE
                tavilySelectImage.visibility = View.VISIBLE
                tavilySelectedImage.visibility = View.GONE
                bochaaiSelectImage.visibility = View.VISIBLE
                bochaaiSelectedImage.visibility = View.GONE
            }
        }



        /*bochaaiSelectImage.setOnClickListener {
            binding.settingSearchTV.text = "bochaai"
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveSearchServiceTypeData("bochaai")
            }
            bochaaiSelectImage.visibility = View.GONE
            bochaaiSelectedImage.visibility = View.VISIBLE

            search1apiSelectImage.visibility = View.VISIBLE
            search1apiSelectedImage.visibility = View.GONE
            tavilySelectImage.visibility = View.VISIBLE
            tavilySelectedImage.visibility = View.GONE
            exaSelectImage.visibility = View.VISIBLE
            exaSelectedImage.visibility = View.GONE

        }*/

        bochaaiConst.setOnClickListener {
            if (bochaaiSelectImage.visibility == View.VISIBLE){
                binding.settingSearchTV.text = "bochaai"
                searchService = "bochaai"
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveSearchServiceTypeData("bochaai")
                }
                bochaaiSelectImage.visibility = View.GONE
                bochaaiSelectedImage.visibility = View.VISIBLE

                search1apiSelectImage.visibility = View.VISIBLE
                search1apiSelectedImage.visibility = View.GONE
                tavilySelectImage.visibility = View.VISIBLE
                tavilySelectedImage.visibility = View.GONE
                exaSelectImage.visibility = View.VISIBLE
                exaSelectedImage.visibility = View.GONE
            }
        }

        // 获取布局中的 Button，并设置点击事件
//        val btnClose = view.findViewById<ImageView>(R.id.btnMoreClose)
//        btnClose.setOnClickListener {
//            // 关闭 BottomSheetDialog
//            ViewAnimationUtils.performClickEffect(it)
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


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

}