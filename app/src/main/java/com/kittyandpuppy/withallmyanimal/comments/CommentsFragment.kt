package com.kittyandpuppy.withallmyanimal.comments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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

    private val TAG = CommentsFragment::class.java.simpleName

    val key = arguments?.getString("key").toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, key)

        val uid = FBAuth.getUid()
        val likesRef = FBRef.likesRef.child(key).child("likes")

        checkLikeStatus(uid, likesRef)

        binding.ivUserLikes.setOnClickListener {
            likesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(uid)) {
                        likesRef.child(uid).removeValue()
                        binding.ivUserLikes.setImageResource(R.drawable.pet_unlike)
                    } else {
                        likesRef.child(uid).setValue(true)
                        binding.ivUserLikes.setImageResource(R.drawable.pet_like)
                    }
                    updateLikes(likesRef)
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
//        getComments(key)
    }

    override fun onResume() {
        super.onResume()
        getComments(key)
    }

    private fun checkLikeStatus(uid: String, likesRef: DatabaseReference) {
        likesRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(uid)) {
                    binding.ivUserLikes.setImageResource(R.drawable.pet_like)
                } else {
                    binding.ivUserLikes.setImageResource(R.drawable.pet_unlike)
                }
                updateLikes(likesRef)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Like Status Checking Failed", error.toException())
            }
        })
    }

    private fun setUpRV() {
        rvAdapter = CommentsRVAdapter(commentDataList)
        binding.rvComments.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = rvAdapter
        }
        Log.d(TAG, "SET UP RV")
    }

    private fun getComments(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentDataList.clear()
                for (dataModel in snapshot.children) {
                    val item = dataModel.getValue(CommentsModel::class.java)
                    if (item != null) {
                        commentDataList.add(item)
                    }
                }
                rvAdapter.submitList(commentDataList)
                Log.d(TAG, commentDataList.size.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Post Failed", error.toException())
            }
        }
        FBRef.commentRef.child(key).addListenerForSingleValueEvent(postListener)
    }

    private fun insertComments(key: String) {
        FBRef.commentRef
            .child(key)
            .push()
            .setValue(
                CommentsModel(
                    binding.etReview.text.toString(),
                    FBAuth.getUid()
                )
            )
    }

    private fun setMyProfileImage() {
        val storageRef = Firebase.storage.reference.child("profileImages").child("${Constants.currentUserUid}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri.toString()) {
                crossfade(true)
            }
        }
    }

    private fun updateLikes(likesRef: DatabaseReference) {
        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                if (count > 0) {
                    val firstUserUid = snapshot.children.first().key
                    FBRef.users.child(firstUserUid!!).child("profile").child("userIdname")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val firstUserName = snapshot.value.toString()
                                binding.tvUserLikeList.text = "$firstUserName 외 ${count - 1}명이 좋아합니다!"
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(TAG, "User Name Loading Failed", error.toException())
                            }
                        })
                } else {
                    binding.tvUserLikeList.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Like Updating Failed", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
