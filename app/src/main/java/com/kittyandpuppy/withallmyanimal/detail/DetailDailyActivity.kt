package com.kittyandpuppy.withallmyanimal.detail

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.comments.CommentsFragment
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailDailyBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.mypage.MypageOtherUsers
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.MypageBehavior
import com.kittyandpuppy.withallmyanimal.write.MypageDaily

class DetailDailyActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding : ActivityDetailDailyBinding by lazy {
        ActivityDetailDailyBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailDailyActivity", "Received key: $key, category: $category")

        if (uid == FBAuth.getUid()) {
            binding.ivDetailEdit.isVisible = true
            binding.ivDetailDelete.isVisible = true
        }
        binding.ivDetailDelete.setOnClickListener {
            val myDialog = LayoutInflater.from(this).inflate(R.layout.alarm_delete, null)
            val builder = AlertDialog.Builder(this)
                .setView(myDialog)

            val alertDialog = builder.show()
            alertDialog.findViewById<Button>(R.id.btn_settinglogout_checkbutton)?.setOnClickListener {
                FBRef.boardRef.child(key).removeValue()
                Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().putExtra("postDeleted", true)
                resultIntent.putExtra("deletedPostUid", uid)
                resultIntent.putExtra("deletedPostKey", key)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            alertDialog.findViewById<Button>(R.id.btn_settinglogout_cancelbutton)?.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        binding.ivDetailEdit.setOnClickListener {
            val intent = Intent(this, MypageDaily::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
        databaseRef.addValueEventListener(object : ValueEventListener{
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
        val storageRef = Firebase.storage.reference.child("${key}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailDailyPictureLeft.load(uri.toString()){
                crossfade(true)
            }
        }
        val storageProfile = Firebase.storage.reference.child("profileImages")
            .child("$uid.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailDailyProfile.load(uri.toString()){
                crossfade(true)
            }
        }
        binding.ivDetailDailyProfile.setOnClickListener {
            val intent = Intent(this, MypageOtherUsers::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        val storageProfileReview = Firebase.storage.reference.child("profileImages")
            .child("${Constants.currentUserUid}.png")
        storageProfileReview.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri.toString()){
                crossfade(true)
            }
        }

        FBRef.users.child(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailDailyNickname.text = userId
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailDailyActivity", "Failed to read userID", error.toException())
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
        binding.btnDetailDailyBack.setOnClickListener{
            finish()
        }
    }
}