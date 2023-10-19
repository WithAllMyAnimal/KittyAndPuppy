package com.kittyandpuppy.withallmyanimal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.kittyandpuppy.withallmyanimal.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val fragmentpassworddialog = SettingPassworddialogFragment()
        val fragmentlogout= SettingLogoutFragment()

        val button_passworddialog = findViewById<TextView>(R.id.tv_setting_passwordchange)
        val button_logout = findViewById<TextView>(R.id.tv_setting_logout)

        button_passworddialog.setOnClickListener{
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container_setting, fragmentpassworddialog).commit()
        }

        button_logout.setOnClickListener{
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container_setting,fragmentlogout).commit()
        }
    }
}

private fun FragmentTransaction.replace(
    constraint: Int,
    fragmentlogout: SettingLogoutFragment
): FragmentTransaction {
    TODO("Not yet implemented")
}