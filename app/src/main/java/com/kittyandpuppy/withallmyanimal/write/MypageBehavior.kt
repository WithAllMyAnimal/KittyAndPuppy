package com.kittyandpuppy.withallmyanimal.write

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils

class MypageBehavior : AppCompatActivity() {

    private val binding: ActivityMypageBehaviorBinding by lazy {
        ActivityMypageBehaviorBinding.inflate(
            layoutInflater
        )
    }

    private val PICK_IMAGE_REQUEST = 0
    private val PERMISSION_REQUEST_CODE = 1

    private var isImageUpload = false
    private var tagListBehavior = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMypageBehaviorSave.setOnClickListener {
            val title = binding.etvMypageBehaviorTitle.text.toString()
            val content = binding.etvMypageBehavior.text.toString()
            val tags = tagListBehavior.toList()
            val review = binding.etvMypageBehaviorReview.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(Behavior("Behavior", content, review, tags, time, title, uid))

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()

            if (isImageUpload) {
                ImageUtils.imageUpload(this, binding.ivMypageBehaviorPictureLeft, key)
            }
            finish()
        }

        binding.ivMypageBehaviorPictureLeft.setOnClickListener {
            isImageUpload = true
            ImageUtils.openGallery(this, PICK_IMAGE_REQUEST)
        }
        binding.btnMypageBehaviorBack.setOnClickListener {
            finish()
        }

        binding.btnBehaviorAdd.setOnClickListener {
            val chipName = binding.etvMypageBehaviorTag.text.toString()
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
                        val typeface: Typeface? = ResourcesCompat.getFont(this@MypageBehavior, R.font.cafe24)
                        this.typeface = typeface
                        tagListBehavior.add(chipName)
                    })
                    Toast.makeText(this, "태그가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    // chip이 추가되면 입력창 초기화시키는 부분
                    binding.etvMypageBehaviorTag.setText("")
                }
            } else {
                Toast.makeText(this, "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여된 경우 갤러리 열기
                ImageUtils.openGallery(this, PICK_IMAGE_REQUEST)
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
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            binding.ivMypageBehaviorPictureLeft.setImageURI(data.data)
        }
    }
}