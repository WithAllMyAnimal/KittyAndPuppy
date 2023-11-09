package com.kittyandpuppy.withallmyanimal.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageLikeListBinding
import com.kittyandpuppy.withallmyanimal.databinding.ItemMypageListBinding
import com.kittyandpuppy.withallmyanimal.detail.DetailBehaviorActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailDailyActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailHospitalActivity
import com.kittyandpuppy.withallmyanimal.detail.DetailPetActivity
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class MyPageRVAdapter(val list: MutableList<BaseModel>, private val startForResult: (Intent) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<BaseModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    val TAG = MyPageRVAdapter::class.java.simpleName

    inner class LikesViewHolder(private val binding: ItemMypageLikeListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clickedItem = list[adapterPosition]
                val uid = clickedItem.uid
                val key = clickedItem.key
                val category = clickedItem.category

                Log.d("MyPageRVAdapter", "uid:$uid")
                Log.d("MyPageRVAdapter", "key: $key")
                Log.d("MyPageRVAdapter", "category: $category")

                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("board").get()

                reference.addOnSuccessListener {
                    if (it.exists()){
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
                                Intent(binding.root.context, DetailPetActivity::class.java)
                        }
                        intent.putExtra("uid", uid)
                        intent.putExtra("key", key)
                        intent.putExtra("category", category)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startForResult(intent)
                    }
                }
            }
        }

        fun bind(model: BaseModel) {
            binding.tvMypageListTitle.text = model.title
            binding.tvMypageListDate.text = model.time
            binding.tvMypageListReview.text = model.content

            val userRef = FirebaseDatabase.getInstance().getReference("users").child(model.uid).child("profile")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfileImageKey = snapshot.child("profileImage").getValue(String::class.java)
                    val userIdName = snapshot.child("userIdname").getValue(String::class.java)

                    userProfileImageKey?.let { key ->
                        val storageRef = Firebase.storage.reference.child("profileImages").child("${model.uid}.png")
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            binding.ivMypageListProfile.load(uri.toString()) {
                                crossfade(true)
                            }
                        }.addOnFailureListener {
                        }
                    }
                    binding.tvMypageListNickname.text = userIdName ?: ""
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Failed to read user info", error.toException())
                }
            })
        }
    }

    inner class MyListViewHolder(private val binding: ItemMypageListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clickedItem = list[adapterPosition]
                val uid = clickedItem.uid
                val key = clickedItem.key
                val category = clickedItem.category

                Log.d("MyPageRVAdapter", "uid:$uid")
                Log.d("MyPageRVAdapter", "key: $key")
                Log.d("MyPageRVAdapter", "category: $category")

                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("board").get()

                reference.addOnSuccessListener {
                    if (it.exists()){

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
                                Intent(binding.root.context, DetailPetActivity::class.java)
                        }
                        intent.putExtra("uid", uid)
                        intent.putExtra("key", key)
                        intent.putExtra("category", category)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startForResult(intent)
                    }
                }
            }
        }
        fun bind(model: BaseModel) {
            binding.ivMypageRvImage.load(model.imageUrl)
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
        val item = currentList[position]
        when (holder) {
            is LikesViewHolder -> {
                if (selectedTab == TYPE_LIKES && list.isNotEmpty()) {
                    holder.bind(item)
                }
            }
            is MyListViewHolder -> {
                if (selectedTab == TYPE_MY_LIST && list.isNotEmpty()) {
                    holder.bind(item)
                }
            }
        }
    }
    override fun getItemViewType(position: Int): Int {
        return selectedTab
    }

    private var selectedTab = TYPE_MY_LIST

    fun selectedTab(tab: Int) {
        selectedTab = tab
        notifyDataSetChanged()
    }

    fun deletePost(key : String) {
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.key == key) {
                val index = list.indexOf(item)
                iterator.remove()
                notifyItemChanged(index)
                break
            }
        }
    }

    fun updateImage(key: String, imageUrl: String) {
        val index = list.indexOfFirst { it.key == key }
        if (index != -1) {
            list[index].imageUrl = imageUrl
            notifyItemChanged(index)
        }
    }

    companion object {
        const val TYPE_LIKES = 0
        const val TYPE_MY_LIST = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BaseModel>() {
            override fun areItemsTheSame(
                oldItem: BaseModel,
                newItem: BaseModel
            ): Boolean {
                return oldItem.key == newItem.key
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