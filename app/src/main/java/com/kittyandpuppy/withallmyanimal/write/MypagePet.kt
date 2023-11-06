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
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypagePetBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class MypagePet : AppCompatActivity() {

    private val binding : ActivityMypagePetBinding by lazy { ActivityMypagePetBinding.inflate(layoutInflater) }

    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private var isImageUpload = false
    private var tagListPet = mutableListOf<String>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val intent = ImageUtils.createGalleryIntent()
            pickImageLauncher.launch(intent)
        } else {
            AlertDialog.Builder(this)
                .setMessage("갤러리 접근 권한이 거부되었습니다. 설정에서 권한을 허용해주세요.")
                .setPositiveButton("설정으로 이동") { _, _ ->
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
            binding.ivMypagePetPictureLeft.setImageURI(result.data?.data)
        }
    }

    private var currentPostKey: String? = null
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        currentPostKey = intent.getStringExtra("key")
        if (currentPostKey != null) {
            loadData(currentPostKey!!)
        }

        binding.btnMypagePetSave.setOnClickListener {
            val uid = FBAuth.getUid()
            FBRef.users.child(uid).child("profile").child("dogcat").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dogcatValue = snapshot.getValue(String::class.java)
                    if (dogcatValue != null) {
                        val animalAndCategory = "${dogcatValue}펫용품"
                        val time = FBAuth.getTime()
                        val title = binding.etvMypagePetTitle.text.toString()
                        val name = binding.etvMypagePetSupplies.text.toString()
                        val price = binding.etvMypagePetPrice.text.toString()
                        val satisfaction = binding.ratMypagePetStar.rating.toLong().toString()
                        val caution = binding.etvMypagePetCaution.text.toString()
                        val tags = tagListPet.toList()
                        val content = binding.etvMypagePetReview.text.toString()
                        val uidAndCategory = "${uid}펫용품"

                        val key = currentPostKey ?: FBRef.boardRef.push().key.toString()

                        val petData = Pet(
                            caution = caution,
                            name = name,
                            price = price,
                            satisfaction = satisfaction,
                            content = content,
                            tags = tags,
                            time = time,
                            title = title,
                            animalAndCategory = animalAndCategory,
                            uid = uid,
                            animal = dogcatValue,
                            uidAndCategory = uidAndCategory,
                            imageUrl = imageUri.toString(),
                            key = key
                        )

                        FBRef.boardRef
                            .child(key)
                            .setValue(petData)
                            .addOnSuccessListener {
                                Toast.makeText(this@MypagePet, "저장되었습니다.", Toast.LENGTH_SHORT)
                                    .show()

                                lifecycleScope.launch {
                                    if (isImageUpload && imageUri != null) {
                                        ImageUtils.imageUpload(
                                            this@MypagePet,
                                            imageUri ?: Uri.EMPTY,
                                            key
                                        )
                                    }
                                    val resultIntent = Intent().putExtra("postAdded", true)
                                    resultIntent.putExtra("addedPostUid", uid)
                                    resultIntent.putExtra("addedPostKey", key)
                                    resultIntent.putExtra("imageUri", imageUri)
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MypagePet, "저장 실패", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        }
        binding.ivMypagePetPictureLeft.setOnClickListener {
            isImageUpload = true
            if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
                val intent = ImageUtils.createGalleryIntent()
                pickImageLauncher.launch(intent)
            } else {
                requestPermissionLauncher.launch(storagePermission)
            }
        }
        binding.btnMypagePetBack.setOnClickListener {
            finish()
        }

        binding.etvMypagePetPrice.addTextChangedListener(object : TextWatcher {
            private var current: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                current = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                if (s.toString() != current) {
                    binding.etvMypagePetPrice.removeTextChangedListener(this)

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
                        binding.etvMypagePetPrice.setText(formatted)
                        binding.etvMypagePetPrice.setSelection(formatted.length - 2)
                    } else {
                        binding.etvMypagePetPrice.text = null
                    }

                    binding.etvMypagePetPrice.addTextChangedListener(this)
                }
            }
        })


        binding.btnPetAdd.setOnClickListener {
            val chipName = binding.etvMypagePetTag.text.toString()
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
                        val typeface: Typeface? = ResourcesCompat.getFont(this@MypagePet, R.font.cafe24)
                        this.typeface = typeface
                        tagListPet.add(chipName)
                    })
                    Toast.makeText(this, "태그가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    // chip이 추가되면 입력창 초기화시키는 부분
                    binding.etvMypagePetTag.setText("")
                }
            } else {
                Toast.makeText(this, "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadData(postKey: String) {
        FBRef.boardRef.child(postKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val behaviorData = snapshot.getValue(Pet::class.java)
                behaviorData?.let {
                    binding.etvMypagePetTitle.setText(it.title)
                    binding.etvMypagePetReview.setText(it.content)
                    binding.etvMypagePetCaution.setText(it.caution)
                    binding.etvMypagePetPrice.setText(it.price)
                    binding.etvMypagePetSupplies.setText(it.name)
                    binding.ratMypagePetStar.rating = it.satisfaction.toFloat()
                    it.tags.forEach { tag ->
                        addChip(tag)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MypagePet, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        val storageImage = Firebase.storage.reference.child("${postKey}.png")
        storageImage.downloadUrl.addOnSuccessListener { uri ->
            binding.ivMypagePetPictureLeft.load(uri.toString()){
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
                    tagListPet.remove(chipName)
                }
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                val typeface: Typeface? =
                    ResourcesCompat.getFont(this@MypagePet, R.font.cafe24)
                this.typeface = typeface
            })
            tagListPet.add(chipName)
        } else {
            Toast.makeText(this, "중복된 태그가 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}