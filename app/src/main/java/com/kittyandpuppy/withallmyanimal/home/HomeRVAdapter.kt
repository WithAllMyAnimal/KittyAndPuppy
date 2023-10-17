package com.kittyandpuppy.withallmyanimal.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kittyandpuppy.withallmyanimal.databinding.ItemHomeBinding

class HomeRVAdapter : ListAdapter<HomeModel, HomeRVAdapter.HomeItemViewHolder>(diffUtil) {
    inner class HomeItemViewHolder(private val binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(homeModel: HomeModel){
            binding.ivRvImage.load(homeModel.image)
            binding.tvRvId.text = homeModel.id
            binding.tvRvTag.text = homeModel.tag
            binding.tvRvLikes.text = homeModel.likes.toString()
            binding.tvRvChat.text = homeModel.comments.toString()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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