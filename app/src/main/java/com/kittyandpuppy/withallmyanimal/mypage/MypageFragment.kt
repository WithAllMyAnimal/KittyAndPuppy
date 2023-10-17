package com.kittyandpuppy.withallmyanimal.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private var _binding : FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: MyPageRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = binding.vpMypageViewpager
        val tabLayout = binding.tlMypageTabLayout
        viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            when (position) {
                0 -> tab.text = "일지 목록"
                1 -> tab.text = "좋아요한 목록"
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> setUpRecyclerView()
                    1 -> setUpRecyclerViewLikes()
                }
            }
        })
    }
    private fun setUpRecyclerView() {
        rvAdapter = MyPageRVAdapter()

        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when(rvAdapter.getItemViewType(position)) {
                    MyPageRVAdapter.TYPE_MY_LIST -> 1
                    else -> throw IllegalArgumentException("Invalid")
                }
            }
        }

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            this.layoutManager = layoutManager
            adapter = rvAdapter
        }
    }

    private fun setUpRecyclerViewLikes() {
        rvAdapter = MyPageRVAdapter()

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }
}