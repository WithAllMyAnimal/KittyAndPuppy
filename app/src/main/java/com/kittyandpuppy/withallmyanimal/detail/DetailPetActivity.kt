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
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailPetBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.Pet

class DetailPetActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding : ActivityDetailPetBinding by lazy {
        ActivityDetailPetBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailPetActivity", "Received key: $key, category: $category")

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(FBAuth.getUid()).child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Pet::class.java) ?: return

                binding.tvDetailPetTitle.text = post.title
                binding.tvDetailDate.text = FBAuth.getTime()
                binding.tvDetailPetNameContents.text = post.name
                binding.tvDetailPetPriceContents.text = post.price
                binding.ratDetailPetStar.rating = post.satisfaction.toFloat()
                binding.tvDetailPetCautionContents.text = post.caution
                binding.tvDetailPetReviewContents.text = post.content
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailPetActivity", "Failed to read post data", error.toException())
            }
        })
        val storageRef = Firebase.storage.reference.child("${key}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailPetPictureLeft.load(uri.toString()) {
                crossfade(true)
            }
        }
        FBRef.users.child(FBAuth.getUid())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailPetNickname.text = userId
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailPetActivity", "Failed to read userID", error.toException())
                }
            })
    }
}