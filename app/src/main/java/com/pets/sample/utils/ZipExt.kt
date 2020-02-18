package com.pets.sample.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


inline fun getZipSize(filePath: String): Long {
    var size: Long = 0
    try {
        val zipFile = ZipFile(filePath);
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            size += entries.nextElement().size
        }

    } catch (e: IOException) {
        size = 0;
    }
    return size;
}

inline fun unZip(filePath: String, outPath: String, listener: OnZipListener) {
    val zipFile = File(filePath)
    if (!zipFile.exists()) {
        listener.onError("zip文件不存在")
    } else {
        val totalLength = getZipSize(filePath)
        var currentLength = 0L
        var zipInputStream: ZipInputStream? = null
        zipInputStream = ZipInputStream(FileInputStream(filePath))
        if (zipInputStream == null || zipInputStream.available() == 0) {
            listener.onError("zip错误")
            return;
        }
        try {
            var zipEntry: ZipEntry? = zipInputStream?.nextEntry
            var tempName: String = ""
            while (zipEntry != null) {
                tempName = zipEntry?.name
                val file = File("$outPath/$tempName")
                if (!file.exists()) {
                    file.getParentFile().mkdirs()
                    file.createNewFile()
                }
                // 获取文件的输出流
                val out = FileOutputStream(file)
                var len = 0
                val buffer = ByteArray(1024)
                // 读取（字节）字节到缓冲区
                len = zipInputStream.read(buffer)
                while (len != -1) {
                    currentLength += len
                    listener.onProgress(currentLength, totalLength)
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                    len = zipInputStream.read(buffer)
                }
                out.close();
                zipEntry = zipInputStream?.nextEntry
            }
            listener.onDone()
        } catch (e: Exception) {
            zipInputStream?.close()
            val file = File(filePath)
            file.delete()
            listener.onError(e.toString())
        }
    }
}

interface OnZipListener {
    fun onProgress(currentLength: Long, totalLength: Long)
    fun onError(msg: String)
    fun onDone()
}