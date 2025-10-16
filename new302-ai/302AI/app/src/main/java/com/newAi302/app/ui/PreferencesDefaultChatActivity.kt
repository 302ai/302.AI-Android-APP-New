package com.newAi302.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.newAi302.app.R
import com.newAi302.app.adapter.ModelType302aiAdapter
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.databinding.ActivityMainBinding
import com.newAi302.app.databinding.ActivityPreferencesDefaultChatBinding
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.ViewAnimationUtils
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MyApplication
import com.newAi302.app.adapter.ModelTypeSelectAdapter
import com.newAi302.app.adapter.ModelTypeSelectDefaultChatAdapter
import com.newAi302.app.adapter.ModelTypeSelectDefaultTitleAdapter
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.SelectModelData
import com.newAi302.app.utils.CommonDialogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll

class PreferencesDefaultChatActivity : BaseActivity() , OnItemClickListener {
    private lateinit var binding: ActivityPreferencesDefaultChatBinding
    private lateinit var adapterUser: ModelType302aiAdapter
    private lateinit var adapter302Ai: ModelType302aiAdapter
    private var isSettingChat = true
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var defaultChatAdapter: ModelTypeSelectDefaultTitleAdapter
    private lateinit var defaultTitleAdapter: ModelTypeSelectDefaultChatAdapter
    private lateinit var defaultChatCustomizeAdapter: ModelTypeSelectDefaultTitleAdapter
    private lateinit var defaultTitleCustomizeAdapter: ModelTypeSelectDefaultChatAdapter
    private lateinit var options3:MutableList<String>
    private lateinit var options2:MutableList<String>
    private var targetIndex = 0
    private var modelType = ""
    private var buildTitleModelType = ""
    private var targetChatIndex = 0
    private var targetTitleIndex = 0
    private var lastModeType = "gemini-2.5-flash-nothink"
    private var lastSelectClick = false
    private lateinit var chatDatabase: ChatDatabase
    private var defaultChatModelType = ""
    private var defaultBuildTitleModelType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreferencesDefaultChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        initData()
        initView()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = dataStoreManager.readUserEmailData.first()?:""
            var userConfigurationRoom = chatDatabase.chatDao().getUserConfigByUserId(userId)
            userConfigurationRoom?.defaultChatModelType = defaultChatModelType
            userConfigurationRoom?.defaultBuildTitleModelType = defaultBuildTitleModelType

//            userConfigurationRoom?.let {
//                chatDatabase.chatDao().insertUserConfig(
//                    UserConfigurationRoom(0,userConfigurationRoom.userId,userConfigurationRoom.systemLanguage,"light",readUseTracelessSwitch,
//                        readSlideBottomSwitch,readAppEmojisData,readSearchServiceType,readModelType,readBuildTitleModelType,modelList,readBuildTitleTime)
//                )
//            }



        }
    }

    private fun initData(){
        val settingType = intent.getStringExtra("setting_type")
        Log.e("ceshi","返回的设置类型$settingType")
        if (settingType != null) {
            if (settingType == "chatModel"){
                binding.settingTypeTv.text = getString(this,R.string.setting_preferences_default_chat_model_message)
                isSettingChat = true
                binding.lastTimeModelTv.text = getString(this,R.string.setting_default_model_last_time_message)
            }else{
                binding.settingTypeTv.text = getString(this,R.string.setting_preferences_default_title_model_message)
                binding.lastTimeModelTv.text = getString(this,R.string.setting_default_model_chat_same_title_message)
                isSettingChat = false
            }
        }
        val job = lifecycleScope.launch(Dispatchers.IO) {
            options3 = dataStoreManager.modelListFlow.first()
            options2 = dataStoreManager.customizeModelListFlow.first()
            Log.e("ceshi","获取模型列表$options3")
            val readModelType = dataStoreManager.readModelType.first()?:"gemini-2.5-flash-nothink"
            val readModelLastType = dataStoreManager.readLastModelType.first()?:"gpt-4o"
            readModelType.let {
                modelType = it
            }
            readModelLastType.let {
                lastModeType = it
            }
            val readBuildTitleModelType = dataStoreManager.readBuildTitleModelType.first()?:"gpt-4o"
            readBuildTitleModelType.let {
                buildTitleModelType = it
            }
            if (isSettingChat){
                targetChatIndex = options3.indexOfFirst { mModelType ->
                    // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                    modelType.equals(mModelType)
                }
            }else{
                targetTitleIndex = options3.indexOfFirst { mModelType ->
                    // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                    buildTitleModelType.equals(mModelType)
                }
            }

        }
        val options = mutableListOf("gemini-2.5-flash-nothink","gpt-4o","gpt4")
        adapter302Ai = ModelType302aiAdapter(this,options,this,true)

        val options1 = mutableListOf("gemini-2.5-flash-nothink","gpt-4o","gpt4")
        adapterUser = ModelType302aiAdapter(this,options1,this,true)

        lifecycleScope.launch(Dispatchers.Main) {
            job.join()
            defaultChatAdapter = ModelTypeSelectDefaultTitleAdapter(this@PreferencesDefaultChatActivity,options3,false){ position, data ->
                // 这里处理点击事件（Lambda的具体实现）
                Log.e("ceshi","点击模型$data")
                targetIndex = position
                setFirstSelect(false)
                lifecycleScope.launch(Dispatchers.Main) {
                    defaultChatCustomizeAdapter.selectModelPosition = -1
                    defaultChatCustomizeAdapter.notifyDataSetChanged()
                }

                defaultChatModelType = data

                lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveModelType(data)
                }
            }
            defaultTitleAdapter = ModelTypeSelectDefaultChatAdapter(this@PreferencesDefaultChatActivity,options3,false){ position, data ->
                // 这里处理点击事件（Lambda的具体实现）
                Log.e("ceshi","1点击模型$data")
                targetIndex = position
                setFirstSelect(false)
                lifecycleScope.launch(Dispatchers.Main) {
                    defaultTitleCustomizeAdapter.selectModelPosition = -1
                    defaultTitleCustomizeAdapter.notifyDataSetChanged()
                }

                defaultBuildTitleModelType = data

                lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveBuildTitleModeTypeData(data)
                }
            }

            defaultChatCustomizeAdapter = ModelTypeSelectDefaultTitleAdapter(this@PreferencesDefaultChatActivity,options2,false){ position, data ->
                // 这里处理点击事件（Lambda的具体实现）
                Log.e("ceshi","点击模型$data")
                //targetIndex = position
                setFirstSelect(false)
                lifecycleScope.launch(Dispatchers.Main) {
                    defaultChatAdapter.selectModelPosition = -1
                    defaultChatAdapter.notifyDataSetChanged()
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    //dataStoreManager.saveModelType(data)
                }
            }
            defaultTitleCustomizeAdapter = ModelTypeSelectDefaultChatAdapter(this@PreferencesDefaultChatActivity,options2,false){ position, data ->
                // 这里处理点击事件（Lambda的具体实现）
                Log.e("ceshi","1点击模型$data")
                //targetIndex = position
                setFirstSelect(false)
                lifecycleScope.launch(Dispatchers.Main) {
                    defaultTitleAdapter.selectModelPosition = -1
                    defaultTitleAdapter.notifyDataSetChanged()
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    //dataStoreManager.saveBuildTitleModeTypeData(data)
                }
            }

            if (isSettingChat){
                binding.ai302Recycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
                binding.ai302Recycle.adapter = defaultChatAdapter

                binding.aiUserRecycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
                binding.aiUserRecycle.adapter = defaultChatCustomizeAdapter
            }else{
                binding.ai302Recycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
                binding.ai302Recycle.adapter = defaultTitleAdapter

                binding.aiUserRecycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
                binding.aiUserRecycle.adapter = defaultTitleCustomizeAdapter
            }

            if (isSettingChat){
                if (targetChatIndex >= 0){
                    binding.ai302Recycle.layoutManager?.scrollToPosition(targetChatIndex)
                    defaultChatAdapter.selectModelPosition = targetChatIndex
                    // 3. 刷新目标item（无需获取ViewHolder）
                    defaultChatAdapter.notifyItemChanged(targetChatIndex)
                }else{
                    setFirstSelect(true)
                }
            }else{
                //Log.e("ceshi","位置是：$targetTitleIndex")
                if (targetTitleIndex >= 0){
                    binding.ai302Recycle.layoutManager?.scrollToPosition(targetTitleIndex)
                    defaultTitleAdapter.selectModelPosition = targetTitleIndex
                    // 3. 刷新目标item（无需获取ViewHolder）
                    defaultTitleAdapter.notifyItemChanged(targetTitleIndex)
                }else{
                    setFirstSelect(true)
                }
            }
        }




    }

    private fun initView(){
        // 可以在这里进行 RecyclerView 的设置等操作
        /*binding.ai302Recycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
        binding.ai302Recycle.adapter = adapter302Ai*/

        binding.aiUserRecycle.layoutManager = LinearLayoutManager(this@PreferencesDefaultChatActivity)
        binding.aiUserRecycle.adapter = adapterUser

        binding.cons1.setOnClickListener {
            if (!lastSelectClick){
                lastSelectClick = true
                binding.selectImage.visibility = View.GONE
                binding.selectedImage.visibility = View.VISIBLE

                if (isSettingChat){
                    defaultChatAdapter.selectModelPosition = -1
                    defaultChatAdapter.notifyDataSetChanged()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveModelType(lastModeType)
                    }
                    defaultChatModelType = lastModeType
                }else{
                    defaultTitleAdapter.selectModelPosition = -1
                    defaultTitleAdapter.notifyDataSetChanged()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveBuildTitleModeTypeData(lastModeType)
                    }
                    defaultBuildTitleModelType = lastModeType
                }
            }else{
                lastSelectClick = false
                binding.selectedImage.visibility = View.GONE
                binding.selectImage.visibility = View.VISIBLE
            }
        }

        /*binding.selectImage.setOnClickListener {
            binding.selectImage.visibility = View.GONE
            binding.selectedImage.visibility = View.VISIBLE

            if (isSettingChat){
                defaultChatAdapter.selectModelPosition = -1
                defaultChatAdapter.notifyDataSetChanged()
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveModelType(lastModeType)
                }
            }else{
                defaultTitleAdapter.selectModelPosition = -1
                defaultTitleAdapter.notifyDataSetChanged()
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveBuildTitleModeTypeData(lastModeType)
                }
            }
        }
        binding.selectedImage.setOnClickListener {
            binding.selectedImage.visibility = View.GONE
            binding.selectImage.visibility = View.VISIBLE
        }*/
        binding.backImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            finish()
        }
    }

    override fun onItemClick(chatItem: ChatItemRoom) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClick(selectList: MutableList<Int>) {
        TODO("Not yet implemented")
    }

    override fun onBackFunctionClick(chatFunction: ChatBackMessage) {
        TODO("Not yet implemented")
    }

    private fun setFirstSelect(isSelected:Boolean){
        if (isSelected){
            binding.selectImage.visibility = View.GONE
            binding.selectedImage.visibility = View.VISIBLE
        }else{
            binding.selectImage.visibility = View.VISIBLE
            binding.selectedImage.visibility = View.GONE
        }

    }

}