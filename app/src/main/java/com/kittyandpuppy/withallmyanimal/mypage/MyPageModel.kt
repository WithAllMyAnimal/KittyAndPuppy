package com.kittyandpuppy.withallmyanimal.mypage

data class LikesListModel(
    val key : String? = "",
    val uid : String? = "",
    val image : String? = "",
    val id : String? = "",
    val title : String? = "",
    val date : String? = ""
)

data class ListModel (
    val key : String? = "",
    val uid : String? = "",
    val image : String? = ""
)
