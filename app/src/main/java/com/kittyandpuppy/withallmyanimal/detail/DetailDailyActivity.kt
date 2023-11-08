package com.kittyandpuppy.withallmyanimal.detail

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import coil.load
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.comments.CommentsFragment
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailDailyBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.mypage.MypageOtherUsers
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.Daily
import com.kittyandpuppy.withallmyanimal.write.MypageDaily

class DetailDailyActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    private val binding: ActivityDetailDailyBinding by lazy {
        ActivityDetailDailyBinding.inflate(layoutInflater)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri") ?: return@registerForActivityResult
                val title = result.data?.getStringExtra("title") ?: return@registerForActivityResult
                val content = result.data?.getStringExtra("content") ?: return@registerForActivityResult
                loadUpdatedImage(imageUri, title, content)
            }
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
                val tasks = arrayListOf<Task<Void>>()

                tasks.add(FBRef.boardRef.child(key).removeValue())
                tasks.add(FBRef.commentRef.child(key).removeValue())
                tasks.add(FBRef.likesCount.child(key).removeValue())
                tasks.add(FirebaseStorage.getInstance().getReference("${key}.png").delete())

                Tasks.whenAll(tasks).addOnCompleteListener { task ->
                    alertDialog.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent().apply {
                            putExtra("postDeleted", true)
                            putExtra("deletedPostUid", uid)
                            putExtra("deletedPostKey", key)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        task.exception?.let {
                            Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            alertDialog.findViewById<Button>(R.id.btn_settinglogout_cancelbutton)?.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        binding.ivDetailEdit.setOnClickListener {
            val intent = Intent(this, MypageDaily::class.java)
            intent.putExtra("key", key)
            startForResult.launch(intent)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DetailHospitalActivity", "$snapshot")
                val post = snapshot.getValue(Daily::class.java) ?: return

                binding.tvDetailDailyTitle.text = post.title
                binding.tvDetailDate.text = post.time
                binding.tvDetailDailyReviewContents.text = post.content
                binding.ivDetailDailyPictureLeft.load(post.imageUrl)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailDailyActivity", "failed to read post data", error.toException())
            }
        })
        val storageProfile = Firebase.storage.reference.child("profileImages")
            .child("$uid.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailDailyProfile.load(uri.toString()) {
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
            binding.ivCircleMy.load(uri.toString()) {
                crossfade(true)
            }
        }

        FBRef.users.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
        binding.btnDetailDailyBack.setOnClickListener {
            finish()
        }
    }
    private fun loadUpdatedImage(imageUri : String, title : String, content : String) {
        binding.ivDetailDailyPictureLeft.load(imageUri)
        binding.tvDetailDailyTitle.text = title
        binding.tvDetailDailyReviewContents.text = content
    }
}