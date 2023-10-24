package com.kittyandpuppy.withallmyanimal.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailHospitalBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.write.Hospital

class DetailHospitalActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding : ActivityDetailHospitalBinding by lazy {
        ActivityDetailHospitalBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailHospitalActivity", "Received key: $key, category: $category")

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Hospital::class.java) ?: return

                binding.tvDetailHospitalTitle.text = post.title
                binding.tvDetailDate.text = FBAuth.getTime()
                binding.tvDetailHospitalDate.text = post.date
                binding.tvDetailHospitalDiseaseContents.text = post.title
                binding.tvDetailHospitalLocationContents.text = post.location
                binding.tvDetailHospitalPriceContents.text = post.price
                binding.tvDetailHospitalReviewContents.text = post.content
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailHospitalActivity", "Failed to read post data", error.toException())
            }

        })
    }
}