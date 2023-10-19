package com.kittyandpuppy.withallmyanimal

import SettingPasswordDialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.kittyandpuppy.withallmyanimal.databinding.ActivitySettingBinding


class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.tvSettingPasswordchange.setOnClickListener{
            val dialogFragment = SettingPasswordDialogFragment()
            val transaction = supportFragmentManager.beginTransaction()
            dialogFragment.show(transaction, "SettingPasswordDialog")
        }

        binding.tvSettingLogout.setOnClickListener {
            val dialogFragment = SettingLogoutFragment()
            val transaction = supportFragmentManager.beginTransaction()
            dialogFragment.show(transaction,"LogoutDialog")
        }
    }
}
