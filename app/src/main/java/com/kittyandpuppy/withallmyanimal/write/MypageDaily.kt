package com.kittyandpuppy.withallmyanimal.write

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageDailyBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class MypageDaily : AppCompatActivity() {

    private val binding : ActivityMypageDailyBinding by lazy { ActivityMypageDailyBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypageDailySave.setOnClickListener {
            val title = binding.etvMypageDailyTitle.text.toString()
            val tag = binding.etvMypageDailyTag.text.toString()
            val content = binding.etvMypageDaily.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(Daily(title, tag, content, uid, time))

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}