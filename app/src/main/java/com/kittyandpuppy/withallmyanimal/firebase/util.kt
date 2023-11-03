package com.kittyandpuppy.withallmyanimal.firebase

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

object ImageUtils {
    fun createGalleryIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        return intent
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun imageUpload(activity: Activity, imageView: ImageView, key: String) = suspendCancellableCoroutine<Boolean> { con ->
        val storageRef = Firebase.storage.reference.child("$key.png")
        val metadata = storageMetadata {
            contentType = "image/jpeg"
            setCustomMetadata("updated", System.currentTimeMillis().toString())
        }

        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storageRef.putBytes(data, metadata)

        uploadTask.addOnFailureListener {
            con.resumeWith(Result.failure(it))
            Toast.makeText(activity, "이미지 업로드에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            val imageUpdateRef = FirebaseDatabase.getInstance().getReference("imageUpdates/$key")
            imageUpdateRef.setValue(System.currentTimeMillis())
                .addOnSuccessListener {
                    Log.d("ImageUpload", "Image update time recorded in Realtime Database.")
                    con.resumeWith(Result.success(true))
                }
                .addOnFailureListener { databaseError ->
                    Log.w("ImageUpload", "Failed to record image update time in Realtime Database.", databaseError)
                    con.resumeWith(Result.failure(databaseError))
                }
            Toast.makeText(activity, "이미지 업로드에 성공하였습니다!", Toast.LENGTH_SHORT).show()
        }
    }
}