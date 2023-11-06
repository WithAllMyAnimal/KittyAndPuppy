package com.kittyandpuppy.withallmyanimal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMainBinding
import com.kittyandpuppy.withallmyanimal.databinding.FabLayoutBinding
import com.kittyandpuppy.withallmyanimal.home.HomeFragment
import com.kittyandpuppy.withallmyanimal.mypage.MypageFragment
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.MypageBehavior
import com.kittyandpuppy.withallmyanimal.write.MypageDaily
import com.kittyandpuppy.withallmyanimal.write.MypageHospital
import com.kittyandpuppy.withallmyanimal.write.MypagePet

class MainActivity : AppCompatActivity() {

    // activity, fab layout binding 선언
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var fabBinding: FabLayoutBinding
    private lateinit var fragmentManager: FragmentManager
    private val homeFragment = HomeFragment()
    private val mypageFragment = MypageFragment()
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    companion object {
        const val USER_PROFILE_IMAGES = "profileImages"
    }

    // 리팩토링 시 필요한 것
    init {
        Constants.currentUserUid = auth.currentUser!!.uid
        storage.reference.child(USER_PROFILE_IMAGES)
            .child("${Constants.currentUserUid}.png").downloadUrl.addOnSuccessListener {
                Constants.currentUserProfileImg = it
            }.addOnFailureListener {
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fabBinding = FabLayoutBinding.bind(binding.root)
        setContentView(binding.root)

        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            switchFragment(homeFragment)
        }

        setupBottomNavigation()
        setupFloatingActionButton()
        setupFABclick()
    }

    // BottomNavigation이 눌렸을 때 화면전환
    private fun setupBottomNavigation() {

        binding.bnMain.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_bn_you -> {
                    switchFragment(homeFragment)
                    true
                }

                R.id.main_bn_home -> {
                    switchFragment(mypageFragment)
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

    private fun setupFABclick() {
        fabBinding.fabHospital.setOnClickListener {
            val intent = Intent(this, MypageHospital::class.java)
            startActivity(intent)
        }

        fabBinding.fabPet.setOnClickListener {
            val intent = Intent(this, MypagePet::class.java)
            startActivity(intent)
        }

        fabBinding.fabBehavior.setOnClickListener {
            val intent = Intent(this, MypageBehavior::class.java)
            startActivity(intent)
        }

        fabBinding.fabDaily.setOnClickListener {
            val intent = Intent(this, MypageDaily::class.java)
            startActivity(intent)
        }
    }

    // Fragment 전환 시 필요한 것
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFramelayout.id, fragment)
            .commit()
    }

    // 아이템이 visible로 바뀔 수 있게 함(FAB 등장)
    private fun showFAB(fab: ExtendedFloatingActionButton) {
        val showAnim = AnimationUtils.loadAnimation(this, R.anim.fab_anim)
        fab.startAnimation(showAnim)
        fab.visibility = View.VISIBLE
    }

    // 아이템이 invisible로 바뀔 수 있게 함(FAB 퇴장)
    private fun hideFAB(fab: ExtendedFloatingActionButton) {
        val hideAnim = AnimationUtils.loadAnimation(this, R.anim.hide_fab_anim)
        fab.startAnimation(hideAnim)
        fab.visibility = View.INVISIBLE
    }
}
