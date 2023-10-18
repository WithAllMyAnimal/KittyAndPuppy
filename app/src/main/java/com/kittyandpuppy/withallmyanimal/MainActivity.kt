package com.kittyandpuppy.withallmyanimal

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMainBinding
import com.kittyandpuppy.withallmyanimal.databinding.FabLayoutBinding
import com.kittyandpuppy.withallmyanimal.home.HomeFragment
import com.kittyandpuppy.withallmyanimal.mypage.MypageFragment

class MainActivity : AppCompatActivity() {

    // activity, fab layout binding 선언
    private lateinit var binding: ActivityMainBinding
    private lateinit var fabBinding: FabLayoutBinding

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        fabBinding = FabLayoutBinding.bind(binding.root)
        setContentView(binding.root)

        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
        }

        setupBottomNavigation()
        setupFloatingActionButton()
    }

    // BottomNavigation이 눌렸을 때 화면전환
    private fun setupBottomNavigation() {
        binding.bnMain.setOnItemSelectedListener { menuItem ->
            Log.d("JINA", "setupBottomNavigation: ${menuItem.title}")
            when (menuItem.title) {
                "홈" -> {
                    switchFragment(HomeFragment())
                    true
                }

                "마이페이지" -> {
                    switchFragment(MypageFragment())
                    true
                }

                else -> false
            }
        }
    }

    // FAB가 눌렸을 때 조건문
    private fun setupFloatingActionButton() {
        binding.mainFab.setOnClickListener {
            if (fabBinding.fabHospital.visibility == View.INVISIBLE) {
                showFAB(fabBinding.fabHospital)
                showFAB(fabBinding.fabPet)
                showFAB(fabBinding.fabBehavior)
                showFAB(fabBinding.fabDaily)
            } else {
                hideFAB(fabBinding.fabHospital)
                hideFAB(fabBinding.fabPet)
                hideFAB(fabBinding.fabBehavior)
                hideFAB(fabBinding.fabDaily)
            }
        }
    }

    // Fragment 전환 시 필요한 것
    private fun switchFragment(fragment: Fragment) {
        val currentFragment = fragmentManager.findFragmentById(R.id.main_framelayout)
        if (currentFragment == null || currentFragment::class.java != fragment::class.java) {
            fragmentManager.beginTransaction().replace(R.id.main_framelayout, fragment).commit()
        }
    }

    // 아이템이 visible로 바뀔 수 있게 함(FAB 등장)
    private fun showFAB(fab: ExtendedFloatingActionButton) {
        fab.visibility = View.VISIBLE
    }

    // 아이템이 invisible로 바뀔 수 있게 함(FAB 등장)
    private fun hideFAB(fab: ExtendedFloatingActionButton) {
        fab.visibility = View.INVISIBLE
    }
}
