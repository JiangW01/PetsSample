package com.pets.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.pets.sample.api.Api
import com.pets.sample.api.RetrofitManager
import com.pets.sample.model.PetModel
import com.pets.sample.ui.adapter.PetListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {


    private val datas = mutableListOf<PetModel>()
    private lateinit var depsAdapter: PetListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pet_recyclerview.layoutManager = LinearLayoutManager(this)
        depsAdapter = PetListAdapter(datas)
        pet_recyclerview.adapter = depsAdapter
        refreshlayout.setOnRefreshListener {
            val api = RetrofitManager.create(Api::class.java)
            val call = api.getDepsList()
            call.enqueue(object :Callback<MutableList<PetModel>>{
                override fun onFailure(call: Call<MutableList<PetModel>>?, t: Throwable?) {
                    println("DepsTag onFailure ${t.toString()}")
                    Toast.makeText(refreshlayout.context,"${t.toString()}",Toast.LENGTH_SHORT).show()
                    refreshlayout.finishRefresh()
                }
                override fun onResponse(call: Call<MutableList<PetModel>>?, response: Response<MutableList<PetModel>>?) {
                    println("DepsTag onResponse")
                    response?.let {
                        if (it.isSuccessful) {
                            val body:MutableList<PetModel> = it.body()
                            datas.clear()
                            datas.addAll(body)
                            depsAdapter.notifyDataSetChanged()
                        }
                    }
                    refreshlayout.finishRefresh()
                }
            })
        }
        refreshlayout.setEnableAutoLoadMore(false)
        refreshlayout.autoRefresh()
    }
}
