package com.kittyandpuppy.withallmyanimal.home

import android.annotation.SuppressLint
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.ItemHomeBinding
import com.kittyandpuppy.withallmyanimal.detail.DetailBehaviorActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailDailyActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailHospitalActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailPetActivity
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class HomeRVAdapter(val boardList: MutableList<BaseModel>) :
    ListAdapter<BaseModel, HomeRVAdapter.HomeItemViewHolder>(diffUtil) {
    inner class HomeItemViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clickedItem = boardList[adapterPosition]
                val key = clickedItem.key
                val category = clickedItem.category

                Log.d("Key값", "key: $key")
                Log.d("category값", "category:$category")


                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("board")

                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(datasnapshot: DataSnapshot) {
                        if (datasnapshot.exists()) {

                            val intent: Intent
                            when (category) {
                                "Behavior" -> intent =
                                    Intent(binding.root.context, DetailBehaviorActivity::class.java)

                                "Daily" -> intent = Intent(
                                    binding.root.context,
                                    DetailDailyActivity::class.java
                                )

                                "Hospital" -> intent = Intent(
                                    binding.root.context,
                                    DetailHospitalActivity::class.java
                                )

                                "pet" -> intent =
                                    Intent(binding.root.context, DetailPetActivity::class.java)

                                else -> intent =
                                    Intent(binding.root.context, DetailPetActivity::class.java)
                            }
                            intent.putExtra("key", key)
                            intent.putExtra("category", category)
                            binding.root.context.startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("HomeRVAdapter", "Failed to read userID", error.toException())
                    }
                })
            }
        }
        fun bind(homeModel: BaseModel) {

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
            binding.tvRvTag.text = boardList[adapterPosition].tags.toString()

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
        val diffUtil = object : DiffUtil.ItemCallback<BaseModel>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                return oldItem == newItem
            }
            override fun areItemsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                return oldItem.uid == newItem.uid
            }
        }
    }
}