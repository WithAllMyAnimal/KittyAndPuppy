package com.kittyandpuppy.withallmyanimal.write

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import coil.load
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef

class MypageBehavior : AppCompatActivity() {

    private lateinit var binding : ActivityMypageBehaviorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypageBehaviorSave.setOnClickListener {
            val title = binding.etvMypageBehaviorTitle.text.toString()
            val content = binding.etvMypageBehavior.text.toString()
            val tag = binding.etvMypageBehaviorTag.text.toString()
            val review = binding.etvMypageBehaviorReview.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(Behavior(title, content, tag, review, uid, time))

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }


    }
}