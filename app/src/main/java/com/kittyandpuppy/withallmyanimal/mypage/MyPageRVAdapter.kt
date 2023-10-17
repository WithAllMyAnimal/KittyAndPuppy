package com.kittyandpuppy.withallmyanimal.mypage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageLikeListBinding
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageListBinding

class MyPageRVAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(DIFF_CALLBACK){

    inner class LikesViewHolder(private val binding : ItemMypageLikeListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model : LikesListModel) {
            binding.ivMypageListProfile.load(model.image)
            binding.tvMypageListNickname.text = model.id
            binding.tvMypageListTitle.text = model.title
            binding.tvMypageListDate.text = model.date
        }
    }
    inner class MyListViewHolder(private val binding: ItemMypageListBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model : ListModel) {
            binding.ivMypageRvImage.load(model.image)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
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
        val item = getItem(position)
        when (holder) {
            is LikesViewHolder -> holder.bind(item as LikesListModel)
            is MyListViewHolder -> holder.bind(item as ListModel)
        }
    }
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LikesListModel -> TYPE_LIKES
            is ListModel -> TYPE_MY_LIST
            else -> throw IllegalArgumentException("Undefined")
        }
    }
    companion object {

        const val TYPE_LIKES = 0
        const val TYPE_MY_LIST = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is LikesListModel && newItem is LikesListModel -> oldItem.uid == newItem.uid
                    oldItem is ListModel && newItem is ListModel -> oldItem.uid == newItem.uid
                    else -> false
                }
            }
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }
}