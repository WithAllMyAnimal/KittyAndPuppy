package com.kittyandpuppy.withallmyanimal.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
    private lateinit var deletedKey: String
    private var boardValueEventListener: ValueEventListener? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var refreshing = false
    private var currentSearchTag: String? = null
    private var startForResult: ActivityResultLauncher<Intent>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "viewCreated 불리니")
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, result.resultCode.toString())
                    deletedKey = result.data?.getStringExtra("deletedPostKey")
                        ?: return@registerForActivityResult
                    rvAdapter?.deletePost(deletedKey)
                }
            }
        setUpRecyclerView()
        onSpinnerItemSelected()
        binding.ivHomeMegaphone.setOnClickListener {
            val intent = Intent(requireContext(), NoticeActivity::class.java)
            startActivity(intent)
        }

        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        var isDogAndCatSpinnerInitialized = false
        var isCategorySpinnerInitialized = false
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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                searchTags(p0.toString())
                if (p0.toString().isEmpty()) {
                    getBoardData()
                }
            }
        })
        binding.etSearch.setOnEditorActionListener { textView, keyboarddown, event ->
            if (keyboarddown == EditorInfo.IME_ACTION_SEARCH || keyboarddown == EditorInfo.IME_ACTION_DONE) {

                // 키보드 내리기
                val inputMethodManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)

                val searchQuery = textView.text.toString()
                searchTags(searchQuery)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultDogCatItem = resources.getStringArray(R.array.dogandcat)[0]
        val defaultCategoryItem = resources.getStringArray(R.array.category)[0]
        val savedDogCatItem = sharedPref.getString("selectedDogCatItem", defaultDogCatItem)
        val savedCategoryItem = sharedPref.getString("selectedCategoryItem", defaultCategoryItem)
        binding.spinnerDogandcat.setSelection(
            (binding.spinnerDogandcat.adapter as ArrayAdapter<String>).getPosition(
                savedDogCatItem
            )
        )
        binding.spinnerCategory.setSelection(
            (binding.spinnerCategory.adapter as ArrayAdapter<String>).getPosition(
                savedCategoryItem
            )
        )
    }

    override fun onPause() {
        super.onPause()

        val selectedDogCatItem = binding.spinnerDogandcat.selectedItem.toString()
        val selectedCategoryItem = binding.spinnerCategory.selectedItem.toString()
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("selectedDogCatItem", selectedDogCatItem)
            putString("selectedCategoryItem", selectedCategoryItem)
            apply()
        }
    }

    private fun saveSearchTagInPreferences(searchTag: String) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("SEARCH_TAG", searchTag)
            apply()
        }
    }

    private fun loadSearchTagFromPreferences(): String {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return ""
        return sharedPref.getString("SEARCH_TAG", "") ?: ""
    }

    private fun setUpRecyclerView() {
        rvAdapter = HomeRVAdapter(boardList) { intent ->
            startForResult?.launch(intent)
        }
        binding.rvHome.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = rvAdapter
        }
    }

    private fun refreshData() {
        if (!refreshing) {
            refreshing = true
            boardList.clear()
            getBoardData()
            swipeRefreshLayout.isRefreshing = false
            refreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        startForResult = null
        super.onDestroy()
    }

    enum class QueryType {
        COMBINED_SPINNER,
        ONLY_CATEGORY,
        ONLY_ANIMAL,
        DEFAULT
    }

    private fun searchTags(tag: String? = null) {
        val search = tag ?: binding.etSearch.text.toString()
        currentSearchTag = search
        saveSearchTagInPreferences(search)
        val positions = listOf("tags/0", "tags/1", "tags/2")

        boardList.clear()
        positions.forEach { position ->
            val query = boardRef.orderByChild(position).equalTo(search)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handlePostsData(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Search Failed", error.toException())
                }
            })
        }
    }

    private fun getBoardData(
        combinedSpinnerValue: String? = null,
        onlyCategory: String? = null,
        onlyAnimal: String? = null
    ) {

        val query: Pair<QueryType, Query> = when {
            combinedSpinnerValue != null -> {
                Pair(
                    QueryType.COMBINED_SPINNER,
                    boardRef.orderByChild("animalAndCategory").equalTo(combinedSpinnerValue)
                )
            }

            onlyCategory != null -> {
                Pair(
                    QueryType.ONLY_CATEGORY,
                    boardRef.orderByChild("category").equalTo(onlyCategory)
                )
            }

            onlyAnimal != null -> {
                Pair(QueryType.ONLY_ANIMAL, boardRef.orderByChild("animal").equalTo(onlyAnimal))
            }

            else -> {
                Pair(QueryType.DEFAULT, boardRef)
            }
        }

        boardValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Snapshot size: ${snapshot.childrenCount}")
                boardList.clear()
                handlePostsData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        }
        query.second.addListenerForSingleValueEvent(boardValueEventListener!!)
    }

    private fun handlePostsData(snapshot: DataSnapshot) {

        for (postSnapshot in snapshot.children) {
            val postKey = postSnapshot.key ?: continue

            val category =
                postSnapshot.child("category").getValue(String::class.java) ?: ""
            val post: BaseModel? = when (category) {
                "행동" -> postSnapshot.getValue(Behavior::class.java)
                "일상" -> postSnapshot.getValue(Daily::class.java)
                "병원" -> postSnapshot.getValue(Hospital::class.java)
                "펫용품" -> postSnapshot.getValue(Pet::class.java)
                else -> null
            }?.apply {
                this.key = postKey
            }

            if (post != null) {
                boardList.add(post)

                val likesCountRef = FBRef.likesCount.child(postKey)
                likesCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val likesCount = snapshot.getValue(Int::class.java) ?: 0
                        post.likesCount = likesCount
                        rvAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("HomeFragment", "Failed to read likes count", error.toException())
                    }
                })

                val commentsRef = FBRef.commentRef.child(postKey)
                commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

    override fun onStart() {
        super.onStart()
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultDogCatItem = resources.getStringArray(R.array.dogandcat)[0]
        val defaultCategoryItem = resources.getStringArray(R.array.category)[0]
        val savedDogCatItem = sharedPref.getString("selectedDogCatItem", defaultDogCatItem)
        val savedCategoryItem = sharedPref.getString("selectedCategoryItem", defaultCategoryItem)
        binding.spinnerDogandcat.setSelection(
            (binding.spinnerDogandcat.adapter as ArrayAdapter<String>).getPosition(
                savedDogCatItem
            )
        )
        binding.spinnerCategory.setSelection(
            (binding.spinnerCategory.adapter as ArrayAdapter<String>).getPosition(
                savedCategoryItem
            )
        )
    }

    override fun onResume() {
        super.onResume()
        currentSearchTag = loadSearchTagFromPreferences() // 화면 재진입 시 검색어 가져오기
        if (!currentSearchTag.isNullOrEmpty()) {
            searchTags(currentSearchTag) // 검색어로 필터링
        } else {
            getBoardData() // 검색어가 없다면 전체 데이터 로드
        }
    }
}