package com.kittyandpuppy.withallmyanimal.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
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
import com.kittyandpuppy.withallmyanimal.firebase.FBRef.Companion.users
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
    var isCategorySpinnerInitialized = false

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
        loadSearchText()

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
                ) {
                    if (!isDogAndCatSpinnerInitialized) {
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
                    if (!isCategorySpinnerInitialized) {
                        isCategorySpinnerInitialized = true
                        return
                    }
                    onSpinnerItemSelected()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        binding.spinnerDogandcat.setSelection(1)
        binding.spinnerCategory.setSelection(1)
        onSpinnerItemSelected()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                searchTags()
                if (p0.toString().isEmpty()) {
                    getBoardData()
                }
            }
        })
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
    override fun onPause() {
        super.onPause()
        saveSearchText()
    }
    enum class QueryType {
        COMBINED_SPINNER,
        ONLY_CATEGORY,
        ONLY_ANIMAL,
        DEFAULT
    }
    private fun searchTags() {
        val search = binding.etSearch.text.toString()
        val query = boardRef.orderByChild("tags").startAt(search).endAt("$search\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                boardList.clear()
                handlePostsData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Search Failed", error.toException())
            }
        })
    }
    private fun getBoardData(
        combinedSpinnerValue: String? = null,
        onlyCategory: String? = null,
        onlyAnimal: String? = null
    ) {
        val query: Pair<QueryType, Query> = when {
            combinedSpinnerValue != null -> {
                Pair(QueryType.COMBINED_SPINNER, boardRef.orderByChild("animalAndCategory").equalTo(combinedSpinnerValue))
            }
            onlyCategory != null -> {
                Pair(QueryType.ONLY_CATEGORY, boardRef.orderByChild("category").equalTo(onlyCategory))
            }
            onlyAnimal != null -> {
                Pair(QueryType.ONLY_ANIMAL, boardRef.orderByChild("animal").equalTo(onlyAnimal))
            }
            else -> { Pair(QueryType.DEFAULT, boardRef) }
        }

        query.second.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Snapshot size: ${snapshot.childrenCount}")
                boardList.clear()
                handlePostsData(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        })
    }
    private fun handlePostsData(snapshot: DataSnapshot) {

        for (postSnapshot in snapshot.children) {
            val postKey = postSnapshot.key ?: continue

            val category =
                postSnapshot.child("category").getValue(String::class.java) ?: ""
            val post: BaseModel? = when (category) {
                "이상행동" -> postSnapshot.getValue(Behavior::class.java)
                "일상" -> postSnapshot.getValue(Daily::class.java)
                "병원" -> postSnapshot.getValue(Hospital::class.java)
                "펫용품" -> postSnapshot.getValue(Pet::class.java)
                else -> null
            }?.apply {
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
        val spinnerDogCatValue = binding.spinnerDogandcat.selectedItem.toString()
        val spinnerCategoryValue = binding.spinnerCategory.selectedItem.toString()

        when {
            spinnerDogCatValue == "전체" && spinnerCategoryValue != "전체" -> {
                getBoardData(onlyCategory = spinnerCategoryValue)
            }
            spinnerDogCatValue != "전체" && spinnerCategoryValue == "전체" -> {
                getBoardData(onlyAnimal = spinnerDogCatValue)
            }
            spinnerDogCatValue != "전체" && spinnerCategoryValue != "전체" -> {
                val combinedKey = "$spinnerDogCatValue$spinnerCategoryValue"
                getBoardData(combinedSpinnerValue = combinedKey)
            }
            else -> {
                getBoardData()
            }
        }
    }
    private fun saveSearchText() {
        val pref = requireActivity().getSharedPreferences("pref", 0)
        val edit = pref.edit()
        edit.putString("search", binding.etSearch.text.toString())
        edit.apply()
    }
    private fun loadSearchText() {
        val pref = requireActivity().getSharedPreferences("pref", 0)
        binding.etSearch.setText(pref.getString("search",""))
    }
}