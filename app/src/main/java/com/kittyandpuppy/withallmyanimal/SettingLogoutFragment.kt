package com.kittyandpuppy.withallmyanimal

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

import androidx.appcompat.app.AppCompatActivity


class SettingLogoutFragment : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.kittyandpuppy.withallmyanimal.R.layout.fragment_setting_logout)

//        val tvString = "정말 로그아웃 하시겠습니까?"
//        val tvLogout = binding.tvSettingLogouttextview
//        val span = SpannableStringBuilder(tvString)
//        span.setSpan(ForegroundColorSpan(Color.parseColor(@color/red)),4,7,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        tvLogout.text = span
        //*아직 확인 불가능
    }
}