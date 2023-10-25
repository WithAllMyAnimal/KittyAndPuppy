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
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailDailyBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.Daily

class DetailDailyActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding : ActivityDetailDailyBinding by lazy {
        ActivityDetailDailyBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(uid).child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DetailHospitalActivity", "$snapshot")
                val post = snapshot.getValue(Daily::class.java) ?: return

                binding.tvDetailDailyTitle.text = post.title
                binding.tvDetailDate.text = FBAuth.getTime()
                binding.tvDetailDailyCautionContents.text = post.content

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailDailyActivity", "failed to read post data",error.toException())
            }
        })
        val strageRef = Firebase.storage.reference.child("${key}.png")
        strageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailDailyPictureLeft.load(uri.toString()){
                crossfade(true)
            }
        }
        FBRef.users.child(FBAuth.getUid())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("Profile").child("userIdname").value.toString()
                    binding.tvDetailDailyNickname.text = userId
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailDailyActivity", "Failed to read userID", error.toException())
                }
            })
    }
}