package com.kittyandpuppy.withallmyanimal.comments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.FragmentCommentsBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class CommentsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: CommentsRVAdapter
    private val commentDataList = mutableListOf<CommentsModel>()

    private val TAG = CommentsFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val key = arguments?.getString("key").toString()
        Log.d(TAG, key)

        binding.btnOk.setOnClickListener {
            insertComments(key)
        }
        setMyProfileImage()
        setUpRV()
        getComments(key)
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
        val storageRef = Firebase.storage.reference.child("${FBAuth.getUid()}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri.toString()) {
                crossfade(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}