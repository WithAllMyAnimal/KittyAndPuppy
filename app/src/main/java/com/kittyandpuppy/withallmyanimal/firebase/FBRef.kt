package com.kittyandpuppy.withallmyanimal.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object {
        private val database = Firebase.database

        val users = database.getReference("users")
        val myDiary = database.getReference("my_diary")
        val likesRef = database.getReference("likesList")
        val boardRef = database.getReference("board")
        val commentRef = database.getReference("comment")
    }
}