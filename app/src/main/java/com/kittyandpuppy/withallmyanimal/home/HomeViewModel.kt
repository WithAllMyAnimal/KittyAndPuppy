package com.kittyandpuppy.withallmyanimal.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kittyandpuppy.withallmyanimal.write.BaseModel

class HomeViewModel : ViewModel() {

    private val _boardList = MutableLiveData<List<BaseModel>>()
    val boardList: LiveData<List<BaseModel>> = _boardList

    fun getImageUrl(key: String): LiveData<String> {
        val imageUrlLiveData = MutableLiveData<String>()
        val imageRef = FirebaseDatabase.getInstance().getReference("${key}.png")
        imageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                imageUrlLiveData.value = url ?: ""
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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