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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageHospitalBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils
import kotlinx.coroutines.launch
import java.text.DecimalFormat

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

    private var tagListHospital = mutableListOf<String>()
    private var isImageUpload = false
    private var currentPostKey: String? = null
    private var imageUri: Uri? = null
    private var currentImageUri: Uri? = null

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
            imageUri = result.data?.data
            binding.ivMypageHospitalPictureLeft.setImageURI(result.data?.data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        currentPostKey = intent.getStringExtra("key")
        if (currentPostKey != null) {
            loadData(currentPostKey!!)
        }

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
                        Log.d("날짜", date)
                        val price = binding.etvMypageHospitalExpense.text.toString()
                        val location = binding.spMypageHospital.text.toString()
                        val tags = tagListHospital.toList()
                        val content = binding.etvMypageHospitalReview.text.toString()
                        val disease = binding.etvMypageHospitalCheckup.text.toString()
                        val uidAndCategory = "${uid}병원"

                        val key = currentPostKey ?: FBRef.boardRef.push().key.toString()

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
                            uidAndCategory = uidAndCategory,
                            key = key,
                            localUrl = if (!isImageUpload && currentImageUri != null) currentImageUri.toString() else imageUri.toString()
                        )
                        FBRef.boardRef
                            .child(key)
                            .setValue(hospitalData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@MypageHospital,
                                        "저장되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (!isImageUpload && currentImageUri != null) {
                                        lifecycleScope.launch {
                                            ImageUtils.imageUpload(
                                                this@MypageHospital,
                                                currentImageUri!!,
                                                key
                                            ).let { uploadSuccess ->
                                                if (uploadSuccess) {
                                                    val resultIntent = Intent().apply {
                                                        putExtra("postAdded", true)
                                                        putExtra("addedPostUid", uid)
                                                        putExtra("addedPostKey", key)
                                                        putExtra("imageUri", currentImageUri.toString())
                                                        putExtra("title", title)
                                                        putExtra("date", date)
                                                        putExtra("disease", disease)
                                                        putExtra("location", location)
                                                        putExtra("price", price)
                                                        putExtra("content", content)
                                                    }
                                                    setResult(Activity.RESULT_OK, resultIntent)
                                                    finish()
                                                }
                                            }
                                        }
                                    } else if (isImageUpload && imageUri != null) {
                                        lifecycleScope.launch {
                                            ImageUtils.imageUpload(
                                                this@MypageHospital,
                                                imageUri!!,
                                                key
                                            ).let { uploadSuccess ->
                                                if (uploadSuccess) {
                                                    val resultIntent = Intent().apply {
                                                        putExtra("postAdded", true)
                                                        putExtra("addedPostUid", uid)
                                                        putExtra("addedPostKey", key)
                                                        putExtra(
                                                            "imageUri",
                                                            imageUri.toString()
                                                        )
                                                        putExtra("title", title)
                                                        putExtra("date", date)
                                                        putExtra("disease", disease)
                                                        putExtra("location", location)
                                                        putExtra("price", price)
                                                        putExtra("content", content)
                                                    }
                                                    setResult(Activity.RESULT_OK, resultIntent)
                                                } else {
                                                    Toast.makeText(
                                                        this@MypageHospital,
                                                        "이미지 업로드 실패",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                finish()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@MypageHospital,
                                            "새 이미지가 선택되지 않았습니다",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.btnMypageHospitalSave.isEnabled
                                    }
                                } else {
                                    Toast.makeText(
                                        this@MypageHospital,
                                        "저장 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
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

        binding.etvMypageHospitalExpense.addTextChangedListener(object : TextWatcher {
            private var current: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                current = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                if (s.toString() != current) {
                    binding.etvMypageHospitalExpense.removeTextChangedListener(this)

                    if (cleanString.isNotEmpty()) {
                        val formatted = try {
                            val number = cleanString.toLong()
                            val formatter = DecimalFormat("#,###")
                            val formattedNumber = formatter.format(number)
                            val result = "$formattedNumber 원"
                            result
                        } catch (e: NumberFormatException) {
                            cleanString
                        }
                        binding.etvMypageHospitalExpense.setText(formatted)
                        binding.etvMypageHospitalExpense.setSelection(formatted.length - 2)
                    } else {
                        binding.etvMypageHospitalExpense.text = null
                    }

                    binding.etvMypageHospitalExpense.addTextChangedListener(this)
                }
            }
        })

        binding.btnDailyAdd.setOnClickListener {
            val chipName = binding.etvMypageHospitalTag.text.toString().trim()

            if (chipName.isNotBlank()) {
                // 태그 제한 개수 설정
                val maxChips = 3
                if (binding.chipGroup.childCount >= maxChips) {
                    Toast.makeText(this, "최대 $maxChips 개의 태그만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val isDuplicate = tagListHospital.any { it.equals(chipName, ignoreCase = true) }
                if (isDuplicate) {
                    Toast.makeText(this, "중복된 태그가 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.chipGroup.addView(Chip(this).apply {
                        text = chipName
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            binding.chipGroup.removeView(this)
                            tagListHospital.remove(chipName)
                        }
                        chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                        val typeface: Typeface? =
                            ResourcesCompat.getFont(this@MypageHospital, R.font.cafe24)
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
    private fun loadData(postKey: String) {
        FBRef.boardRef.child(postKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val behaviorData = snapshot.getValue(Hospital::class.java)
                behaviorData?.let {
                    binding.etvMypageHospitalTitle.setText(it.title)
                    binding.etvMypageHospitalReview.setText(it.content)
                    binding.etvMypageHospitalExpense.setText(it.price)
                    binding.etvMypageHospitalCheckup.setText(it.disease)
                    binding.spMypageHospital.setText(it.location)
                    binding.edtMypageHospital.setText(it.date)
                    binding.ivMypageHospitalPictureLeft.load(it.imageUrl)
                    currentImageUri = it.localUrl.toUri()
                    it.tags.forEach { tag ->
                        addChip(tag)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MypageHospital, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
    private fun addChip(chipName: String) {
        binding.chipGroup.addView(Chip(this).apply {
            text = chipName
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipGroup.removeView(this)
                tagListHospital.remove(chipName)
            }
            chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
            val typeface: Typeface? =
                ResourcesCompat.getFont(this@MypageHospital, R.font.cafe24)
            this.typeface = typeface
        })
        tagListHospital.add(chipName)
    }
}