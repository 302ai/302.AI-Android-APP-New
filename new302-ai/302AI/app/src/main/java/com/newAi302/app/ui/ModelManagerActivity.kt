package com.newAi302.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
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
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.ViewAnimationUtils
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
    // 修改后（线程安全）
    //private var options2 = CopyOnWriteArrayList<String>()
    private lateinit var dataStoreManager: DataStoreManager
    private var targetIndex = 0
    private var targetCustomizeIndex = 0
    private lateinit var chatDatabase: ChatDatabase
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
                    options3.removeAt(position)
                    //adapter302Ai.notifyItemRemoved(position)
                    adapter302Ai.notifyDataSetChanged()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveModelList(options3)
                    }
                }else if (data == "Delete1"){
                    options3.removeAt(position)
                    adapter302Ai.notifyItemRemoved(position)
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveModelList(options3)
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

                val job2 = lifecycleScope.launch(Dispatchers.IO) {
                    targetIndex = options3.indexOfFirst { mModelType ->
                        // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                        mModelType.contains(s.toString())
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    job2.join() // 等待数据库操作完成
                    if (targetIndex != 0){
                        binding.ai302Recycle.layoutManager?.scrollToPosition(targetIndex)
                        // 3. 刷新目标item（无需获取ViewHolder）
                        adapter302Ai.notifyItemChanged(targetIndex)
                    }

                }

                val job3 = lifecycleScope.launch(Dispatchers.IO) {
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

                }

            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("ceshi","afterTextChanged搜索文字：${s.toString()}")



                if (s?.isEmpty() == true) {

                }

            }
        })

        binding.searchCloseBtn.setOnClickListener {
            // 清空输入框
            binding.editSearchSetting.text?.clear()
            binding.searchCloseBtn.visibility = View.GONE
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


}