package com.kittyandpuppy.withallmyanimal.comments

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ItemCommentsBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.util.Constants.currentUserUid

class CommentsRVAdapter(
    val commentDataList: MutableList<CommentsModel>,
    private val postkey: String
) :
    ListAdapter<CommentsModel, CommentsRVAdapter.CommentsItemViewHolder>(diffUtil) {

    private val TAG = CommentsRVAdapter::class.java.simpleName

    inner class CommentsItemViewHolder(private val binding: ItemCommentsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(baseModel: CommentsModel) {
            Log.d(TAG, "BIND")
            Log.d(TAG, commentDataList.toString())


            if (baseModel.uid == currentUserUid) {
                binding.ivCommentsDelete.visibility = View.VISIBLE
                binding.ivCommentsDelete.setOnClickListener {
                    Log.d("currentList값", "currentList:${currentList}")
                    val commentKey = currentList[adapterPosition].key
                    Log.d("commentkey값", "commentkey:$commentKey")

                    if (commentKey != null) {
                        deletecomments(postkey, commentKey)
                    }
                }
            } else {
                binding.ivCommentsDelete.visibility = View.GONE
            }

            val storageRef =
                Firebase.storage.reference.child("profileImages").child("${baseModel.uid}.png")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                binding.circleIvMy.load(uri.toString()) {
                    crossfade(true)
                }
            }
            FBRef.users.child(baseModel.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userId = snapshot.child("profile").child("userIdname").value.toString()
                        binding.tvUserName.text = userId
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("CommentsRVAdapter", "Failed to read userID", error.toException())
                    }
                })
            binding.tvComments.text = commentDataList[adapterPosition].contents

        }


    }

    private fun deletecomments(postkey: String, commentkey: String) {
        FBRef.commentRef.child(postkey).child(commentkey).removeValue()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsItemViewHolder {
        Log.d(TAG, "ON CREATE")
        return CommentsItemViewHolder(
            ItemCommentsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false

            )
        )
    }

    override fun onBindViewHolder(holder: CommentsItemViewHolder, position: Int) {
        holder.bind(currentList[position])
        Log.d("currentList", currentList.size.toString())
        Log.d(TAG, "ON BINDVIEWHOLDER")

    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CommentsModel>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: CommentsModel,
                newItem: CommentsModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: CommentsModel, newItem: CommentsModel): Boolean {
                return oldItem.key == newItem.key
            }
        }
    }
}