package com.kittyandpuppy.withallmyanimal.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.comments.CommentsRVAdapter.Companion.diffUtil
import com.kittyandpuppy.withallmyanimal.databinding.ItemHomeBinding
import com.kittyandpuppy.withallmyanimal.detail.DetailBehaviorActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailDailyActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailHospitalActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailPetActivity
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class HomeRVAdapter(val boardList: MutableList<BaseModel>, private val startForResult: (Intent) -> Unit ) :
    androidx.recyclerview.widget.ListAdapter<BaseModel, HomeRVAdapter.HomeItemViewHolder>(diffUtil) {
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
    inner class HomeItemViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(homeModel: BaseModel) {

            FBRef.users.child(homeModel.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userId = snapshot.child("profile").child("userIdname").value.toString()
                        binding.tvRvId.text = userId
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("HomeRVAdapter", "Failed to read userID", error.toException())
                    }
                })
            binding.tvRvTag.text = homeModel.tags.toString()
            binding.tvRvLikes.text = homeModel.likesCount.toString()
            binding.tvRvChat.text = homeModel.commentsCount.toString()
            binding.ivRvImage.load(homeModel.imageUrl)

            binding.root.setOnClickListener {
                val clickedItem = boardList[adapterPosition]
                val uid = clickedItem.uid
                val key = clickedItem.key
                val category = clickedItem.category

                Log.d("uid값", "uid:$uid")
                Log.d("Key값", "key: $key")
                Log.d("clickedItem값", "clickedItem: $clickedItem")
                Log.d("category값", "category:$category")

                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("board").get()
                reference.addOnSuccessListener {
                    if (it.exists()) {

                        val intent: Intent
                        when (category) {
                            "행동" -> intent =
                                Intent(binding.root.context, DetailBehaviorActivity::class.java)

                            "일상" -> intent = Intent(
                                binding.root.context,
                                DetailDailyActivity::class.java
                            )

                            "병원" -> intent = Intent(
                                binding.root.context,
                                DetailHospitalActivity::class.java
                            )

                            "펫용품" -> intent =
                                Intent(binding.root.context, DetailPetActivity::class.java)

                            else -> intent =
                                Intent(binding.root.context, DetailHospitalActivity::class.java)
                        }
                        intent.putExtra("uid", uid)
                        intent.putExtra("key", key)
                        intent.putExtra("category", category)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startForResult(intent)
                    }
                }
            }
        }
    }
    fun updateImage(key: String, imageUrl: String) {
        val index = boardList.indexOfFirst { it.key == key }
        if (index != -1) {
            boardList[index].imageUrl = imageUrl
            notifyItemChanged(index)
        }

    }
    fun deletePost(key : String) {
        val iterator = boardList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.key == key) {
                val index = boardList.indexOf(item)
                iterator.remove()
                notifyItemChanged(index)
                break
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BaseModel>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                return oldItem.key == newItem.key
            }
        }
    }
}