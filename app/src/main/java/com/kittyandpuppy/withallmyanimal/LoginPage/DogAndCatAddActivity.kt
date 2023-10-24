package com.kittyandpuppy.withallmyanimal.LoginPage

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDogAndCatAddBinding
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils

class DogAndCatAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDogAndCatAddBinding
    private lateinit var userRef: DatabaseReference
    private val GALLERY_REQUEST_CODE = 1

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
            ImageUtils.openGallery(this, GALLERY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data?.data

                if (selectedImage != null) {
                    binding.ivCircleMy.setImageURI(selectedImage)

                    ImageUtils.imageUpload(this, binding.ivCircleMy, "key")
                }
            }
        }
    }

    private fun checkDuplicateId() {
        val userIdname = binding.etDogAndCatAddNick.text.toString()

        userRef.orderByChild("profile/userIdname").equalTo(userIdname).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(this@DogAndCatAddActivity, "중복된 아이디입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DogAndCatAddActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
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
            val statusMessage = binding.etFeel.text.toString()

            userRef.child(
                userId
            ).child("profile").setValue(
                mapOf(
                    "userIdname" to userIdname,
                    "petName" to petName,
                    "birth" to birth,
                    "dogcat" to dogCat,
                    "breed" to breed,
                    "statusMessage" to statusMessage
                )
            )
            binding.btnDogAndCatAddSave.setOnClickListener {
                saveUserInfoToDatabase()

                startActivity(Intent(this@DogAndCatAddActivity, MainActivity::class.java))

                finish()
            }
        } else {
            Toast.makeText(this@DogAndCatAddActivity, "유저 ID 없음", Toast.LENGTH_SHORT).show()
        }
    }
}