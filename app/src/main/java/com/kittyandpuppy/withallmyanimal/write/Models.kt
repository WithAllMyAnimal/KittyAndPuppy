package com.kittyandpuppy.withallmyanimal.write

data class Behavior (
    val category : String = "Behavior",
    val content : String? = "",
    val review : String? = "",
    val tag : String? = "",
    val time : String? = "",
    val title : String? = "",
)
data class Daily (
    val category : String = "Daily",
    val content : String? = "",
    val tag : String? = "",
    val time : String? = "",
    val title : String? = "",
)
data class Hospital (
    val category : String = "Hospital",
    val content : String? = "",
    val date : String? = "",
    val location : String? = "",
    val price : String? = "",
    val tag : String? = "",
    val time : String? = "",
    val title : String? = "",
)
data class Pet (
    val category : String = "Pet",
    val caution : String? = "",
    val content : String? = "",
    val name : String? = "",
    val price : String? = "",
    val satisfaction : String? = "",
    val tag : String? = "",
    val time : String? = "",
    val title : String? = "",
)