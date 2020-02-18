package com.pets.sample.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitManager {


    private val retrofit :Retrofit

    init {
        val  okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())//解析方法
            .baseUrl("https://petdemoserver.now.sh/")
            .build()
    }

    fun <T> create(service: Class<T>):T{
        return retrofit.create(service)
    }

}

