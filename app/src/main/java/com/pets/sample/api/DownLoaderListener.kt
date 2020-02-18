package com.pets.sample.api

interface DownLoaderListener {


    fun onLoadStart()

    fun onLoadPrgress(progress:Long,total:Long)

    fun onLoadEnd()

    fun onError(string: String)

}