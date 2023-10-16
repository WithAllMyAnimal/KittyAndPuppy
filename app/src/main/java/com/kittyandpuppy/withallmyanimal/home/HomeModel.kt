package com.kittyandpuppy.withallmyanimal.home

data class HomeModel(
    val image : String? = "",
    val id : String? = "",
    val tag : String? = "",
    val likes : Int? = 0,
    val comments : Int? = 0
)