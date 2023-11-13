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
import coil.transform.CircleCropTransformation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
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
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDetailPetBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.mypage.MypageOtherUsers
import com.kittyandpuppy.withallmyanimal.util.Constants
import com.kittyandpuppy.withallmyanimal.write.MypagePet
import com.kittyandpuppy.withallmyanimal.write.Pet

class DetailPetActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    private val binding: ActivityDetailPetBinding by lazy {
        ActivityDetailPetBinding.inflate(layoutInflater)
    }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri") ?: return@registerForActivityResult
                val title = result.data?.getStringExtra("title") ?: return@registerForActivityResult
                val name = result.data?.getStringExtra("name") ?: return@registerForActivityResult
                val price = result.data?.getStringExtra("price") ?: return@registerForActivityResult
                val satisfaction = result.data?.getStringExtra("satisfaction") ?: return@registerForActivityResult
                val caution = result.data?.getStringExtra("caution") ?: return@registerForActivityResult
                val content = result.data?.getStringExtra("content") ?: return@registerForActivityResult
                loadUpdatedImage(imageUri, title, name, price, satisfaction, caution, content)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") ?: return
        val key = intent.getStringExtra("key") ?: return
        val category = intent.getStringExtra("category") ?: return
        Log.d("DetailPetActivity", "Received key: $key, category: $category")

        if (uid == FBAuth.getUid()) {
            binding.ivDetailEdit.isVisible = true
            binding.ivDetailDelete.isVisible = true
        }

        val userId = Firebase.auth.currentUser?.uid
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("profileImages").child("$userId.png")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            binding.ivCircleMy.load(uri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }.addOnFailureListener {
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
                            Toast.makeText(this, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            alertDialog.findViewById<Button>(R.id.btn_settinglogout_cancelbutton)?.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        binding.ivDetailEdit.setOnClickListener {
            val intent = Intent(this, MypagePet::class.java)
            intent.putExtra("key", key)
            startForResult.launch(intent)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("board").child(key)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Pet::class.java) ?: return

                binding.tvDetailPetTitle.text = post.title
                binding.tvDetailDate.text = post.time
                binding.tvDetailPetNameContents.text = post.name
                binding.tvDetailPetPriceContents.text = post.price
                if (post.satisfaction.isNotBlank()) {
                    binding.ratDetailPetStar.rating = post.satisfaction.toFloat()
                }
                binding.tvDetailPetCautionContents.text = post.caution
                binding.tvDetailPetReviewContents.text = post.content
                binding.ivDetailPetPictureLeft.load(post.imageUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DetailPetActivity", "Failed to read post data", error.toException())
            }
        })

        val storageProfile = Firebase.storage.reference.child("profileImages")
            .child("$uid.png")
        storageProfile.downloadUrl.addOnSuccessListener { uri ->
            binding.ivDetailPetProfile.load(uri.toString()) {
                crossfade(true)
            }
        }
        binding.ivDetailPetProfile.setOnClickListener {
            val intent = Intent(this, MypageOtherUsers::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        FBRef.users.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.child("profile").child("userIdname").value.toString()
                    binding.tvDetailPetNickname.text = userId
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailPetActivity", "Failed to read userID", error.toException())
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
        binding.btnDetailPetBack.setOnClickListener {
            finish()
        }
    }
    private fun loadUpdatedImage(imageUri : String, title : String, name : String, price : String, satisfaction : String, caution : String, content : String) {
        binding.ivDetailPetPictureLeft.load(imageUri)
        binding.tvDetailPetTitle.text = title
        binding.tvDetailPetNameContents.text = name
        binding.tvDetailPetPriceContents.text = price
        binding.ratDetailPetStar.rating = satisfaction.toFloat()
        binding.tvDetailPetCautionContents.text = caution
        binding.tvDetailPetReviewContents.text = content
    }
}