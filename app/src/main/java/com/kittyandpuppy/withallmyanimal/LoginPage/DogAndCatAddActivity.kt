package com.kittyandpuppy.withallmyanimal.LoginPage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDogAndCatAddBinding
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils

class DogAndCatAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDogAndCatAddBinding
    private lateinit var userRef: DatabaseReference
    private var selectedImageUri: Uri? = null

    val storage = Firebase.storage
    val storageRef = storage.reference

    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // 권한이 부여된 경우 갤러리 열기
            val intent = ImageUtils.createGalleryIntent()
            pickImageLauncher.launch(intent)
        } else {
            android.app.AlertDialog.Builder(this)
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
            binding.ivCircleMy.setImageURI(result.data?.data)
            selectedImageUri = result.data?.data
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogAndCatAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        val dogCatAdapter = ArrayAdapter.createFromResource(
            this, R.array.dogandcat, android.R.layout.simple_spinner_item
        )
        dogCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDogAndCatAddDogcat.adapter = dogCatAdapter

        val petTypeAdapter = ArrayAdapter.createFromResource(
            this, R.array.dogbreed, android.R.layout.simple_spinner_item
        )
        petTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDogAndCatAddPetType.adapter = petTypeAdapter

        binding.spDogAndCatAddDogcat.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = binding.spDogAndCatAddDogcat.selectedItem.toString()
                    if (selectedItem == "강아지") {
                        val dogAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.dogbreed,
                            android.R.layout.simple_spinner_item
                        )
                        dogAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spDogAndCatAddPetType.adapter = dogAdapter
                    } else if (selectedItem == "고양이") {
                        val catAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.catbreed,
                            android.R.layout.simple_spinner_item
                        )
                        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spDogAndCatAddPetType.adapter = catAdapter
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.btnLoginSignup.setOnClickListener {
            checkDuplicateId()
        }
        binding.btnDogAndCatAddSave.setOnClickListener {
            saveUserInfoToDatabase()
        }

        binding.ivCircleMy.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
                // 권한이 이미 부여되었을 경우
                val intent = ImageUtils.createGalleryIntent()
                pickImageLauncher.launch(intent)
            } else {
                // 권한이 부여되지 않았을 경우 권한 요청
                requestPermissionLauncher.launch(storagePermission)
            }
        }
    }

    private fun checkDuplicateId() {
        val userIdname = binding.etDogAndCatAddNick.text.toString()

        userRef.orderByChild("profile/userIdname").equalTo(userIdname)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@DogAndCatAddActivity, "중복된 아이디입니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@DogAndCatAddActivity,
                            "사용 가능한 아이디입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    private fun saveUserInfoToDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userIdname = binding.etDogAndCatAddNick.text.toString()
            val petName = binding.etDogAndCatAddPetname.text.toString()
            val birth = binding.etDogAndCatAddBirth.text.toString()
            val dogCat = binding.spDogAndCatAddDogcat.selectedItem.toString()
            val breed = binding.spDogAndCatAddPetType.selectedItem.toString()
//            val statusMessage = binding.etFeel.text.toString()

            if (selectedImageUri != null) {
                val imageKey = userRef.push().key
                val imageRef = storageRef.child("profileImages").child("$userId.png")
                val uploadTask = imageRef.putFile(selectedImageUri!!)

                uploadTask.addOnFailureListener {
                    Toast.makeText(
                        this@DogAndCatAddActivity,
                        "이미지 업로드에 실패하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnSuccessListener { _ ->

                    val userInfo = mapOf(
                        "userIdname" to userIdname,
                        "petName" to petName,
                        "birth" to birth,
                        "dogcat" to dogCat,
                        "breed" to breed,
//                        "statusMessage" to statusMessage,
                        "profileImage" to imageKey
                    )

                    userRef.child(userId).child("profile").setValue(userInfo)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(
                                    this@DogAndCatAddActivity,
                                    MainActivity::class.java
                                )
                                intent.putExtra("selectedImage", selectedImageUri)
                                startActivity(intent)
                                finish()

                                val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("imageKey", imageKey)
                                editor.apply()
                            } else {
                                Toast.makeText(
                                    this@DogAndCatAddActivity,
                                    "데이터 저장에 실패하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this@DogAndCatAddActivity, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@DogAndCatAddActivity, "유저 ID 없음", Toast.LENGTH_SHORT).show()
        }
    }
}