package com.kittyandpuppy.withallmyanimal.comments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.FragmentCommentsBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.util.Constants

class CommentsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: CommentsRVAdapter
    private val commentDataList = mutableListOf<CommentsModel>()
    private lateinit var key: String

    private val TAG = CommentsFragment::class.java.simpleName

    private lateinit var commentsListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        key = arguments?.getString("key").toString()

        val uid = FBAuth.getUid()
        val userLikesRef = FBRef.users.child(uid).child("likedlist").child(key)
        val postLikesCountRef = FBRef.likesCount.child(key)
        checkLikeStatus(uid, userLikesRef)
        updateLikes(key)

        binding.ivUserLikes.setOnClickListener {
            userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userLikesRef.removeValue()
                        binding.ivUserLikes.setImageResource(R.drawable.pet_unlike)
                        postLikesCountRef.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val currentLikesCount = currentData.getValue(Int::class.java) ?: 0
                                currentData.value = currentLikesCount - 1
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                error: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                Log.d(TAG, "postTransaction:onComplete:$error")
                                updateLikes(key)
                            }
                        })
                    } else {
                        userLikesRef.setValue(true)
                        binding.ivUserLikes.setImageResource(R.drawable.pet_like)
                        postLikesCountRef.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val currentLikesCount = currentData.getValue(Int::class.java) ?: 0
                                currentData.value = currentLikesCount + 1
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                error: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                // Transaction completed
                                Log.d(TAG, "postTransaction:onComplete:$error")
                                updateLikes(key)
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Like Checking Failed", error.toException())
                }
            })
        }

        binding.btnOk.setOnClickListener {
            insertComments(key)
        }
        setMyProfileImage()
        setUpRV()
        getComments(key)
    }

    private fun checkLikeStatus(uid: String, userLikesRef: DatabaseReference) {
        userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.ivUserLikes.setImageResource(if (snapshot.exists()) R.drawable.pet_like else R.drawable.pet_unlike)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Like Status Checking Failed", error.toException())
            }
        })
    }

    private fun setUpRV() {
        rvAdapter = CommentsRVAdapter(commentDataList, key)
        binding.rvComments.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = rvAdapter
        }
        Log.d(TAG, "SET UP RV")
    }

    private fun getComments(key: String) {
        commentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentDataList.clear()
                Log.d("snapshot.children", "snapshot.children,${snapshot}")
                for (dataModel in snapshot.children) {
                    Log.d("dataModel", "datamodel,${dataModel.key}")
                    val item = dataModel.getValue(CommentsModel::class.java)
                    if (item != null) {
                        commentDataList.add(item)
                    }
                }
                rvAdapter.submitList(commentDataList.toList())
                Log.d(TAG, commentDataList.size.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(commentsListener)
    }

    private fun insertComments(key: String) {
        val commentkey = FBRef.commentRef.child(key).push().key
        if (commentkey != null) {
            FBRef.commentRef
                .child(key)
                .child(commentkey)
                .setValue(
                    CommentsModel(
                        binding.etReview.text.toString(),
                        FBAuth.getUid(),
                        commentkey
                    )
                )
        }
        binding.etReview.setText("")
    }

    private fun setMyProfileImage() {
        val userId = Firebase.auth.currentUser?.uid
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("profileImages").child("$userId.png")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }.addOnFailureListener {
        }
    }

    private fun updateLikes(postKey: String) {
        var count = 0

        FBRef.users.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    if (userSnapshot.child("likedlist").hasChild(postKey)) {
                        count++
                    }
                }
                binding.tvUserLikeList.text = if (count > 0) "${count}명이 좋아합니다!" else ""
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "User Name Loading Failed", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FBRef.commentRef.child(key).removeEventListener(commentsListener)
        _binding = null
    }
}