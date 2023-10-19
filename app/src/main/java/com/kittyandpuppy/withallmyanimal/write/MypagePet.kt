package com.kittyandpuppy.withallmyanimal.write

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.kittyandpuppy.withallmyanimal.R
import com.kittyandpuppy.withallmyanimal.databinding.ActivityMypagePetBinding
import com.kittyandpuppy.withallmyanimal.firebase.FBAuth
import com.kittyandpuppy.withallmyanimal.firebase.FBRef
import com.kittyandpuppy.withallmyanimal.firebase.ImageUtils

class MypagePet : AppCompatActivity() {

    private val binding : ActivityMypagePetBinding by lazy { ActivityMypagePetBinding.inflate(layoutInflater) }

    private val PICK_IMAGE_REQUEST = 0
    private val PERMISSION_REQUEST_CODE = 1

    private var isImageUpload = false
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

            if (isImageUpload) {
                ImageUtils.imageUpload(this, binding.ivMypagePetPictureLeft, key)
            }
            finish()
        }
        binding.ivMypagePetPictureLeft.setOnClickListener {
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
            binding.ivMypagePetPictureLeft.setImageURI(data.data)
        }
    }
}