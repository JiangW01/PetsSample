package com.pets.sample.model

data class PetModel(
    val id:String,
    val name:String,
    val image:String,
    val useCount :Int= 0,
    val likeCount :Int= 0,
    val zip :String? = null
)