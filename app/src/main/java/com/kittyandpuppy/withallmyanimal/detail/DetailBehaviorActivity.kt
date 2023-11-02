package com.kittyandpuppy.withallmyanimal.detail

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.Manifest
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.comments.CommentsFragment
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.mypage.MypageOtherUsers
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.util.viewSave
import com.kittyandpuppy.withallmyanimal.write.Behavior


class DetailBehaviorActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
    }

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
            binding.ivDetailSave.isVisible = true
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

        binding.ivDetailSave.setOnClickListener {
            if (hasWritePermission()) {
                viewSave(binding.conDetailBehavior)
            } else {
                requestWritePermission()
            }
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
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
        val storageRef = Firebase.storage.reference.child("${key}.png")
        storageRef.downloadUrl.addOnSuccessListener { uri->
            binding.ivDetailBehaviorPictureLeft.load(uri.toString()){
                crossfade(true)
                allowHardware(false)
            }
        }
        val storageProfile = Firebase.storage.reference.child("profileImages")
            .child("$uid.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailBehaviorProfile.load(uri.toString()){
                crossfade(true)
                allowHardware(false)
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
                allowHardware(false)
            }
        }

        FBRef.users.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
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
    private fun hasWritePermission() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestWritePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_PERMISSIONS
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한이 부여된 경우 이미지 저장
                    viewSave(binding.conDetailBehavior)
                } else {
                    Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // 다른 'requestPermissions' 호출 처리
            }
        }
    }
    // viewSave, viewToBitmap, getSaveFileName, bitmapFileSave, saveBitmapUsingMediaStore 함수 구현...
}