package com.kittyandpuppy.withallmyanimal.LoginPage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityDogAndCatAddBinding

class DogAndCatAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDogAndCatAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogAndCatAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


        binding.spDogAndCatAddDogcat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = binding.spDogAndCatAddDogcat.selectedItem.toString()
                if (selectedItem == "강아지") {
                    val dogAdapter = ArrayAdapter.createFromResource(
                        applicationContext, R.array.dogbreed, android.R.layout.simple_spinner_item
                    )
                    dogAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spDogAndCatAddPetType.adapter = dogAdapter
                } else if (selectedItem == "고양이") {
                    val catAdapter = ArrayAdapter.createFromResource(
                        applicationContext, R.array.catbreed, android.R.layout.simple_spinner_item
                    )
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spDogAndCatAddPetType.adapter = catAdapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}