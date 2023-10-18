package com.kittyandpuppy.withallmyanimal.write

data class Behavior (
    val category : String = "Behavior",
    val title : String? = "",
    val content : String? = "",
    val tag : String? = "",
    val review : String? = "",
    val uid : String? = "",
    val time : String? = "",
)
data class Daily (
    val category : String = "Daily",
    val title : String? = "",
    val tag : String? = "",
    val content : String? = "",
    val uid : String? = "",
    val time : String? = ""
)
data class Hospital (
    val category : String = "Hospital",
    val title : String? = "",
    val date : String? = "",
    val price : String? = "",
    val location : String? = "",
    val tag : String? = "",
    val content : String? = "",
    val uid : String? = "",
    val time : String? = ""
)
data class Pet (
    val category : String = "Pet",
    val title : String? = "",
    val name : String? = "",
    val price : String? = "",
    val satisfaction : String? = "",
    val caution : String? = "",
    val tag : String? = "",
    val content : String? = "",
    val uid : String? = "",
    val time : String? = ""
)