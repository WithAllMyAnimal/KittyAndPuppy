package com.kittyandpuppy.withallmyanimal.mypage

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageLikeListBinding
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageListBinding
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class MyPageRVAdapter(val list: MutableList<BaseModel>) :
    ListAdapter<BaseModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    val TAG = MyPageRVAdapter::class.java.simpleName

    inner class LikesViewHolder(private val binding: ItemMypageLikeListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BaseModel) {
            binding.tvMypageListTitle.text = model.title
            binding.tvMypageListDate.text = model.time
        }
    }

    inner class MyListViewHolder(private val binding: ItemMypageListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BaseModel) {
            Log.d("rv adapter", "bind request")
            val storageRef = Firebase.storage.reference.child("${model.key}.png")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                binding.ivMypageRvImage.load(uri.toString()) {
                    crossfade(true)
                    Log.d("rv adapter", "success")
                }
            }.addOnFailureListener {
                binding.ivMypageRvImage.load(R.drawable.image_no_images_found) {
                    crossfade(true)
                    Log.d(TAG, "binded")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        Log.d(TAG, "ON CREATE")
        return when (viewType) {
            TYPE_LIKES -> {
                val binding = ItemMypageLikeListBinding.inflate(layoutInflater, parent, false)
                LikesViewHolder(binding)
            }

            TYPE_MY_LIST -> {
                val binding = ItemMypageListBinding.inflate(layoutInflater, parent, false)
                MyListViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        when (holder) {
            is LikesViewHolder -> holder.bind(item)
            is MyListViewHolder -> holder.bind(item)
        }
        Log.d(TAG, "ON BINDVIEWHOLDER")
    }

    override fun getItemViewType(position: Int): Int {
        return selectedTab
    }

    private var selectedTab = TYPE_MY_LIST

    fun selectedTab(tab: Int) {
        selectedTab = tab
        notifyDataSetChanged()
    }

    companion object {

        const val TYPE_LIKES = 0
        const val TYPE_MY_LIST = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BaseModel>() {
            override fun areItemsTheSame(
                oldItem: BaseModel,
                newItem: BaseModel
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: BaseModel,
                newItem: BaseModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}