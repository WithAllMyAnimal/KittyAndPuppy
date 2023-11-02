package com.kittyandpuppy.withallmyanimal.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

fun viewToBitmap(view : View) : Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}
fun getSaveFileName() : String {
    val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
    val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    return "$folder/$fileName.jpg"
}
fun bitmapFileSave(bitmap : Bitmap, path : String) {
    val fos : FileOutputStream
    try {
        fos = FileOutputStream(File(path))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
    } catch (e : IOException) {
        e.printStackTrace()
    }
}
fun viewSave(view: View) {
    val bitmap = viewToBitmap(view)
    val filePath = getSaveFileName()
    bitmapFileSave(bitmap, filePath)
}