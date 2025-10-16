package com.newAi302.app.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.newAi302.app.R
import com.newAi302.app.databinding.ActivityConsumptionDetailsBinding
import com.newAi302.app.databinding.ActivityVersionInformationBinding

class ConsumptionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConsumptionDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConsumptionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }
    }

}