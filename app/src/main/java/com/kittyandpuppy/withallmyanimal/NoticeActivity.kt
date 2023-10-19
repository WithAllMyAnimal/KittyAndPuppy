package com.kittyandpuppy.withallmyanimal.notice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kittyandpuppy.withallmyanimal.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnNoticePetBack.setOnClickListener{
            finish()
        }
    }
}
