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
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.newAi302.app.R

class FeedBackDialog(context: Context) : Dialog(context) {

    private lateinit var etFeedBck: EditText
    private var onSaveClickListener: ((String) -> Unit)? = null // 保存按钮点击回调
    private var onCancelClickListener: (() -> Unit)? = null // 取消按钮点击回调
    private var oneBad = false
    private var twoBad = false
    private var threeBad = false
    private var fourBad = false
    private var fiveBad = false

    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_feed_back)

        etFeedBck = findViewById(R.id.etFeedBck)
        val btnCancel: ImageView = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        val tvBad1: TextView = findViewById(R.id.tvBad1)
        val tvBad2: TextView = findViewById(R.id.tvBad2)
        val tvBad3: TextView = findViewById(R.id.tvBad3)
        val tvBad4: TextView = findViewById(R.id.tvBad4)
        val tvBad5: TextView = findViewById(R.id.tvBad5)

        tvBad1.setOnClickListener {
            if (!oneBad){
                oneBad = true
                tvBad1.setTextColor( ContextCompat.getColor(context, android.R.color.white))
                tvBad1.setBackgroundResource(R.drawable.shape_select_site_bg_purple_line)
            }else{
                oneBad = false
                tvBad1.setTextColor( ContextCompat.getColor(context, android.R.color.black))
                tvBad1.setBackgroundResource(R.drawable.shape_select_site_bg_write_line)
            }
        }
        tvBad2.setOnClickListener {
            if (!twoBad){
                twoBad = true
                tvBad2.setTextColor( ContextCompat.getColor(context, android.R.color.white))
                tvBad2.setBackgroundResource(R.drawable.shape_select_site_bg_purple_line)
            }else{
                twoBad = false
                tvBad2.setTextColor( ContextCompat.getColor(context, android.R.color.black))
                tvBad2.setBackgroundResource(R.drawable.shape_select_site_bg_write_line)
            }
        }
        tvBad3.setOnClickListener {
            if (!threeBad){
                threeBad = true
                tvBad3.setTextColor( ContextCompat.getColor(context, android.R.color.white))
                tvBad3.setBackgroundResource(R.drawable.shape_select_site_bg_purple_line)
            }else{
                threeBad = false
                tvBad3.setTextColor( ContextCompat.getColor(context, android.R.color.black))
                tvBad3.setBackgroundResource(R.drawable.shape_select_site_bg_write_line)
            }
        }
        tvBad4.setOnClickListener {
            if (!fourBad){
                fourBad = true
                tvBad4.setTextColor( ContextCompat.getColor(context, android.R.color.white))
                tvBad4.setBackgroundResource(R.drawable.shape_select_site_bg_purple_line)
            }else{
                fourBad = false
                tvBad4.setTextColor( ContextCompat.getColor(context, android.R.color.black))
                tvBad4.setBackgroundResource(R.drawable.shape_select_site_bg_write_line)
            }
        }
        tvBad5.setOnClickListener {
            if (!fiveBad){
                fiveBad = true
                tvBad5.setTextColor( ContextCompat.getColor(context, android.R.color.white))
                tvBad5.setBackgroundResource(R.drawable.shape_select_site_bg_purple_line)
            }else{
                fiveBad = false
                tvBad5.setTextColor( ContextCompat.getColor(context, android.R.color.black))
                tvBad5.setBackgroundResource(R.drawable.shape_select_site_bg_write_line)
            }
        }

        // 取消按钮逻辑
        btnCancel.setOnClickListener {
            onCancelClickListener?.invoke()
            dismiss() // 关闭对话框
        }

        // 保存按钮逻辑
        btnSave.setOnClickListener {
            val feedBack = etFeedBck.text.toString().trim()
            var result = StringBuilder()
            if (oneBad){
                result.append("内容质量问题")
            }
            if (twoBad){
                result.append("逻辑缺陷")
            }
            if (threeBad){
                result.append("表达不清")
            }
            if (fourBad){
                result.append("答非所问")
            }
            if (fiveBad){
                result.append(feedBack)
            }
            onSaveClickListener?.invoke(result.toString())
            dismiss() // 关闭对话框
        }
    }

    // 设置“保存”按钮点击监听，传递新名称
    fun setOnSaveClickListener(listener: (feedBack: String) -> Unit) {
        this.onSaveClickListener = listener
    }

    // 设置“取消”按钮点击监听
    fun setOnCancelClickListener(listener: () -> Unit) {
        this.onCancelClickListener = listener
    }

    // 可传入默认名称，用于初始化输入框
    fun setDefaultName(defaultName: String) {
        etFeedBck.setText(defaultName)
        etFeedBck.setSelection(defaultName.length) // 光标定位到末尾，方便修改
    }
}