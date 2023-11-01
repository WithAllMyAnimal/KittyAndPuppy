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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageHospitalBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils

class MypageHospital : AppCompatActivity() {

    private val binding: ActivityMypageHospitalBinding by lazy {
        ActivityMypageHospitalBinding.inflate(
            layoutInflater
        )
    }

    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private var tagListHospital: MutableList<String> = mutableListOf()
    private var isImageUpload = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            binding.ivMypageHospitalPictureLeft.setImageURI(result.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypageHospitalSave.setOnClickListener {
            val uid = FBAuth.getUid()
            FBRef.users.child(uid).child("profile").child("dogcat").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dogcatValue = snapshot.getValue(String::class.java)
                    if (dogcatValue != null) {
                        val animalAndCategory = "${dogcatValue}병원"
                        val time = FBAuth.getTime()
                        val title = binding.etvMypageHospitalTitle.text.toString()
                        val date = binding.edtMypageHospital.text.toString()
                        val price = binding.etvMypageHospitalExpense.text.toString()
                        val location = binding.spMypageHospital.text.toString()
                        val tags = tagListHospital.joinToString(", ")
                        val content = binding.etvMypageHospitalReview.text.toString()
                        val disease = binding.etvMypageHospitalCheckup.text.toString()
                        val uidAndCategory = "${uid}병원"

                        val key = FBRef.boardRef.push().key.toString()

                        val hospitalData = Hospital(
                            date = date,
                            disease = disease,
                            location = location,
                            price = price,
                            content = content,
                            tags = tags,
                            time = time,
                            title = title,
                            animalAndCategory = animalAndCategory,
                            uid = uid,
                            animal = dogcatValue,
                            uidAndCategory = uidAndCategory
                        )
                        FBRef.boardRef
                            .child(key)
                            .setValue(hospitalData)

                        Toast.makeText(this@MypageHospital, "저장되었습니다.", Toast.LENGTH_SHORT).show()

                        if (isImageUpload) {
                            ImageUtils.imageUpload(this@MypageHospital, binding.ivMypageHospitalPictureLeft, key)
                        }
                        val resultIntent = Intent().putExtra("postAdded", true)
                        resultIntent.putExtra("addedPostUid", uid)
                        resultIntent.putExtra("addedPostKey", key)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        binding.ivMypageHospitalPictureLeft.setOnClickListener {
            isImageUpload = true
            if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
                val intent = ImageUtils.createGalleryIntent()
                pickImageLauncher.launch(intent)
            } else {
                requestPermissionLauncher.launch(storagePermission)
            }
        }
        binding.btnMypageHospitalBack.setOnClickListener {
            finish()
        }
        binding.etvMypageHospitalTag.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 != null && p0.contains(",")) {
                    binding.etvMypageHospitalTag.error = ",는 입력할 수 없습니다."
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.btnDailyAdd.setOnClickListener {
            val chipName = binding.etvMypageHospitalTag.text.toString()
            if (chipName.isNotBlank()) {
                // 태그 제한 개수 설정
                val maxChips = 3
                if (binding.chipGroup.childCount >= maxChips) {
                    Toast.makeText(this, "최대 $maxChips 개의 태그만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
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
                        setOnCloseIconClickListener { binding.chipGroup.removeView(this) }
                        chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                        val typeface: Typeface? = ResourcesCompat.getFont(this@MypageHospital, R.font.cafe24)
                        this.typeface = typeface
                        tagListHospital.add(chipName)
                    })
                    Toast.makeText(this, "태그가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    // chip이 추가되면 입력창 초기화시키는 부분
                    binding.etvMypageHospitalTag.setText("")
                }
            } else {
                Toast.makeText(this, "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}