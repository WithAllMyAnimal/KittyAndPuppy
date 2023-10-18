package com.kittyandpuppy.withallmyanimal.write

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypageBehaviorBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils
import java.io.ByteArrayOutputStream
import java.util.UUID

class MypageBehavior : AppCompatActivity() {

    private val binding: ActivityMypageBehaviorBinding by lazy {
        ActivityMypageBehaviorBinding.inflate(
            layoutInflater
        )
    }

    private val PICK_IMAGE_REQUEST = 0
    private val PERMISSION_REQUEST_CODE = 1

    private var isImageUpload = false

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
                .setValue(Behavior("Behavior", content, review, tag, time, title, uid))

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