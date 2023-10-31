package com.kittyandpuppy.withallmyanimal.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentHomeBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.FBRef.Companion.boardRef
import com.kittyandpuppy.withallmyanimal.setting.NoticeActivity
import com.kittyandpuppy.withallmyanimal.write.BaseModel
import com.kittyandpuppy.withallmyanimal.write.Behavior
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.Hospital
import com.kittyandpuppy.withallmyanimal.write.Pet

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var rvAdapter: HomeRVAdapter? = null
    private val boardList = mutableListOf<BaseModel>()
    private val TAG = HomeFragment::class.java.simpleName
    var isDogAndCatSpinnerInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        binding.spinnerDogandcat.setSelection(1)
        onSpinnerItemSelected()

        binding.ivHomeMegaphone.setOnClickListener {
            val intent = Intent(requireContext(), NoticeActivity::class.java)
            startActivity(intent)
        }

        val dogCatAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dogandcat,
            android.R.layout.simple_spinner_dropdown_item
        )
        dogCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDogandcat.adapter = dogCatAdapter

        binding.spinnerDogandcat.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {  if (!isDogAndCatSpinnerInitialized) {
                    isDogAndCatSpinnerInitialized = true
                    return
                }
                    onSpinnerItemSelected()
                 }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        val categoryAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.category,
            android.R.layout.simple_spinner_dropdown_item
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    onSpinnerItemSelected()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
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

    private fun getBoardData(spinnerValue: String? = null) {
        val query: Query = if (spinnerValue != null) {
            FBRef.users.orderByChild("profile/dogcat").equalTo(spinnerValue)
        } else {
            boardRef
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                boardList.clear()
                val users: Iterable<DataSnapshot> = snapshot.children

                for (userSnapshot in users) {
                    val uid = userSnapshot.key ?: continue

                    if (spinnerValue != null) {
                        val boardRef = FirebaseDatabase.getInstance().getReference("board/$uid")
                        boardRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(boardSnapshot: DataSnapshot) {
                                handlePostsData(boardSnapshot, uid)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(TAG, "Failed to get board data", error.toException())
                            }
                        })
                    } else {
                        handlePostsData(userSnapshot, uid)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        })
    }

    private fun handlePostsData(snapshot: DataSnapshot, uid: String) {
        for (postSnapshot in snapshot.children) {
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

                val likesRef = FBRef.likesRef.child(postKey).child("likes")
                likesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        post.likesCount = snapshot.childrenCount.toInt()
                        rvAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("HomeFragment", "Failed to read likes count", error.toException())
                    }
                })

                val commentsRef = FBRef.commentRef.child(postKey)
                commentsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        post.commentsCount = snapshot.childrenCount.toInt()
                        rvAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(
                            "HomeFragment",
                            "Failed to read comments count",
                            error.toException()
                        )
                    }
                })
            }
        }
        rvAdapter?.submitList(boardList.toList())
    }
    fun onSpinnerItemSelected() {
        val spinnerValue = if (binding.spinnerDogandcat.selectedItem.toString() == "전체") {
            null
        } else {
            binding.spinnerDogandcat.selectedItem.toString()
        }
        getBoardData(spinnerValue)
    }
}