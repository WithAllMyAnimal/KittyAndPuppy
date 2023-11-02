package com.kittyandpuppy.withallmyanimal.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object {
        private val database = Firebase.database

        val users = database.getReference("users")
        val writeCategory = database.getReference("writeCategory")
        val likesCountRef = database.getReference("likescCount")
        val boardRef = database.getReference("board")
        val commentRef = database.getReference("comment")
    }
}