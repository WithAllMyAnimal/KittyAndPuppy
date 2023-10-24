package com.kittyandpuppy.withallmyanimal.home

import android.content.Intent
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentHomeBinding
import com.kittyandpuppy.withallmyanimal.detail.DetailBehaviorActivity
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.notice.NoticeActivity
import com.kittyandpuppy.withallmyanimal.write.BaseModel
import com.kittyandpuppy.withallmyanimal.write.Behavior
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.Hospital
import com.kittyandpuppy.withallmyanimal.write.Pet
import java.lang.System.load

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: HomeRVAdapter

    private val boardList = mutableListOf<BaseModel>()

    private val TAG = HomeFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        binding.ivHomeMegaphone.setOnClickListener {
            val intent = Intent(requireContext(), NoticeActivity::class.java)
            startActivity(intent)
        }
        getBoardData()
    }

    private fun setUpRecyclerView() {
        rvAdapter = HomeRVAdapter(boardList)
        binding.rvHome.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = rvAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getBoardData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                boardList.clear()
                for (userSnapshot in snapshot.children) {
                    val uid = userSnapshot.key ?: continue
                    for (postSnapshot in userSnapshot.children) {
                        val postKey = postSnapshot.key ?: continue

                        val category =
                            postSnapshot.child("category").getValue(String::class.java) ?: ""
                        val post: BaseModel? = when (category) {
                            "Behavior" -> postSnapshot.getValue(Behavior::class.java)
                            "Daily" -> postSnapshot.getValue(Daily::class.java)
                            "Hospital" -> postSnapshot.getValue(Hospital::class.java)
                            "Pet" -> postSnapshot.getValue(Pet::class.java)
                            else -> null
                        }?.apply {
                            this.uid = uid
                            this.key = postKey
                        }

                        if (post != null) {
                            boardList.add(post)
                        }
                    }
                }
                boardList.reverse()
                rvAdapter.submitList(boardList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        }
        FBRef.boardRef.addListenerForSingleValueEvent(postListener)
    }
}