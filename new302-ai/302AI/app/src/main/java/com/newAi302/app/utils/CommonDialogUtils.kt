package com.newAi302.app.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.R
import com.newAi302.app.adapter.ModelType302aiAdapter
import com.newAi302.app.adapter.ModelTypeSelectAdapter
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.datastore.DataStoreManager
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.infa.OnItemSelectModelClickListener
import com.newAi302.app.infa.OnPromptSelectedListener
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.room.SelectModelData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newAi302.app.adapter.EmojiAdapter
import com.newAi302.app.adapter.ModelTypeSelectChatAdapter
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CommonDialogUtils {

    private lateinit var adapterSelectModel: ModelTypeSelectChatAdapter


    @SuppressLint("MissingInflatedId")
    fun showBottomSheetChooseTextDialog(context: Context,text:String) {
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_choose_text_layout, null)
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

        var chooseText = view.findViewById<TextView>(R.id.chooseTextTv)
        chooseText.text = text


        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun showBottomSheetPromptDialog(context: Context, dataStoreManager: DataStoreManager,lifecycleScope: LifecycleCoroutineScope,mPrompt:String,listener: OnPromptSelectedListener){
        var isNoPromptSelect = false
        var isOnePromptSelect = false
        var isTwoPromptSelect = false
        var isUserPromptSelect = false
        val bottomSheetDialog = BottomSheetDialog(context)
        // 设置弹窗内容布局
        val dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_prompt_dialog, null)
        bottomSheetDialog.setContentView(dialogView)


        val cueWordHelpImage = dialogView.findViewById<ImageView>(R.id.cueWordHelpImage)

        val noPromptSelectImage = dialogView.findViewById<ImageView>(R.id.noPromptSelectImage)
        val noPromptSelectedImage = dialogView.findViewById<ImageView>(R.id.noPromptSelectedImage)

        val onePromptSelectImage = dialogView.findViewById<ImageView>(R.id.onePromptSelectImage)
        val onePromptSelectedImage = dialogView.findViewById<ImageView>(R.id.onePromptSelectedImage)

        val twoPromptSelectImage = dialogView.findViewById<ImageView>(R.id.twoPromptSelectImage)
        val twoPromptSelectedImage = dialogView.findViewById<ImageView>(R.id.twoPromptSelectedImage)

        val userPromptSelectImage = dialogView.findViewById<ImageView>(R.id.userPromptSelectImage)
        val userPromptSelectedImage = dialogView.findViewById<ImageView>(R.id.userPromptSelectedImage)

        val etCueWords = dialogView.findViewById<EditText>(R.id.etCueWords)

        val userPromptLine = dialogView.findViewById<LinearLayout>(R.id.line3)

        val noPromptSelectConst = dialogView.findViewById<ConstraintLayout>(R.id.noPromptSelectConst)
        val onePromptSelectConst = dialogView.findViewById<ConstraintLayout>(R.id.onePromptSelectConst)
        val twoPromptSelectConst = dialogView.findViewById<ConstraintLayout>(R.id.twoPromptSelectConst)
        val userPromptSelectConst = dialogView.findViewById<ConstraintLayout>(R.id.userPromptSelectConst)

        var prompt = mPrompt

        cueWordHelpImage.setOnClickListener {
            showHelpDialog(context)
        }


        when(mPrompt){
            "这是删除过的内容变为空白" -> {
                noPromptSelectImage.visibility = View.GONE
                noPromptSelectedImage.visibility = View.VISIBLE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE

                userPromptLine.visibility = View.GONE
            }
            "Chatgpt" -> {
                onePromptSelectImage.visibility = View.GONE
                onePromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE

                userPromptLine.visibility = View.GONE
            }
            "Claude" -> {
                twoPromptSelectImage.visibility = View.GONE
                twoPromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE

                userPromptLine.visibility = View.GONE
            }
            else -> {
                userPromptSelectImage.visibility = View.GONE
                userPromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE
                etCueWords.setText(mPrompt)
                userPromptLine.visibility = View.VISIBLE
            }

        }



        // 单选组设置默认选中（这里模拟“自定义”选中，可根据实际数据调整）
        //dialogView.rb_custom.isChecked = true
        dialogView.findViewById<LinearLayout>(R.id.promptDialogBackImage).setOnClickListener {
            // 当Dialog关闭时，通过回调返回结果
            listener.onPromptSelected(prompt)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnDismissListener {
            // 当Dialog关闭时，通过回调返回结果
            listener.onPromptSelected(prompt)
        }

        /*noPromptSelectImage.setOnClickListener {
            userPromptLine.visibility = View.GONE

            noPromptSelectImage.visibility = View.GONE
            noPromptSelectedImage.visibility = View.VISIBLE

            onePromptSelectImage.visibility = View.VISIBLE
            onePromptSelectedImage.visibility = View.GONE

            twoPromptSelectImage.visibility = View.VISIBLE
            twoPromptSelectedImage.visibility = View.GONE

            userPromptSelectImage.visibility = View.VISIBLE
            userPromptSelectedImage.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("这是删除过的内容变为空白")
            }
            prompt = "这是删除过的内容变为空白"
        }
        noPromptSelectedImage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("这是删除过的内容变为空白")
            }
            prompt = "这是删除过的内容变为空白"
        }*/

        noPromptSelectConst.setOnClickListener {
            if (!isNoPromptSelect){
                isNoPromptSelect = true
                isOnePromptSelect = false
                isTwoPromptSelect = false
                isUserPromptSelect = false
                userPromptLine.visibility = View.GONE

                noPromptSelectImage.visibility = View.GONE
                noPromptSelectedImage.visibility = View.VISIBLE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveCueWords("这是删除过的内容变为空白")
                }
                prompt = "这是删除过的内容变为空白"
            }
        }

        onePromptSelectConst.setOnClickListener {
            if (!isOnePromptSelect){
                isOnePromptSelect = true
                isNoPromptSelect = false
                isTwoPromptSelect = false
                isUserPromptSelect = false
                userPromptLine.visibility = View.GONE

                onePromptSelectImage.visibility = View.GONE
                onePromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveCueWords("Chatgpt")
                }
                prompt = "Chatgpt"
            }
        }

        /*onePromptSelectImage.setOnClickListener {
            userPromptLine.visibility = View.GONE

            onePromptSelectImage.visibility = View.GONE
            onePromptSelectedImage.visibility = View.VISIBLE

            noPromptSelectImage.visibility = View.VISIBLE
            noPromptSelectedImage.visibility = View.GONE

            twoPromptSelectImage.visibility = View.VISIBLE
            twoPromptSelectedImage.visibility = View.GONE

            userPromptSelectImage.visibility = View.VISIBLE
            userPromptSelectedImage.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("Chatgpt")
            }
            prompt = "Chatgpt"
        }
        onePromptSelectedImage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("Chatgpt")
            }
            prompt = "Chatgpt"
        }*/

        /*twoPromptSelectImage.setOnClickListener {
            userPromptLine.visibility = View.GONE
            twoPromptSelectImage.visibility = View.GONE
            twoPromptSelectedImage.visibility = View.VISIBLE

            noPromptSelectImage.visibility = View.VISIBLE
            noPromptSelectedImage.visibility = View.GONE

            onePromptSelectImage.visibility = View.VISIBLE
            onePromptSelectedImage.visibility = View.GONE

            userPromptSelectImage.visibility = View.VISIBLE
            userPromptSelectedImage.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("Claude")
            }
            prompt = "Claude"
        }
        twoPromptSelectedImage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveCueWords("Claude")
            }
            prompt = "Claude"
        }*/
        twoPromptSelectConst.setOnClickListener {
            if (!isTwoPromptSelect){
                isTwoPromptSelect = true
                isOnePromptSelect = false
                isNoPromptSelect = false
                isUserPromptSelect = false
                userPromptLine.visibility = View.GONE
                twoPromptSelectImage.visibility = View.GONE
                twoPromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                userPromptSelectImage.visibility = View.VISIBLE
                userPromptSelectedImage.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    dataStoreManager.saveCueWords("Claude")
                }
                prompt = "Claude"
            }
        }

        /*userPromptSelectImage.setOnClickListener {
            userPromptLine.visibility = View.VISIBLE

            userPromptSelectImage.visibility = View.GONE
            userPromptSelectedImage.visibility = View.VISIBLE

            noPromptSelectImage.visibility = View.VISIBLE
            noPromptSelectedImage.visibility = View.GONE

            onePromptSelectImage.visibility = View.VISIBLE
            onePromptSelectedImage.visibility = View.GONE

            twoPromptSelectImage.visibility = View.VISIBLE
            twoPromptSelectedImage.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                if (etCueWords.text.toString() != ""){
                    dataStoreManager.saveCueWords(etCueWords.text.toString())

                }else{
                    dataStoreManager.saveCueWords("这是删除过的内容变为空白")

                }

            }
            if (etCueWords.text.toString() != ""){
                prompt = etCueWords.text.toString()
            }else{
                prompt = "这是删除过的内容变为空白"
            }
        }
        userPromptSelectedImage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (etCueWords.text.toString() != ""){
                    dataStoreManager.saveCueWords(etCueWords.text.toString())
                }else{
                    dataStoreManager.saveCueWords("这是删除过的内容变为空白")
                }

            }
            if (etCueWords.text.toString() != ""){
                prompt = etCueWords.text.toString()
            }else{
                prompt = "这是删除过的内容变为空白"
            }
        }*/

        userPromptSelectConst.setOnClickListener {
            if (!isUserPromptSelect){
                isUserPromptSelect = true
                isTwoPromptSelect = false
                isOnePromptSelect = false
                isNoPromptSelect = false
                userPromptLine.visibility = View.VISIBLE

                userPromptSelectImage.visibility = View.GONE
                userPromptSelectedImage.visibility = View.VISIBLE

                noPromptSelectImage.visibility = View.VISIBLE
                noPromptSelectedImage.visibility = View.GONE

                onePromptSelectImage.visibility = View.VISIBLE
                onePromptSelectedImage.visibility = View.GONE

                twoPromptSelectImage.visibility = View.VISIBLE
                twoPromptSelectedImage.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    if (etCueWords.text.toString() != ""){
                        dataStoreManager.saveCueWords(etCueWords.text.toString())

                    }else{
                        dataStoreManager.saveCueWords("这是删除过的内容变为空白")

                    }

                }
                if (etCueWords.text.toString() != ""){
                    prompt = etCueWords.text.toString()
                }else{
                    prompt = "这是删除过的内容变为空白"
                }
            }
        }

        etCueWords.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","自定义输入存储：${s}，，，$")
                    if (userPromptSelectedImage.visibility == View.VISIBLE){
                        dataStoreManager.saveCustomizeKeyData(s.toString())
                    }
                }
                if (userPromptSelectedImage.visibility == View.VISIBLE){
                    prompt = s.toString()
                }

            }
        })




        bottomSheetDialog.show()
        //Log.e("ceshi","0提示词：$prompt")
    }


    @SuppressLint("MissingInflatedId")
    fun showBottomSheetCodePreDialog(context: Context,codeStr:String,codeName:String) {
        var isCodePre = true
        val markwon = Markwon.builder(context)
            .usePlugin(CorePlugin.create()) // 核心解析插件
            .usePlugin(HtmlPlugin.create()) // 支持HTML标签
            .usePlugin(StrikethroughPlugin.create()) // 支持删除线
            .usePlugin(TaskListPlugin.create(context)) // 支持任务列表
            .usePlugin(TablePlugin.create(context)) // 支持表格
            .usePlugin(GlideImagesPlugin.create(context)) // 使用Glide加载图片（需添加Glide依赖）
            .build()
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_code_pre_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<LinearLayout>(R.id.codePreBackImage)
        btnClose.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            // 关闭 BottomSheetDialog
            bottomSheetDialog.dismiss()

        }

        var codePreTitleTV = view.findViewById<TextView>(R.id.codePreTitleTV)
        var codePreTv = view.findViewById<TextView>(R.id.codePreTv)
        var codePreWeb = view.findViewById<WebView>(R.id.codePreWeb)
        var eyeCodePreImage = view.findViewById<ImageView>(R.id.eyeCodePreImage)
        var copyCodePreImage = view.findViewById<ImageView>(R.id.copyCodePreImage)

        codePreTitleTV.setText(codeName)

        // 加载HTML内容到WebView
        codePreWeb.loadDataWithBaseURL(
            null,
            StringObjectUtils.extractHtmlFromMarkdownCode(codeStr),
            "text/html",
            "UTF-8",
            null
        )

        markwon.setMarkdown(codePreTv, StringObjectUtils.extractHtml(codeStr))

        eyeCodePreImage.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            if (isCodePre){
                isCodePre = false
                codePreTv.visibility = View.GONE
                codePreWeb.visibility = View.VISIBLE
            }else{
                isCodePre = true
                codePreTv.visibility = View.VISIBLE
                codePreWeb.visibility = View.GONE
            }
        }

        copyCodePreImage.setOnClickListener {
            SystemUtils.copyTextToClipboard(context,StringObjectUtils.extractHtml(codeStr))
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = ContextCompat.getString(context, R.string.copy_code_toast_message),
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }






        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    fun showBottomSheetSelectModelDialog(context: Context,options1: MutableList<String>,dataStoreManager: DataStoreManager,lifecycleScope: LifecycleCoroutineScope,modelType:String,listener: OnItemSelectModelClickListener) {
        adapterSelectModel = ModelTypeSelectChatAdapter(context,options1,false){ position, data ->
            // 这里处理点击事件（Lambda的具体实现）
            listener.onItemClick(SelectModelData(position,data))
            /*lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveModelType(data)
            }*/
        }

        val targetIndex = options1.indexOfFirst { mModelType ->
            // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
            modelType.equals(mModelType)
        }




        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_select_model_dialog, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 获取布局中的 Button，并设置点击事件
        val btnClose = view.findViewById<LinearLayout>(R.id.selectModelDialogBackImage)
        btnClose.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            // 关闭 BottomSheetDialog
            bottomSheetDialog.dismiss()

        }

        val modelSelectRecycle = view.findViewById<RecyclerView>(R.id.modelSelectRecycle)
        // 可以在这里进行 RecyclerView 的设置等操作
        modelSelectRecycle.layoutManager = LinearLayoutManager(context)
        modelSelectRecycle.adapter = adapterSelectModel

        Log.e("ceshi","移动的位置是$targetIndex")
        if (targetIndex != 0){
            modelSelectRecycle.layoutManager?.scrollToPosition(targetIndex)
            //adapterSelectModel.upDataSelectModel(modelType,targetIndex)
            adapterSelectModel.selectModelPosition = targetIndex
            // 关键方法：根据Adapter位置获取ViewHolder
            /*var holder = modelSelectRecycle.findViewHolderForAdapterPosition(targetIndex) as? ModelTypeSelectAdapter.ChatViewHolder
            Log.e("ceshi","0移动的位置是$holder")
            holder?.settingModelTypSelectImage?.visibility = View.GONE
            holder?.settingModelTypSelectedImage?.visibility = View.VISIBLE
            adapterSelectModel.notifyItemChanged(targetIndex)*/
            // 2. 更新数据源的选中状态

            // 3. 刷新目标item（无需获取ViewHolder）
            adapterSelectModel.notifyItemChanged(targetIndex)
        }





        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }


    private fun showHelpDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_help)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }




}