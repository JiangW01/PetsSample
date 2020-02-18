package com.pets.sample.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pets.sample.model.PetModel
import com.pets.sample.R
import com.pets.sample.ui.PetDetailActivity

class PetListAdapter(val datas:MutableList<PetModel>) :RecyclerView.Adapter<DepsItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepsItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deps,parent,false)
        return DepsItemViewHolder(view)
    }
    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: DepsItemViewHolder, position: Int) {
        holder.bindData(datas[position])
    }
}

class DepsItemViewHolder(view:View) :RecyclerView.ViewHolder(view){

    val name = view.findViewById<TextView>(R.id.deps_name)
    val image = view.findViewById<ImageView>(R.id.deps_picture)

    fun bindData(deps: PetModel){
        name.text = deps.name
        Glide.with(itemView.context).load(deps.image).into(image);
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, PetDetailActivity::class.java)
            intent.putExtra("id",deps.id)
            itemView.context.startActivity(intent)
        }
    }

}