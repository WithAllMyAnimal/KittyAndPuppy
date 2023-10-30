package com.kittyandpuppy.withallmyanimal.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentHomeBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
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
    var isBreedSpinnerInitialized = false

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

        val dogCatAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dogandcat,
            android.R.layout.simple_spinner_item
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
                ) {
                    if (!isDogAndCatSpinnerInitialized) {
                        isDogAndCatSpinnerInitialized = true
                        return
                    }

                    if (position == 0) {
                        Toast.makeText(context, "종류를 선택하세요", Toast.LENGTH_SHORT).show()
                    }

                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    val breedArray = when (selectedItem) {
                        "전체" -> R.array.all
                        "강아지" -> R.array.dogbreed
                        "고양이" -> R.array.catbreed
                        else -> return
                    }
                    val breedAdapter = ArrayAdapter.createFromResource(
                        requireContext(),
                        breedArray,
                        android.R.layout.simple_spinner_item
                    )
                    breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerBreed.adapter = breedAdapter
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.spinnerBreed.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (!isBreedSpinnerInitialized) {
                        isBreedSpinnerInitialized = true
                        return
                    }

                    if (position == 0) {
                        Toast.makeText(context, "품종을 선택하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    override fun onResume() {
        super.onResume()
        getBoardData()
        Log.d(TAG, "RESUME")
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

                            val likesRef = FBRef.likesRef.child(postKey).child("likes")
                            likesRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    post.likesCount = snapshot.childrenCount.toInt()
                                    rvAdapter?.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d(
                                        "HomeFragment",
                                        "Failed to read likes count",
                                        error.toException()
                                    )
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
                }
                Log.d(TAG, boardList.size.toString())
                rvAdapter?.submitList(boardList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        }
        FBRef.boardRef.addListenerForSingleValueEvent(postListener)
    }
}