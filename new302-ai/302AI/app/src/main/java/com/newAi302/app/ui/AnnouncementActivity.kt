package com.newAi302.app.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityAnnouncementBinding
import com.newAi302.app.databinding.ActivitySettingBinding

class AnnouncementActivity : BaseActivity() {
    private lateinit var binding: ActivityAnnouncementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_announcement)
        binding = ActivityAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }

    }
}