package com.kittyandpuppy.withallmyanimal.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class HomeViewModel : ViewModel() {

    private val _boardList = MutableLiveData<List<BaseModel>>()
    val boardList: LiveData<List<BaseModel>> = _boardList

    fun getImageUrl(key: String): LiveData<String> {
        val imageUrlLiveData = MutableLiveData<String>()

        val storageReference = FirebaseStorage.getInstance().reference.child("$key.png")

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            imageUrlLiveData.value = uri.toString()
        }.addOnFailureListener {
            imageUrlLiveData.value = ""
        }

        return imageUrlLiveData
    }
}


//    init {
//        loadBoardList()
//    }
//
//    private fun loadBoardList() {
//        val databaseRef = FirebaseDatabase.getInstance().getReference("board")
//        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val items = snapshot.children.map { child ->
//                    child.getValue(BaseModel::class.java)!!
//                }
//                _boardList.value = items
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//    }