package com.kittyandpuppy.withallmyanimal.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.Behavior


class DetailBehaviorActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding: ActivityDetailBehaviorBinding by lazy{
        ActivityDetailBehaviorBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailBehaviorActivity","key: $key, category: $category")

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(uid).child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Behavior::class.java) ?: return

                binding.tvDetailBehaviorTitle.text = post.title
                binding.tvDetailDate.text = FBAuth.getTime()
                binding.tvDetailBehaviorCautionContents.text = post.content
                binding.tvDetailBehaviorReviewContents.text = post.review
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailBehaviorActivity","Failed to read post data",error.toException())
            }
        })
        val storegeRef = Firebase.storage.reference.child("${key}.png")
        storegeRef.downloadUrl.addOnSuccessListener { uri->
            binding.ivDetailBehaviorPictureLeft.load(uri.toString()){
                crossfade(true)
            }
        }
        FBRef.users.child(FBAuth.getUid())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userid = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailBehaviorNickname.text = userid
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailBehaviorActivity", "Failed to read userId", error.toException())
                }
            })

    }
}