package com.newAi302.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityMainBinding
import com.newAi302.app.databinding.ActivityShowScreenImageBinding
import com.newAi302.app.utils.BitmapUtils

class ShowScreenImageActivity : BaseActivity() {
    private lateinit var binding:ActivityShowScreenImageBinding
    companion object {
        private const val EXTRA_KEY_PATH = "path"
        @JvmStatic
        fun action(context: Context, path: String) {
            val intent = Intent(context, ShowScreenImageActivity::class.java)
            intent.putExtra(EXTRA_KEY_PATH, path)
            context.startActivity(intent)
        }
    }
    private var path = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShowScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.getStringExtra(EXTRA_KEY_PATH) != ""){
            path = intent.getStringExtra(EXTRA_KEY_PATH).toString()
        }
        binding.iv.setImageBitmap(BitmapUtils.decodeBitmap(path, this))
    }


}