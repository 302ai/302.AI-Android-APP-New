package com.newAi302.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.adapter.HomeMessageAdapter
import com.newAi302.app.adapter.ModelType302aiAdapter
import com.newAi302.app.adapter.ModelTypeManager302aiAdapter
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.databinding.ActivityAnnouncementBinding
import com.newAi302.app.databinding.ActivityModelManagerBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.http.ApiService
import com.newAi302.app.http.NetworkFactory
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.viewModel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll
import java.util.concurrent.CopyOnWriteArrayList

class ModelManagerActivity : BaseActivity(), OnItemClickListener {
    private lateinit var binding: ActivityModelManagerBinding
    private lateinit var adapterUser: ModelTypeManager302aiAdapter
    private lateinit var adapter302Ai: ModelTypeManager302aiAdapter


    private lateinit var options3:MutableList<String>
    private lateinit var options2:MutableList<String>

    private var modelSearchList:MutableList<String> = mutableListOf()
    // 修改后（线程安全）
    //private var options2 = CopyOnWriteArrayList<String>()
    private lateinit var dataStoreManager: DataStoreManager
    private var targetIndex = 0
    private var targetCustomizeIndex = 0
    private lateinit var chatDatabase: ChatDatabase

    private val chatViewModel: ChatViewModel by viewModels()
    private val BASE_URL = "https://api.302.ai/"
    private val BASE_URL1 = "https://gptutils-chat.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityModelManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        binding.backImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            finish()
        }
        initData()
        initView()
    }

    override fun onRestart() {
        super.onRestart()
        initData()
    }

    private fun initData(){
        chatViewModel.modelListResult.observe(this){
            Log.e("ceshi","模型列表回复：$it")

            it?.let {

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
                        options3 = CopyOnWriteArrayList(mModelList)
                    }else{
                        options3 = it
                        if (it.isNotEmpty()){
                            options3.add("gemini-2.5-flash-nothink")
                        }
                    }
                    Log.e("ceshi","这里的数据库模型列表:${options3}")
                    removeDuplicates(options3)

                    dataStoreManager.saveModelList(options3)

                    lifecycleScope.launch(Dispatchers.Main) {
                        //adapter302Ai.notifyDataSetChanged()
                        adapter302Ai.updateData(options3)
                    }

                }
            }
        }


        val job = lifecycleScope.launch(Dispatchers.IO) {
            options3 = dataStoreManager.modelListFlow.first()
            Log.e("ceshi","获取模型列表$options3")
            options2 = dataStoreManager.customizeModelListFlow.first()
            Log.e("ceshi","获取自定义模型列表$options2")
            /*if (options2.isNotEmpty()){
                binding.text2.visibility = View.VISIBLE
                binding.aiUserRecycle.visibility = View.VISIBLE
            }else{
                binding.text2.visibility = View.GONE
                binding.aiUserRecycle.visibility = View.GONE
            }*/
        }


        lifecycleScope.launch(Dispatchers.Main) {
            job.join()
            val options = mutableListOf("gemini-2.5-flash-nothink","gpt-4o","gpt4")
            adapter302Ai = ModelTypeManager302aiAdapter(this@ModelManagerActivity,options3,chatDatabase,lifecycleScope){ position, data ->
                if (data == "Delete"){
                    val modelId = options3[position]
                    if (options3.size>1){
                        options3.removeAt(position)
                        //adapter302Ai.notifyItemRemoved(position)
                        adapter302Ai.updateData(options3)
                        adapter302Ai.notifyDataSetChanged()
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatDatabase.chatDao().deleteModelById(modelId)
                            dataStoreManager.saveModelList(options3)

                            if (modelId == dataStoreManager.readModelType.first()){
                                val readChatDefaultModelType = options3[0]
                                dataStoreManager.saveModelType(readChatDefaultModelType)
                            }
                            if (modelId == dataStoreManager.readBuildTitleModelType.first()){
                                val readBuildTitleModelType = options3[0]
                                dataStoreManager.saveBuildTitleModeTypeData(readBuildTitleModelType)
                            }

                        }
                    }else{
                        ToastUtils.showLong(resources.getString(R.string.setting_model_manager_delete_fail_toast_message))
                    }

                }else if (data == "Delete1"){
                    val modelId = options3[position]
                    if (options3.size>1){
                        options3.removeAt(position)
                        adapter302Ai.updateData(options3)
                        adapter302Ai.notifyItemRemoved(position)
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatDatabase.chatDao().deleteModelById(modelId)
                            dataStoreManager.saveModelList(options3)

                            if (modelId == dataStoreManager.readModelType.first()){
                                val readChatDefaultModelType = options3[0]
                                dataStoreManager.saveModelType(readChatDefaultModelType)
                            }
                            if (modelId == dataStoreManager.readBuildTitleModelType.first()){
                                val readBuildTitleModelType = options3[0]
                                dataStoreManager.saveBuildTitleModeTypeData(readBuildTitleModelType)
                            }
                        }
                    }else{
                        ToastUtils.showLong(resources.getString(R.string.setting_model_manager_delete_fail_toast_message))
                    }
                }else{
                    val intent = Intent(this@ModelManagerActivity, ModelAddActivity::class.java)
                    intent.putExtra("model_type", data)
                    startActivity(intent)
                }
            }

            val options1 = mutableListOf("gemini-2.5-flash-nothink","gpt-4o","gpt4")
            adapterUser = ModelTypeManager302aiAdapter(this@ModelManagerActivity,options2,chatDatabase,lifecycleScope){ position, data ->
                if (data == "Delete"){
                    Log.e("ceshi","删除后的$position,,$data")
                    options2.removeAt(position)
                    adapterUser.notifyItemRemoved(position)
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.deleteFromCustomizeModelList(options2[position])
                        Log.e("ceshi","删除后的$options2")
                    }

                }else{
                    val intent = Intent(this@ModelManagerActivity, ModelAddActivity::class.java)
                    intent.putExtra("model_type", data)
                    startActivity(intent)
                }
            }

            // 可以在这里进行 RecyclerView 的设置等操作
            binding.ai302Recycle.layoutManager = LinearLayoutManager(this@ModelManagerActivity)
            binding.ai302Recycle.adapter = adapter302Ai

            binding.aiUserRecycle.layoutManager = LinearLayoutManager(this@ModelManagerActivity)
            binding.aiUserRecycle.adapter = adapterUser

        }




    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initView(){


        binding.addModeTypeBt.setOnClickListener {
            val intent = Intent(this, ModelAddActivity::class.java)
            intent.putExtra("action_type", "ADD")
            startActivity(intent)
        }

        binding.editSearchSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                Log.e("ceshi","0搜索文字：${start},$before,$count")
                binding.searchCloseBtn.visibility = View.VISIBLE

                // 1. 预处理搜索关键词：判空、去首尾空格、转为小写（忽略大小写）
                val keyword = s?.toString()?.trim()?.lowercase() ?: ""
                //modelSearchList.clear()
                if (modelSearchList.isNotEmpty()){
                    modelSearchList.clear()
                }
                val job2 = lifecycleScope.launch(Dispatchers.IO) {
                    /*targetIndex = options3.indexOfFirst { mModelType ->
                        // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                        mModelType.contains(s.toString())
                    }*/
                    for (modelType in options3){
                        if (modelType.contains(s.toString(),ignoreCase = true)){
                            modelSearchList.add(modelType)
                        }
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    job2.join() // 等待数据库操作完成
                    /*if (targetIndex != 0){
                        binding.ai302Recycle.layoutManager?.scrollToPosition(targetIndex)
                        // 3. 刷新目标item（无需获取ViewHolder）
                        adapter302Ai.notifyItemChanged(targetIndex)
                    }*/
                    adapter302Ai.updateData(modelSearchList)

                }

                /*val job3 = lifecycleScope.launch(Dispatchers.IO) {
                    targetCustomizeIndex = options2.indexOfFirst { mModelType ->
                        // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                        mModelType.contains(s.toString())
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    job3.join() // 等待数据库操作完成
                    if (targetCustomizeIndex != 0){
                        binding.aiUserRecycle.layoutManager?.scrollToPosition(targetCustomizeIndex)
                        // 3. 刷新目标item（无需获取ViewHolder）
                        adapter302Ai.notifyItemChanged(targetCustomizeIndex)
                    }

                }*/

            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("ceshi","afterTextChanged搜索文字：${s.toString()}")

                adapter302Ai.updateData(options3)



                if (s?.isEmpty() == true) {
                    binding.searchCloseBtn.visibility = View.GONE
                }

            }
        })

        binding.searchCloseBtn.setOnClickListener {
            // 清空输入框
            binding.editSearchSetting.text?.clear()
            binding.searchCloseBtn.visibility = View.GONE
        }

        binding.refreshLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            lifecycleScope.launch(Dispatchers.IO) {
                val data = dataStoreManager.readData.first()
                data?.let {
                    Log.e("ceshi", "appKey是多少：$it")
                    chatViewModel.get302AiModelList(it, apiService)
                }
            }
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

    // 假设 modelList 是 CopyOnWriteArrayList<String> 类型
    fun removeDuplicates(modelList: MutableList<String>) {
        // 1. 将列表转换为 LinkedHashSet（去重且保留顺序）
        val uniqueSet = LinkedHashSet(modelList)
        // 2. 清空原列表
        modelList.clear()
        // 3. 将去重后的元素添加回原列表（或创建新的 CopyOnWriteArrayList）
        modelList.addAll(uniqueSet)
    }


}