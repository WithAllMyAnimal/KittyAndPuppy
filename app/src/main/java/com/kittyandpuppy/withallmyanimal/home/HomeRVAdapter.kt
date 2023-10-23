package com.kittyandpuppy.withallmyanimal.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.ItemHomeBinding
import com.kittyandpuppy.withallmyanimal.detail.DetailBehaviorActivity
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class HomeRVAdapter(val boardList: MutableList<HomeModel>) :
    ListAdapter<HomeModel, HomeRVAdapter.HomeItemViewHolder>(diffUtil) {
    inner class HomeItemViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(homeModel: HomeModel) {

            val storageRef = Firebase.storage.reference.child("${homeModel.key}.png")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                binding.ivRvImage.load(uri.toString()) {
                    crossfade(true)
                }
            }
            FBRef.users.child(homeModel.uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvRvId.text = userId
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("HomeRVAdapter", "Failed to read userID", error.toException())
                }
            })
            binding.tvRvTag.text = boardList[adapterPosition].tag
            binding.tvRvLikes.text = boardList[adapterPosition].likes.toString()
            binding.tvRvChat.text = boardList[adapterPosition].comments.toString()

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(
            ItemHomeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<HomeModel>() {
            override fun areContentsTheSame(oldItem: HomeModel, newItem: HomeModel): Boolean {
                return oldItem == newItem
            }
            override fun areItemsTheSame(oldItem: HomeModel, newItem: HomeModel): Boolean {
                return oldItem.uid == newItem.uid
            }
        }
    }
}