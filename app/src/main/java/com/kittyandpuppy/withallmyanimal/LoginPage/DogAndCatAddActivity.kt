package com.kittyandpuppy.withallmyanimal.LoginPage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kittyandpuppy.withallmyanimal.MainActivity
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDogAndCatAddBinding

class DogAndCatAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDogAndCatAddBinding
    private lateinit var userRef: DatabaseReference

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

        binding.btnDogAndCatAddSave.setOnClickListener {
            saveUserInfoToDatabase()
        }
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
                    "petName" to petName, "birth" to birth,
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