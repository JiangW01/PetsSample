package com.pets.sample.ui

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import android.view.Gravity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.provider.Settings.canDrawOverlays
import android.view.View
import android.widget.ImageView
import com.pets.sample.api.DownloadManager
import com.pets.sample.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class PetServices : Service() {


    private var isStart = false

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val images = mutableListOf<String>()
    private lateinit var displayView: View
    private lateinit var imageView: ImageView
    private var executorService:ExecutorService? = null
    private var id:String = ""

    override fun onCreate() {
        super.onCreate()
        isStart = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        showPetWindow()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        id = intent.getStringExtra("id")
        startThread()
        return super.onStartCommand(intent, flags, startId)
    }


    private val handle = Handler(object :Handler.Callback{
        override fun handleMessage(msg: Message): Boolean {
            val frameIndex = msg.what
            if(images.size != 0){
                val filePath = images[frameIndex%images.size]
                val bitmap = BitmapFactory.decodeFile(filePath)
                imageView.setImageBitmap(bitmap)
            }
            return true
        }
    })

    private val lock = Object()

    private fun startThread() {
        executorService?.shutdown()
        executorService = Executors.newSingleThreadExecutor()
        executorService?.execute(Runnable {
            //获取图片列表
            getImages()
            //假设fps帧率是25帧，
            val duration:Long = 1000/25
            var frameIndex = 0
            while (!executorService!!.awaitTermination(duration, TimeUnit.MILLISECONDS)){
                println("Pet frameIndex = $frameIndex")
                handle.sendEmptyMessage(frameIndex)
                frameIndex++
                if (frameIndex >= images.size){
                    frameIndex = 0
                }

            }
        })
    }

    private fun getImages() {
        images.clear()
        val dirPath = DownloadManager.getZipDir(id)
        val dir = File(dirPath)
        if(dir.exists() && dir.isDirectory){
            dir.listFiles()
                .filter {
                    it.isFile && it.name.toLowerCase().endsWith(".png")
                }
                .forEach {
                    images.add(it.absolutePath)
                }
        }
    }

    private fun showPetWindow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || canDrawOverlays(this)) {
            val layoutInflater = LayoutInflater.from(this)
            displayView = layoutInflater.inflate(R.layout.image_display, null)
            imageView = displayView.findViewById<ImageView>(R.id.image_display_imageview)
            windowManager.addView(displayView, layoutParams)
        }
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}