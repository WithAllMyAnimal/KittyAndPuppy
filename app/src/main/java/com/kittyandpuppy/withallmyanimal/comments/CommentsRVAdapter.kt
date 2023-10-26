package com.kittyandpuppy.withallmyanimal.comments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kittyandpuppy.withallmyanimal.databinding.ItemCommentsBinding
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class CommentsRVAdapter : ListAdapter<BaseModel, CommentsRVAdapter.CommentsItemViewHolder>(diffUtil) {
    inner class CommentsItemViewHolder(private val binding : ItemCommentsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(baseModel: BaseModel) {
            binding.tvUserName.text = "abc"
            binding.tvComments.text = "가나다라마바사아자차카타파하"
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsItemViewHolder {
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