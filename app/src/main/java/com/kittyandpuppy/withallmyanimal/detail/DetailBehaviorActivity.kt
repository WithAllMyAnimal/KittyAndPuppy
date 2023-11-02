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
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import coil.load
import coil.request.CachePolicy
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
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.mypage.MypageOtherUsers
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.Behavior
import com.kittyandpuppy.withallmyanimal.write.MypageBehavior

class DetailBehaviorActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference

    private val binding: ActivityDetailBehaviorBinding by lazy {
        ActivityDetailBehaviorBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailBehaviorActivity","$uid, key: $key, category: $category")
        Log.d("DetailBehaviorActivity", FBAuth.getUid())


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
                FirebaseStorage.getInstance().getReference("${key}.png").delete()
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
            val intent = Intent(this, MypageBehavior::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
        databaseRef.addValueEventListener(object : ValueEventListener{
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

        val storageProfile = Firebase.storage.reference.child("profileImages")
            .child("$uid.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailBehaviorProfile.load(uri.toString()){

                crossfade(true)
            }
        }
        binding.ivDetailBehaviorProfile.setOnClickListener {
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
                    val userid = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailBehaviorNickname.text = userid
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailBehaviorActivity", "Failed to read userId", error.toException())
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
        binding.btnDetailBehaviorBack.setOnClickListener{
            finish()
        }
    }
//    override fun onStart() {
//        super.onStart()
//        val key = intent.getStringExtra("key") ?: return
//        val storageRef = Firebase.storage.reference.child("${key}.png")
//        Log.d("mmmmm", "성공22")
//        storageRef.downloadUrl.addOnSuccessListener { uri->
//            binding.ivDetailBehaviorPictureLeft.load(uri.toString()){
//                crossfade(true)
//            }
//        }
//    }
}