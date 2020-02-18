package com.pets.sample.api

import android.os.Environment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object DownloadManager {


    private val BASE_CACHE__DIR: String

    init {
        BASE_CACHE__DIR = "${Environment.getExternalStorageDirectory()}/Android/data/com.pets.sample/pets/"
        println("Pets dirPath = $BASE_CACHE__DIR")
    }


    fun existsZipFile(id: String): Boolean {
        val dir = File("$BASE_CACHE__DIR${id}/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val filePath = getZipFilePath(id)
        println("Pets filePath = $filePath")
        val zipFile = File(filePath)
        return zipFile.exists()
    }

    fun getZipFilePath(id: String): String {
        val filePath = "$BASE_CACHE__DIR${id}/${id}.zip"
        return filePath
    }

    fun getZipDir(id: String): String {
        val dir = "$BASE_CACHE__DIR${id}/"
        return dir
    }


    fun downloadFile(id: String, url: String, listener: DownLoaderListener) {
        val dir = File("$BASE_CACHE__DIR${id}/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val filePath = getZipFilePath(id)
        val zipFile = File(filePath)
        if (zipFile.exists()) {
            listener.onLoadEnd()
        } else {
            listener.onLoadStart()
            val call = RetrofitManager.create(Api::class.java).downloadZip(url)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    listener.onError(t.toString())
                }

                override fun onResponse(
                    call: Call<ResponseBody>?,
                    response: Response<ResponseBody>?
                ) {
                    response?.let {
                        val body: ResponseBody = it.body()
                        val thread = Thread(Runnable {
                            writeFileToDisk(zipFile, body, listener)
                        })
                        thread.start()
                    }
                }
            })
        }
    }

    private fun writeFileToDisk(
        zipFile: File,
        responseBody: ResponseBody,
        listener: DownLoaderListener
    ) {
        if (!zipFile.parentFile.exists()) {
            zipFile.parentFile.mkdirs()
            zipFile.createNewFile()
        }
        var currentLength: Long = 0
        var os: OutputStream? = null
        var inputStream = responseBody.byteStream()
        val totalLength = responseBody.contentLength()
        os = FileOutputStream(zipFile)
        var len: Int
        val buffer = ByteArray(1204)
        try {
            len = inputStream.read(buffer)
            while (len != -1) {
                os.write(buffer, 0, len);
                currentLength += len;
                //计算当前下载百分比，并经由回调传出
                listener.onLoadPrgress(currentLength, totalLength);
                if (currentLength == totalLength) {
                    listener.onLoadEnd()//下载完成
                }
                len = inputStream.read(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            listener.onError(e.toString())
        }
    }


    private fun getFileName(url: String): String {
        val splits = url.split("/")
        return if (splits.isNullOrEmpty()) "" else splits[splits.size - 1]
    }


}