package com.kittyandpuppy.withallmyanimal.write

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageHospitalBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class MypageHospital : AppCompatActivity() {

    private val binding  : ActivityMypageHospitalBinding by lazy { ActivityMypageHospitalBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypageHospitalSave.setOnClickListener {
            val title = binding.etvMypageHospitalTitle.text.toString()
            val date = binding.dpMypageHospital.toString()
            val price = binding.etvMypageHospitalExpense.text.toString()
            val location = binding.etvMypageHospitalLocation.text.toString()
            val tag = binding.etvMypageHospitalTag.text.toString()
            val content = binding.etvMypageHospitalReview.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(Hospital("Hospital", content, date, location, price, tag, time, title, uid))

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}