package com.kittyandpuppy.withallmyanimal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMainBinding
import com.kittyandpuppy.withallmyanimal.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentFragment: Fragment
    private lateinit var fragmentManager: FragmentManager

    private val mypageFragment = MypageFragment()
    private val homeFragment = HomeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentManager = supportFragmentManager
        currentFragment = homeFragment

        binding.mainFab.setOnClickListener {
            switchFragment(mypageFragment)
        }

        val navigation = binding.bnMain
        navigation.setOnItemSelectedListener{ menuItem ->
            when(menuItem.title) {
                "홈" -> {
                    switchFragment(homeFragment)
                    true
                }
                "마이페이지" -> {
                    switchFragment(mypageFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != currentFragment) {
            fragmentManager.beginTransaction().replace(R.id.main_framelayout, fragment).commit()
            currentFragment = fragment
        }
    }
}