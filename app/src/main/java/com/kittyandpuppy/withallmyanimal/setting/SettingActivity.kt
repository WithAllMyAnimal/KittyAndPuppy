package com.kittyandpuppy.withallmyanimal.setting

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.ResignationFragment
import com.kittyandpuppy.withallmyanimal.databinding.ActivitySettingBinding
import java.util.Locale


class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var sharedPrefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sharedPrefs = getSharedPreferences("CurrentPrefs", Context.MODE_PRIVATE)

        binding.btnSettingBackbutton.setOnClickListener{
            finish()
        }

        // 비밀번호 변경 버튼
        binding.tvSettingPasswordchange.setOnClickListener{
            val dialogFragment = SettingPasswordDialogFragment()
            val transaction = supportFragmentManager.beginTransaction()
            dialogFragment.show(transaction, "SettingPasswordDialog")
        }

        // 로그아웃 버튼
        binding.tvSettingLogout.setOnClickListener {
            val dialogFragment = SettingLogoutFragment()
            val transaction = supportFragmentManager.beginTransaction()
            dialogFragment.show(transaction,"LogoutDialog")
        }

        binding.tvSettingCancel.setOnClickListener {
            // 회원탈퇴 화면인 ResignationFragment로 이동
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container_setting, ResignationFragment())
            transaction.addToBackStack(null)
            transaction.commit()
    }
    fun changeLocale(languageCode: String, context: Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
}}
