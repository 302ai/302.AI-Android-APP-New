package com.newAi302.app.dialog

/**
 * author :
 * e-mail :
 * time   : 2025/8/5
 * desc   :
 * version: 1.0
 */
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.newAi302.app.R

class RenameDialog(context: Context,private val oldName:String) : Dialog(context) {

    private lateinit var etName: EditText
    private lateinit var textCountTv: TextView
    private var onSaveClickListener: ((String) -> Unit)? = null // 保存按钮点击回调
    private var onCancelClickListener: (() -> Unit)? = null // 取消按钮点击回调

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_rename)

        etName = findViewById(R.id.etName)
        textCountTv = findViewById(R.id.textCountTv)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // 取消按钮逻辑
        btnCancel.setOnClickListener {
            onCancelClickListener?.invoke()
            dismiss() // 关闭对话框
        }

        // 保存按钮逻辑
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            onSaveClickListener?.invoke(newName)
            dismiss() // 关闭对话框
        }

        textCountTv.text = oldName.length.toString()
        etName.setText(oldName)

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
                textCountTv.text = s.toString().length.toString()
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                //chatTitle = s.toString()

            }
        })
    }

    // 设置“保存”按钮点击监听，传递新名称
    fun setOnSaveClickListener(listener: (newName: String) -> Unit) {
        this.onSaveClickListener = listener
    }

    // 设置“取消”按钮点击监听
    fun setOnCancelClickListener(listener: () -> Unit) {
        this.onCancelClickListener = listener
    }

    // 可传入默认名称，用于初始化输入框
    fun setDefaultName(defaultName: String) {
        etName.setText(defaultName)
        etName.setSelection(defaultName.length) // 光标定位到末尾，方便修改
    }
}