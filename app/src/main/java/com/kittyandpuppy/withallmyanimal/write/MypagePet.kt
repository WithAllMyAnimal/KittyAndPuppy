package com.kittyandpuppy.withallmyanimal.write

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypagePetBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class MypagePet : AppCompatActivity() {

    private val binding : ActivityMypagePetBinding by lazy { ActivityMypagePetBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypagePetSave.setOnClickListener {
            val title = binding.etvMypagePetTitle.text.toString()
            val name = binding.etvMypagePetSupplies.toString()
            val price = binding.etvMypagePetPrice.text.toString()
            val satisfaction = binding.ratMypagePetStar.toString()
            val caution = binding.etvMypagePetCaution.text.toString()
            val tag = binding.etvMypagePetTag.text.toString()
            val content = binding.etvMypagePetReview.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(Pet("Pet", caution, content, name, price, satisfaction, tag, time, title, uid))

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}