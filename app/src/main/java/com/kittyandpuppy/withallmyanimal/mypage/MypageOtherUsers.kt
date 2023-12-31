package com.kittyandpuppy.withallmyanimal.mypage

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageOtherUsersBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.BaseModel
import com.kittyandpuppy.withallmyanimal.write.Behavior
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.Hospital
import com.kittyandpuppy.withallmyanimal.write.Pet
import java.util.Calendar

class MypageOtherUsers : AppCompatActivity() {

    private val binding: ActivityMypageOtherUsersBinding by lazy {
        ActivityMypageOtherUsersBinding.inflate(layoutInflater)
    }
    private lateinit var rvAdapter: MyPageRVAdapter
    private val list = mutableListOf<BaseModel>()
    private lateinit var gridLayoutManager: GridLayoutManager
    private val TAG = MypageFragment::class.java.simpleName
    private lateinit var uid: String
    private lateinit var deletedKey: String
    private var startForResult: ActivityResultLauncher<Intent>? = null

    override fun onDestroy() {
        startForResult = null
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid") ?: return
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    deletedKey = result.data?.getStringExtra("deletedPostKey")
                        ?: return@registerForActivityResult
                    rvAdapter.deletePost(deletedKey)
                }
            }

        setUpRV()
        loadDefaultTabData()
        loadUserData()
        setProfileImage()

        val tabLayout = binding.tlOtherUserTabLayout
        val defaultTab = tabLayout.getTabAt(0)
        defaultTab?.select()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        gridLayoutManager.spanCount = 3
                        binding.conOtherUserTag.visibility = View.VISIBLE
                        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_MY_LIST)
                        getMyData()

                    }

                    1 -> {
                        gridLayoutManager.spanCount = 1
                        binding.conOtherUserTag.visibility = View.GONE
                        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_LIKES)
                        getLikedPosts()
                    }

                    else -> {}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnOtherUserTagHospital.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                getMyData("병원")
            } else {
                getMyData()
            }
        }
        binding.btnOtherUserTagPet.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                getMyData("펫용품")
            } else {
                getMyData()
            }
        }
        binding.btnOtherUserTagBehavior.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                getMyData("행동")
            } else {
                getMyData()
            }
        }
        binding.btnOtherUserTagDaily.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                getMyData("일상")
            } else {
                getMyData()
            }
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun resetButtonSelectionsExcept(currentButton: View) {
        if (binding.btnOtherUserTagHospital != currentButton) binding.btnOtherUserTagHospital.isSelected =
            false
        if (binding.btnOtherUserTagPet != currentButton) binding.btnOtherUserTagPet.isSelected =
            false
        if (binding.btnOtherUserTagBehavior != currentButton) binding.btnOtherUserTagBehavior.isSelected =
            false
        if (binding.btnOtherUserTagDaily != currentButton) binding.btnOtherUserTagDaily.isSelected =
            false
    }

    private fun setUpRV() {
        rvAdapter = MyPageRVAdapter(list) { intent ->
            startForResult?.launch(intent)
        }
        gridLayoutManager = GridLayoutManager(this, 3)

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            adapter = rvAdapter
        }
    }

    private fun loadDefaultTabData() {
        binding.conOtherUserTag.visibility = View.VISIBLE
        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_MY_LIST)
        getMyData()
    }

    private fun getMyData(filter: String? = null) {

        val query = if (filter != null) {
            FBRef.boardRef.orderByChild("uidAndCategory").equalTo("${uid}$filter")
        } else {
            FBRef.boardRef.orderByChild("uid").equalTo(uid)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (postSnapshot in snapshot.children) {
                    val postKey = postSnapshot.key ?: continue
                    val category = postSnapshot.child("category").getValue(String::class.java) ?: ""

                    val post: BaseModel? = when (category) {
                        "행동" -> postSnapshot.getValue(Behavior::class.java)
                        "일상" -> postSnapshot.getValue(Daily::class.java)
                        "병원" -> postSnapshot.getValue(Hospital::class.java)
                        "펫용품" -> postSnapshot.getValue(Pet::class.java)
                        else -> null
                    }?.apply {
                        this.uid = uid
                        this.key = postKey
                    }

                    if (post != null) {
                        list.add(post)
                    }
                }
                rvAdapter.submitList(list.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MyPageFragment", "loadPost:onCancelled", error.toException())
            }
        })
    }

    private fun getLikedPosts() {
        list.clear()
        FBRef.users.child(uid).child("likedlist")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val likedPostKeys = snapshot.children.map { it.key!! }
                        for (postKey in likedPostKeys) {
                            FBRef.boardRef.child(postKey)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val post = snapshot.getValue(BaseModel::class.java)
                                        post?.let { list.add(it) }
                                        rvAdapter.submitList(list.toList())
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.w(
                                            "MypageOtherUsers",
                                            "loadPost:onCancelled",
                                            error.toException()
                                        )
                                    }
                                })
                        }
                    } else {
                        rvAdapter.submitList(emptyList<BaseModel>())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            })
    }

    private fun loadUserData() {
        val userId = uid
        val storage = Firebase.storage
        val storageRef = storage.reference

        if (userId != null) {
            FBRef.users.child("$userId").child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageRef = storageRef.child("profileImages").child("$userId.png")
                        val userIdname =
                            snapshot.child("userIdname").getValue(String::class.java)
                        val petName = snapshot.child("petName").getValue(String::class.java)
                        val birth = snapshot.child("birth").getValue(String::class.java)
                        Log.d("JINA", "onDataChange: $userIdname pet $petName birth $birth")

                        binding.tvOtherUserPageName.text = petName
                        binding.tvOtherUserPageId.text = userIdname
                        binding.tvOtherUserPageBirth.text = birth

                        if (birth != null && todayBirthday(birth)) {
                            binding.ivMypageBirthday.visibility = View.VISIBLE
                            binding.ivMypageBirthdayBackground.visibility = View.VISIBLE
                        } else {
                            binding.ivMypageBirthday.visibility = View.INVISIBLE
                            binding.ivMypageBirthdayBackground.visibility = View.INVISIBLE
                        }
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            binding.imgOtherUserProfile.load(uri) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error loading user data: ${error.message}")
                    }
                })
        }
    }

    private fun todayBirthday(birth: String): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val birthParts = birth.split("/")
        if (birthParts.size < 3) {
            return false
        }
        val birthMonth = birthParts[1].toIntOrNull() ?: run {
            return false
        }
        val birthDay = birthParts[2].toIntOrNull() ?: run {
            return false
        }
        return day == birthDay && month == birthMonth
    }

    private fun setProfileImage() {
        val storageRef = Firebase.storage.reference.child("profileImages").child("${uid}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.imgOtherUserProfile.load(uri.toString()) {
                crossfade(true)
            }
        }
    }
}