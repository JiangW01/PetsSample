package com.pets.sample.api

import com.pets.sample.model.PetModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url
import okhttp3.ResponseBody
import retrofit2.http.Streaming



interface Api {


    @GET("pets")
    fun getDepsList(): Call<MutableList<PetModel>>



    @GET("pet/{petId}")
    fun getDepsDetailById(@Path("petId") petId:String): Call<PetModel>

    @Streaming
    @GET
    fun downloadZip(@Url fileUrl: String): Call<ResponseBody>
}