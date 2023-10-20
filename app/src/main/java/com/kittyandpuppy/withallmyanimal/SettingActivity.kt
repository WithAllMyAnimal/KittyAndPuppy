package com.kittyandpuppy.withallmyanimal

import SettingPasswordDialogFragment
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
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

        val isDarkMode = sharedPrefs.getBoolean("darkMode", false)
        binding.swSettingSwitchbutton.isChecked = isDarkMode

        // 다크모드 On/Off코드
        binding.swSettingSwitchbutton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sharedPrefs.edit().putBoolean("darkMode", isChecked).apply()
        }

        // English On/Off코드
        val isEnglishMode: Boolean = sharedPrefs.getBoolean("englishMode", false)
        binding.swSettingSwitchbutton2.isChecked = isEnglishMode

        binding.swSettingSwitchbutton2.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                changeLocale("en",this)
            }else{
                changeLocale("default",this)
            }
            sharedPrefs.edit().putBoolean("englishMode", isChecked).apply()
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
    }
    fun changeLocale(languageCode: String, context: Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
}
