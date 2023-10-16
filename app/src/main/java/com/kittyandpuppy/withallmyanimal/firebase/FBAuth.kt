package com.kittyandpuppy.withallmyanimal.firebase

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FBAuth {
    companion object {
        private lateinit var auth: FirebaseAuth

        fun getUid() : String {
            auth = FirebaseAuth.getInstance()
            return auth.currentUser?.uid.toString()
        }

        fun getTime(): String {
            val currentDate = Calendar.getInstance().time
            return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(currentDate)
        }
    }
}