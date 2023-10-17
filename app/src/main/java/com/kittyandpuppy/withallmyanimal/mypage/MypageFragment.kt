package com.kittyandpuppy.withallmyanimal.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private var _binding : FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: MyPageRVAdapter

    private lateinit var tabLayout : TabLayout
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private val tabIconList = listOf(R.drawable.image_mypage_diary_button, R.drawable.pet_like)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        val viewPager = binding.vpMypageViewpager
//        val tabLayout = binding.tlMypageTabLayout
//        viewPager.adapter = ViewPagerAdapter(this)

        viewPagerAdapter = ViewPagerAdapter(this)

        binding.vpMypageViewpager.adapter = viewPagerAdapter
        tabLayout = binding.tlMypageTabLayout

        TabLayoutMediator(tabLayout, binding.vpMypageViewpager) { tab, position ->
            tab.setIcon(tabIconList[position])
        }.attach()

        binding.vpMypageViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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

        binding.recyclerviewMypageList.apply {
            setHasFixedSize(true)
            this.layoutManager = GridLayoutManager(requireContext(), 3)
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewPagerAdapter = null
        _binding = null
    }
}