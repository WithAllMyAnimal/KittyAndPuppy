package com.kittyandpuppy.withallmyanimal.write

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils
import kotlinx.coroutines.launch

class MypageBehavior : AppCompatActivity() {

    private val binding: ActivityMypageBehaviorBinding by lazy {
        ActivityMypageBehaviorBinding.inflate(
            layoutInflater
        )
    }

    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private var isImageUpload = false
    private var tagListBehavior = mutableListOf<String>()
    private var currentPostKey: String? = null
    private var imageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 부여된 경우 갤러리 열기
                val intent = ImageUtils.createGalleryIntent()
                pickImageLauncher.launch(intent)
            } else {
                AlertDialog.Builder(this)
                    .setMessage("갤러리 접근 권한이 거부되었습니다. 설정에서 권한을 허용해주세요.")
                    .setPositiveButton("설정으로 이동") { _, _ ->
                        // 설정 화면으로 이동하여 권한을 허용할 수 있도록 유도
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("취소") { _, _ -> }
                    .show()
            }
        }
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imageUri = result.data?.data
                binding.ivMypageBehaviorPictureLeft.setImageURI(result.data?.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        currentPostKey = intent.getStringExtra("key")
        if (currentPostKey != null) {
            loadData(currentPostKey!!)
        }

        binding.btnMypageBehaviorSave.setOnClickListener {
            val uid = FBAuth.getUid()
            FBRef.users.child(uid).child("profile").child("dogcat")
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val dogcatValue = snapshot.getValue(String::class.java)
                        if (dogcatValue != null) {
                            val animalAndCategory = "${dogcatValue}행동"

                            val title = binding.etvMypageBehaviorTitle.text.toString()
                            val content = binding.etvMypageBehavior.text.toString()
                            val tags = tagListBehavior.toList()
                            val review = binding.etvMypageBehaviorReview.text.toString()
                            val time = FBAuth.getTime()
                            val uidAndCategory = "${uid}행동"

                            val key = currentPostKey ?: FBRef.boardRef.push().key.toString()

                            val behaviorData = Behavior(
                                review = review,
                                content = content,
                                tags = tags,
                                time = time,
                                title = title,
                                animalAndCategory = animalAndCategory,
                                uid = uid,
                                animal = dogcatValue,
                                uidAndCategory = uidAndCategory,
                                key = key
                            )

                            FBRef.boardRef
                                .child(key)
                                .setValue(behaviorData)
                                .addOnCompleteListener {
                                    Toast.makeText(
                                        this@MypageBehavior,
                                        "저장되었습니다.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    lifecycleScope.launch {

                                        if (isImageUpload && imageUri != null) {
                                            ImageUtils.imageUpload(
                                                this@MypageBehavior,
                                                imageUri ?: Uri.EMPTY,
                                                key
                                            )
                                        }
                                        val resultIntent = Intent().putExtra("postAdded", true)
                                        resultIntent.putExtra("addedPostUid", uid)
                                        resultIntent.putExtra("addedPostKey", key)
                                        resultIntent.putExtra("imageUri", imageUri)
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        Log.d("mmm", "성공33")
                                        finish()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@MypageBehavior, "저장 실패", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        binding.ivMypageBehaviorPictureLeft.setOnClickListener {
            isImageUpload = true
            if (ContextCompat.checkSelfPermission(
                    this,
                    storagePermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 이미 부여되었을 경우
                val intent = ImageUtils.createGalleryIntent()
                pickImageLauncher.launch(intent)
            } else {
                // 권한이 부여되지 않았을 경우 권한 요청
                requestPermissionLauncher.launch(storagePermission)
            }
        }
        binding.btnMypageBehaviorBack.setOnClickListener {
            finish()
        }

        binding.btnBehaviorAdd.setOnClickListener {
            val chipName = binding.etvMypageBehaviorTag.text.toString()
            if (chipName.isNotBlank()) {
                // 태그 제한 개수 설정
                val maxChips = 3
                if (binding.chipGroup.childCount >= maxChips) {
                    Toast.makeText(this, "최대 $maxChips 개의 태그만 추가할 수 있습니다.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                var isDuplicate = false
                for (i in 0 until binding.chipGroup.childCount) {
                    val chip = binding.chipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == chipName) {
                        isDuplicate = true
                        break
                    }
                }

                if (isDuplicate) {
                    Toast.makeText(this, "중복된 태그가 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.chipGroup.addView(Chip(this).apply {
                        text = chipName
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { binding.chipGroup.removeView(this)
                            // 이 부분이 없어서 오류가 났었다.
                            tagListBehavior.remove(chipName)
                        }
                        chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                        val typeface: Typeface? =
                            ResourcesCompat.getFont(this@MypageBehavior, R.font.cafe24)
                        this.typeface = typeface
                        tagListBehavior.add(chipName)
                    })
                    Toast.makeText(this, "태그가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    // chip이 추가되면 입력창 초기화시키는 부분
                    binding.etvMypageBehaviorTag.setText("")
                }
            } else {
                Toast.makeText(this, "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadData(postKey: String) {
        FBRef.boardRef.child(postKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val behaviorData = snapshot.getValue(Behavior::class.java)
                behaviorData?.let {
                    binding.etvMypageBehaviorTitle.setText(it.title)
                    binding.etvMypageBehavior.setText(it.content)
                    binding.etvMypageBehaviorReview.setText(it.review)
                    it.tags.forEach { tag ->
                        addChip(tag)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MypageBehavior, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        val storageImage = Firebase.storage.reference.child("${postKey}.png")
        storageImage.downloadUrl.addOnSuccessListener { uri ->
            Log.d("JINA", "loadUpdatedImage: ${uri.toString()}")
            binding.ivMypageBehaviorPictureLeft.load(uri.toString()){
                crossfade(true)
            }
        }
    }

    private fun addChip(chipName: String) {
        var isDuplicate = false
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            if (chip.text.toString() == chipName) {
                isDuplicate = true
                break
            }
        }

        if (!isDuplicate) {
            binding.chipGroup.addView(Chip(this).apply {
                text = chipName
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    binding.chipGroup.removeView(this)
                    tagListBehavior.remove(chipName)
                }
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                val typeface: Typeface? =
                    ResourcesCompat.getFont(this@MypageBehavior, R.font.cafe24)
                this.typeface = typeface
            })
            tagListBehavior.add(chipName)
        } else {
            Toast.makeText(this, "중복된 태그가 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}