package com.kittyandpuppy.withallmyanimal.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentCommentsBinding

class CommentsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: CommentsRVAdapter
    private var isLiked = false
    private lateinit var userId: String
    private lateinit var likeRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        likeRef = FirebaseDatabase.getInstance().getReference("좋아요")

        setUpRV()
        checkLikeStatus()

        binding.ivUserUnlikes.setOnClickListener {
            if (isLiked) {
                onUnlikeClick()
            } else {
                onLikeClick()
            }
        }
    }

    private fun checkLikeStatus() {
        likeRef.child("좋아요 목록").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isLiked = snapshot.exists()
                    updateLikeButton()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun onLikeClick() {
        likeRef.child("좋아요 목록").child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLiked = true
                    updateLikeButton()
                    updateLikeList()
                } else {
                }
            }
    }

    private fun onUnlikeClick() {
        likeRef.child("좋아요 목록").child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLiked = false
                    updateLikeButton()
                    updateLikeList()
                } else {
                }
            }
    }

    private fun updateLikeButton() {
        val likeImageRes = if (isLiked) R.drawable.pet_like else R.drawable.pet_unlike
        binding.ivUserUnlikes.setImageResource(likeImageRes)
    }

    private fun updateLikeList() {
        likeRef.child("좋아요 목록").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likeCount = snapshot.childrenCount.toInt()

                val userIds = mutableListOf<String>()
                for (childSnapshot in snapshot.children) {
                    val userId = childSnapshot.key
                    userId?.let { userIds.add(it) }
                }

                val firstUser = userIds.firstOrNull()
                if (firstUser != null) {
                    binding.tvUserLikeList.text = "${firstUser}님 외 ${likeCount - 1}명이 좋아합니다."
                } else {
                    binding.tvUserLikeList.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun setUpRV() {
        rvAdapter = CommentsRVAdapter()
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}