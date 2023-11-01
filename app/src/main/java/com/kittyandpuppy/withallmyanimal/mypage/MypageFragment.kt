package com.kittyandpuppy.withallmyanimal.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.FragmentMypageBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.setting.SettingActivity
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.BaseModel
import com.kittyandpuppy.withallmyanimal.write.Behavior
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.Hospital
import com.kittyandpuppy.withallmyanimal.write.Pet
import java.util.Calendar

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: MyPageRVAdapter

    private val list = mutableListOf<BaseModel>()
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var database: DatabaseReference
    private val TAG = MypageFragment::class.java.simpleName

    private lateinit var userProfileRef: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        binding.imgMypageProfile.load(Constants.currentUserProfileImg)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        loadDefaultTabData()
        loadUserData()

        val tabLayout = binding.tlMypageTabLayout
        val defaultTab = tabLayout.getTabAt(0)
        defaultTab?.select()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        gridLayoutManager.spanCount = 3
                        binding.conMypageTag.visibility = View.VISIBLE
                        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_MY_LIST)

                    }
                    1 -> {
                        gridLayoutManager.spanCount = 1
                        binding.conMypageTag.visibility = View.GONE
                        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_LIKES)
                    }
                    else -> {}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnMypageChange.setOnClickListener {
            val dialogFragment = DialogProfileChange()
            val transaction = parentFragmentManager.beginTransaction()
            dialogFragment.show(transaction, "ProfileChangeDialog")
        }

        binding.btnMypageSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }

        binding.btnMypageTagHospital.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected){
                getMyData("병원")
            } else {
                getMyData()
            }
        }
        binding.btnMypageTagPet.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected){
                getMyData("펫용품")
            } else {
                getMyData()
            }
        }
        binding.btnMypageTagBehavior.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected){
                getMyData("이상행동")
            } else {
                getMyData()
            }
        }
        binding.btnMypageTagDaily.setOnClickListener {
            resetButtonSelectionsExcept(it)
            it.isSelected = !it.isSelected
            if (it.isSelected){
                getMyData("일상")
            } else {
                getMyData()
            }
        }
    }

    // 버튼 2번 클릭 시 원래 대로 돌아가기
    private fun resetButtonSelectionsExcept(currentButton: View) {
        if (binding.btnMypageTagHospital != currentButton) binding.btnMypageTagHospital.isSelected =
            false
        if (binding.btnMypageTagPet != currentButton) binding.btnMypageTagPet.isSelected = false
        if (binding.btnMypageTagBehavior != currentButton) binding.btnMypageTagBehavior.isSelected =
            false
        if (binding.btnMypageTagDaily != currentButton) binding.btnMypageTagDaily.isSelected = false
    }

    private fun setUpRecyclerView() {

        rvAdapter = MyPageRVAdapter(list)

        gridLayoutManager = GridLayoutManager(requireContext(), 3)

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            adapter = rvAdapter
        }
    }

    private fun loadDefaultTabData() {
        binding.conMypageTag.visibility = View.VISIBLE
        rvAdapter.selectedTab(MyPageRVAdapter.TYPE_MY_LIST)
        getMyData()
    }

    private fun getMyData(filter : String? = null) {
        val currentUserId = FBAuth.getUid()

        val query = if (filter != null) {
            FBRef.boardRef.orderByChild("uidAndCategory").equalTo("${currentUserId}$filter")
        } else {
            FBRef.boardRef.orderByChild("uid").equalTo(currentUserId)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (postSnapshot in snapshot.children) {
                    val postKey = postSnapshot.key ?: continue
                    val category = postSnapshot.child("category").getValue(String::class.java) ?: ""

                    val post: BaseModel? = when (category) {
                        "이상행동" -> postSnapshot.getValue(Behavior::class.java)
                        "일상" -> postSnapshot.getValue(Daily::class.java)
                        "병원" -> postSnapshot.getValue(Hospital::class.java)
                        "펫용품" -> postSnapshot.getValue(Pet::class.java)
                        else -> null
                    }?.apply {
                        this.uid = currentUserId
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

    private fun loadUserData() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            userProfileRef =
                FirebaseDatabase.getInstance().getReference("users").child(userId).child("profile")

            valueEventListener = userProfileRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded && !isDetached && !isRemoving) {
                        val userIdname = snapshot.child("userIdname").getValue(String::class.java)
                        val petName = snapshot.child("petName").getValue(String::class.java)
                        val birth = snapshot.child("birth").getValue(String::class.java)
                        Log.d("JINA", "onDataChange: ${birth}")

                        binding.tvMypage.text = petName
                        binding.tvMypageNickname.text = userIdname
                        binding.tvMypageBirth.text = birth

                        if (birth != null && todayBirthday(birth)) {
                            binding.ivMypageCakeLeft.visibility = View.VISIBLE
                        } else {
                            binding.ivMypageCakeLeft.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error loading user data: ${error.message}")
                }
            })
        }
    }

    private fun todayBirthday(birth:String):Boolean {
        // 오늘 날짜 불러오기
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        Log.d("jina", "Today's Date: ${month}/$day")

        val birthParts = birth.split("/")
        if (birthParts.size < 3) {
            Log.d("jina", "Invalid birth format: $birth")
            return false
        }

        val birthYear = birthParts[0].toIntOrNull()
        val birthMonth = birthParts[1].toIntOrNull() ?: run {
            Log.d("jina", "Invalid birth month in: $birth")
            return false
        }
        val birthDay = birthParts[2].toIntOrNull() ?: run {
            Log.d("jina", "Invalid birth day in: $birth")
            return false
        }

        Log.d("rina", "Parsed Birth Date: $birthMonth$birthDay")

        return day == birthDay && month == birthMonth

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::userProfileRef.isInitialized && ::valueEventListener.isInitialized) {
            userProfileRef.removeEventListener(valueEventListener)
        }
        _binding = null
    }
}