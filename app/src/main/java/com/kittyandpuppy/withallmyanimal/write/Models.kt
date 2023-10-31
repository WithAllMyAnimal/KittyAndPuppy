package com.kittyandpuppy.withallmyanimal.write
open class BaseModel(
    open val category: String = "",
    open val content: String = "",
    open var key : String = "",
    open val tags: List<String> = listOf(),
    open val time: String = "",
    open val title: String = "",
    open var uid : String = "",
    open var likesCount: Int = 0,
    open var commentsCount: Int = 0,
    open val animalAndCategory: String = ""
)

data class Behavior(
    val review: String = "",
    override var uid : String = "",
    override val content: String = "",
    override val tags: List<String> = emptyList(),
    override val time: String = "",
    override val title: String = "",
    override val animalAndCategory: String = ""
) : BaseModel(category = "Behavior")

data class Daily(
    override val content: String = "",
    override var uid : String = "",
    override val tags: List<String> = emptyList(),
    override val time: String = "",
    override val title: String = "",
    override val animalAndCategory: String = ""
) : BaseModel(category = "Daily")

data class Hospital(
    val date: String = "",
    val location: String = "",
    val price: String = "",
    val disease : String = "",
    override val content: String = "",
    override var uid : String = "",
    override val tags: List<String> = emptyList(),
    override val time: String = "",
    override val title: String = "",
    override val animalAndCategory: String = ""
) : BaseModel(category = "Hospital")

data class Pet(
    val caution: String = "",
    val name: String = "",
    val price: String = "",
    val satisfaction: String = "",
    override val content: String = "",
    override var uid : String = "",
    override val tags: List<String> = emptyList(),
    override val time: String = "",
    override val title: String = "",
    override val animalAndCategory: String = ""
) : BaseModel(category = "Pet")
