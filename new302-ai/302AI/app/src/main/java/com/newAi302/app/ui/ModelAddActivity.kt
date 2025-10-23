package com.newAi302.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityModelAddBinding
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ModelDataRoom
import com.newAi302.app.utils.ViewAnimationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class ModelAddActivity : BaseActivity() {
    private lateinit var binding: ActivityModelAddBinding
    private var isShowAdvancedSetting = false
    private var modelType = ""
    private var modelSearchList = mutableListOf<String>()

    private lateinit var chatDatabase: ChatDatabase
    private lateinit var dataStoreManager: DataStoreManager

    private var remark = ""
    private var reasoning = false
    private var imageUnderstanding = false
    private var baseUrl = "https://api.302.ai/"
    private var apiKey = ""
    private var isActionAdd = false
    private var modelList = mutableListOf<String>()
    private var isSelectReason = false
    private var isSelectPicture = false
    private var isSettingDownImage = false
    // 修改后（线程安全）
    //private var modelList = CopyOnWriteArrayList<String>()
    private var curModelType = ""
    private var isCustomize = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityModelAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        initData()
        initView()
    }

    private fun initData(){
        val mModelType = intent.getSerializableExtra("model_type") as? String
        if (mModelType != null){
            modelType = mModelType
            curModelType = mModelType
            binding.editIdSetting.setText(modelType)
            lifecycleScope.launch(Dispatchers.IO) {
                modelList = dataStoreManager.modelListFlow.first()
                Log.e("ceshi","01获取自定义模型列表$modelList")
                val modelData = chatDatabase.chatDao().getModelById(modelType)
                Log.e("ceshi","模型获取数据库:${modelData}")
                modelData?.let {
                    remark = it.remark
                    reasoning = it.reasoning
                    imageUnderstanding = it.imageUnderstanding
                    baseUrl = it.baseUrl
                    apiKey = it.apiKey
                    isCustomize = it.isCustomize

                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.editRemarkSetting.setText(it.remark)
                        if (reasoning){
                            binding.selectedImage.visibility = View.VISIBLE
                            binding.selectImage.visibility = View.GONE
                        }else{
                            binding.selectedImage.visibility = View.GONE
                            binding.selectImage.visibility = View.VISIBLE
                        }
                        if (imageUnderstanding){
                            binding.selectPictureImage.visibility = View.GONE
                            binding.selectedPictureImage.visibility = View.VISIBLE
                        }else{
                            binding.selectPictureImage.visibility = View.VISIBLE
                            binding.selectedPictureImage.visibility = View.GONE
                        }
                        binding.editUrlSetting.setText(baseUrl)
                        binding.editKeySetting1.setText(apiKey)
                    }
                }




            }
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                val readData = dataStoreManager.readData.first()?:""
                readData.let {
                    apiKey = it
                }


            }
            baseUrl = ""

        }
        val actionType = intent.getSerializableExtra("action_type") as? String
        if (actionType != null){
            if (actionType == "ADD"){
                apiKey = ""
                baseUrl = ""
                isActionAdd = true
                val job = lifecycleScope.launch(Dispatchers.IO) {
                    //modelList = dataStoreManager.customizeModelListFlow.first()
                    modelList = dataStoreManager.modelListFlow.first()
                    Log.e("ceshi","0获取自定义模型列表$modelList")
                }
            }
        }

    }

    private fun initView(){
        binding.cons2.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (!isSettingDownImage){
                isSettingDownImage = true
                binding.settingDownImage.visibility = View.GONE
                binding.settingUpImage.visibility = View.VISIBLE
                binding.advancedSettingLine.visibility = View.VISIBLE
            }else{
                isSettingDownImage = false
                binding.settingUpImage.visibility = View.GONE
                binding.settingDownImage.visibility = View.VISIBLE
                binding.advancedSettingLine.visibility = View.GONE
            }

        }

        /*binding.settingUpImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            binding.settingUpImage.visibility = View.GONE
            binding.settingDownImage.visibility = View.VISIBLE
            binding.advancedSettingLine.visibility = View.GONE
        }*/

        binding.backImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            finish()
        }

        binding.reasonConst.setOnClickListener {
            if (!isSelectReason){
                isSelectReason = true
                binding.selectImage.visibility = View.GONE
                binding.selectedImage.visibility = View.VISIBLE
                reasoning = true
            }else{
                isSelectReason = false
                binding.selectedImage.visibility = View.GONE
                binding.selectImage.visibility = View.VISIBLE
                reasoning = false
            }
        }
        /*binding.selectImage.setOnClickListener {
            binding.selectImage.visibility = View.GONE
            binding.selectedImage.visibility = View.VISIBLE
            reasoning = true
        }
        binding.selectedImage.setOnClickListener {
            binding.selectedImage.visibility = View.GONE
            binding.selectImage.visibility = View.VISIBLE
            reasoning = false
        }*/

        binding.understandingPictureConst.setOnClickListener {
            if (!isSelectPicture){
                isSelectPicture = true
                binding.selectPictureImage.visibility = View.GONE
                binding.selectedPictureImage.visibility = View.VISIBLE
                imageUnderstanding = true
            }else{
                isSelectPicture = false
                binding.selectedPictureImage.visibility = View.GONE
                binding.selectPictureImage.visibility = View.VISIBLE
                imageUnderstanding = false
            }
        }

        /*binding.selectPictureImage.setOnClickListener {
            binding.selectPictureImage.visibility = View.GONE
            binding.selectedPictureImage.visibility = View.VISIBLE
            imageUnderstanding = true
        }
        binding.selectedPictureImage.setOnClickListener {
            binding.selectedPictureImage.visibility = View.GONE
            binding.selectPictureImage.visibility = View.VISIBLE
            imageUnderstanding = false
        }*/

        binding.saveModeTypeBt.setOnClickListener {

            if (modelType == ""){
                Toast.makeText(this, ContextCompat.getString(this@ModelAddActivity, R.string.setting_add_model_no_id_message), Toast.LENGTH_SHORT).show()
            }else{
                lifecycleScope.launch(Dispatchers.IO) {

                    if (isActionAdd){
                        val model = ModelDataRoom(
                            modelId = modelType,
                            remark = remark,
                            reasoning = reasoning,
                            imageUnderstanding = imageUnderstanding,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            isCustomize = true
                        )
                        chatDatabase.chatDao().insertModel(model)
                        modelList.add(modelType)
                        //dataStoreManager.saveCustomizeModelList(modelList)
                        dataStoreManager.saveModelList(modelList)
                    }else{
                        val model = ModelDataRoom(
                            modelId = modelType,
                            remark = remark,
                            reasoning = reasoning,
                            imageUnderstanding = imageUnderstanding,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            isCustomize = isCustomize
                        )
                        chatDatabase.chatDao().insertModel(model)
//                        modelList.remove(curModelType)
//                        modelList.add(modelType)
                        replaceModelType(curModelType,modelType)
                        //dataStoreManager.saveCustomizeModelList(modelList)
                        dataStoreManager.saveModelList(modelList)
                    }
                    /*if (isActionAdd){
                        lifecycleScope.launch(Dispatchers.IO) {
                            modelList.add(modelType)
                            //dataStoreManager.saveCustomizeModelList(modelList)
                            dataStoreManager.saveModelList(modelList)
                        }
                    }*/

                    lifecycleScope.launch(Dispatchers.Main) {
                        finish()
                    }
                }
            }


        }//


        binding.editIdSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                modelType = s.toString()
            }
        })

        binding.editRemarkSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                remark = s.toString()
            }
        })

        binding.editUrlSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                baseUrl = s.toString()
            }
        })

        binding.editKeySetting1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                apiKey = s.toString()
            }
        })



    }

    /**
     * 在 modelList 中找到 curModelType 的位置，并用 modelType 替换该位置的元素
     * @param curModelType 要查找的目标元素
     * @param modelType 要替换成的新元素
     * @return 是否替换成功（true：找到并替换；false：未找到 curModelType）
     */
    fun replaceModelType(curModelType: String, modelType: String): Boolean {
        // 1. 查找 curModelType 在列表中第一次出现的位置（索引）
        val index = modelList.indexOf(curModelType)

        // 2. 检查索引是否有效（存在该元素）
        if (index != -1) {
            // 3. 替换该位置的元素为 modelType
            modelList[index] = modelType
            return true
        }
        // 未找到目标元素，返回 false
        return false
    }

}