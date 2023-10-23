package com.kittyandpuppy.withallmyanimal.mypage

import android.content.Intent
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.DialogProfileChange
import com.kittyandpuppy.withallmyanimal.SettingActivity
import com.kittyandpuppy.withallmyanimal.databinding.FragmentMypageBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.home.HomeModel

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: MyPageRVAdapter

    private val myList = mutableListOf<HomeModel>()

    private val TAG = MypageFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "here")
        setUpRecyclerView()

        val tabLayout = binding.tlMypageTabLayout

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> binding.conMypageTag.visibility = View.VISIBLE
                    1 -> binding.conMypageTag.visibility = View.GONE
                    else -> {}
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnMypageChange.setOnClickListener{
            val dialogFragment = DialogProfileChange()
            val transaction = parentFragmentManager.beginTransaction()
            dialogFragment.show(transaction, "ProfileChangeDialog")
        }
        // 태그 버튼 클릭 시 버튼 색 변경 & 다른 태그 눌릴 시 기존에 선택된 태그 해제
        binding.btnMypageSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }
        binding.btnMypageTagHospital.setOnClickListener {
            resetButtonSelections()
            it.isSelected = true
        }
        binding.btnMypageTagPet.setOnClickListener {
            resetButtonSelections()
            it.isSelected = true
        }
        binding.btnMypageTagBehavior.setOnClickListener {
            resetButtonSelections()
            it.isSelected = true
        }
        binding.btnMypageTagDaily.setOnClickListener {
            resetButtonSelections()
            it.isSelected = true
        }

        getMyData()
    }

    private fun resetButtonSelections() {
        binding.btnMypageTagHospital.isSelected = false
        binding.btnMypageTagPet.isSelected = false
        binding.btnMypageTagBehavior.isSelected = false
        binding.btnMypageTagDaily.isSelected = false
    }

    private fun setUpRecyclerView() {
        rvAdapter = MyPageRVAdapter()

        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (rvAdapter.getItemViewType(position)) {
                    MyPageRVAdapter.TYPE_LIKES -> 2
                    MyPageRVAdapter.TYPE_MY_LIST -> 1
                    else -> throw IllegalArgumentException("Invalid")

                }
            }
        }
        Log.d(TAG, "RV SET UP")

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            this.layoutManager = layoutManager
            adapter = rvAdapter
        }
    }

    private fun getMyData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                myList.clear()
                for (dataModel in snapshot.children) {
                    val key = dataModel.key ?: ""
                    val itemList = dataModel.getValue(HomeModel::class.java)?.copy(key = key)
                    val myUid = FBAuth.getUid()
                    val writerUid = itemList?.uid

                    if (myUid == writerUid) {
                        myList.add(itemList)
                    }
                }
                myList.reverse()
                rvAdapter.submitList(myList as List<Any>?)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Upload Failed", error.toException())
            }
        }
        FBRef.myDiary.addValueEventListener(postListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}