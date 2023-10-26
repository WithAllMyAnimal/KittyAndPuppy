package com.kittyandpuppy.withallmyanimal.detail

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.comments.CommentsFragment
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailHospitalBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.write.Hospital

class DetailHospitalActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding : ActivityDetailHospitalBinding by lazy {
        ActivityDetailHospitalBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailHospitalActivity", "Received key: $key, category: $category")

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(uid).child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DetailHospitalActivity", "$snapshot")
                val post = snapshot.getValue(Hospital::class.java) ?: return
                Log.d("DetailHospitalActivity", "snapshot")

                binding.tvDetailHospitalTitle.text = post.title
                binding.tvDetailDate.text = FBAuth.getTime()
                binding.tvDetailHospitalDateContents.text = post.date
                binding.tvDetailHospitalDiseaseContents.text = post.disease
                binding.tvDetailHospitalLocationContents.text = post.location
                binding.tvDetailHospitalPriceContents.text = post.price
                binding.tvDetailHospitalReviewContents.text = post.content

                Log.d("DetailHospitalActivity", "Snapshot data: ${snapshot.toString()}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailHospitalActivity", "Failed to read post data", error.toException())
            }
        })
        val storageRef = Firebase.storage.reference.child("${key}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailHospitalPictureLeft.load(uri.toString()) {
                crossfade(true)
            }
        }
        val storageProfile = Firebase.storage.reference.child("${FBAuth.getUid()}.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri.toString()){
                crossfade(true)
            }
        }
        FBRef.users.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailHospitalNickname.text = userId
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailHospitalActivity", "Failed to read userID", error.toException())
                }
            })
        binding.etReview.paintFlags = binding.etReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.etReview.setOnClickListener {
            val commentsFragment = CommentsFragment()
            val bundle = Bundle()
            bundle.putString("key", key)
            commentsFragment.arguments = bundle
            commentsFragment.show(supportFragmentManager, "comments")
        }
        binding.btnDetailHospitalBack.setOnClickListener{
            finish()
        }
    }
}