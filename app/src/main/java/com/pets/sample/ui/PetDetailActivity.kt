package com.pets.sample.ui

import android.app.ProgressDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.pets.sample.api.Api
import com.pets.sample.api.DownLoaderListener
import com.pets.sample.api.DownloadManager
import com.pets.sample.api.RetrofitManager
import com.pets.sample.model.PetModel
import com.pets.sample.utils.OnZipListener
import com.pets.sample.utils.unZip
import kotlinx.android.synthetic.main.activity_deps_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.os.Build
import android.net.Uri

import android.provider.Settings;
import com.pets.sample.R


class PetDetailActivity : AppCompatActivity() {


    private val request_external_storage = 100
    private var model: PetModel? = null

    private var progrossDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deps_detail)
        getDetail()

        btn_setting.setOnClickListener {
            model?.let {
                if (DownloadManager.existsZipFile(it.id)) {
                    //使用
                    //检查是否已经授予权限，大于6.0的系统适用，小于6.0系统默认打开，无需理会
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                            this
                        )
                    ) {
                        //没有权限，需要申请权限，因为是打开一个授权页面，所以拿不到返回状态的，所以建议是在onResume方法中从新执行一次校验
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        intent.setData(Uri.parse("package:$packageName"))
                        startActivityForResult(intent, 100)
                    } else {
                        //已经有权限，可以直接显示悬浮窗
                        startPetServices()
                    }
                } else {
                    verifyStoragePermissions()
                }
            }
        }
    }


    private fun startPetServices() {
        val intent = Intent(this, PetServices::class.java)
        intent.putExtra("id", model!!.id)
        startService(intent)
    }

    private fun verifyStoragePermissions() {
        val permission =
            ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE")
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            val permissions = arrayOf<String>(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
            ActivityCompat.requestPermissions(this, permissions, request_external_storage);
        } else {
            model?.let {
                progrossDialog = ProgressDialog(this)
                progrossDialog?.let {
                    it.setTitle("下载文件")
                    it.max = 100
                    it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    it.show()
                    it.setCancelable(false)
                }
                DownloadManager.downloadFile(it.id, it.zip!!, object : DownLoaderListener {
                    override fun onLoadStart() {

                    }

                    override fun onLoadPrgress(progress: Long, total: Long) {
                        progrossDialog?.progress = (progress * 100f / total).toInt()
                    }

                    override fun onLoadEnd() {
                        unZipFile()
                    }

                    override fun onError(string: String) {
                        progrossDialog?.dismiss()
                        Toast.makeText(applicationContext, string, Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }

    }

    private fun unZipFile() {
        model?.let {
            if (DownloadManager.existsZipFile(it.id)) {
                runOnUiThread {
                    progrossDialog?.dismiss()
                    //解压缩文件
                    progrossDialog = ProgressDialog(this)
                    progrossDialog?.let {
                        it.setTitle("解压缩文件")
                        it.max = 100
                        it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                        it.show()
                        it.setCancelable(false)
                    }
                }
                val zipFilePath = DownloadManager.getZipFilePath(it.id)
                val unzipDir = DownloadManager.getZipDir(it.id)
                val thread = Thread(Runnable {
                    unZip(zipFilePath, unzipDir, object : OnZipListener {
                        override fun onProgress(currentLength: Long, totalLength: Long) {
                            println("Pets onProgress currentLength = $currentLength totalLength = $totalLength")
                            runOnUiThread {
                                progrossDialog?.progress =
                                    (currentLength * 100f / totalLength).toInt()
                            }
                        }

                        override fun onError(msg: String) {
                            println("Pets onError")
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        }

                        override fun onDone() {
                            println("Pets onDone")
                            runOnUiThread {
                                progrossDialog?.dismiss()
                                btn_setting.text = getString(R.string.seting)
                            }
                        }
                    })
                })
                thread.start()
            } else {
                verifyStoragePermissions()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == request_external_storage) {
            verifyStoragePermissions()
        }
    }


    private fun getDetail() {
        val depsId = intent.getStringExtra("id")
        val api = RetrofitManager.create(Api::class.java)
        val call = api.getDepsDetailById(depsId)
        call.enqueue(object : Callback<PetModel> {
            override fun onFailure(call: Call<PetModel>?, t: Throwable?) {
                println("DepsTag onFailure ${t.toString()}")
                Toast.makeText(deps_image.context, "${t.toString()}", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<PetModel>?, response: Response<PetModel>?) {
                println("DepsTag onResponse")
                response?.let {
                    if (it.isSuccessful) {
                        model = it.body()
                        model?.let {
                            showDeps(it)
                        }
                    }
                }
            }
        })

    }

    private fun showDeps(depsModel: PetModel) {
        Glide.with(this).load(depsModel.image).into(deps_image)
        deps_name.text = depsModel.name
        like_use_count.text = "使用次数:${depsModel.useCount} 喜欢次数:${depsModel.likeCount}"
        btn_setting.visibility = View.VISIBLE
        btn_setting.text = if (DownloadManager.existsZipFile(depsModel.id)) {
            //解压缩文件
            getString(R.string.seting)
        } else {
            getString(R.string.download)
        }
    }
}
