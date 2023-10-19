package com.kittyandpuppy.withallmyanimal.firebase

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun openGallery(activity: Activity, requestCode: Int) {

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO)
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            // 권한 요청
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                requestCode
            )
        } else {
            // 이미 권한이 있는 경우
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun imageUpload(activity: Activity, imageView: ImageView, key: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val animalsRef = storageRef.child("$key.png")

        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = animalsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(activity, "이미지 업로드에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            Toast.makeText(activity, "이미지 업로드에 성공하였습니다!", Toast.LENGTH_SHORT).show()
        }
    }
}