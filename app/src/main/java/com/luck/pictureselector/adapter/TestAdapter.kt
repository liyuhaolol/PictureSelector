package com.luck.pictureselector.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luck.pictureselector.databinding.ItemImgBinding
import spa.lyh.cn.lib_image.app.ImageLoadUtil

class TestAdapter(val context: Context,val list:ArrayList<String>): RecyclerView.Adapter<TestAdapter.ViewHolder>() {



    class ViewHolder(val b: ItemImgBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemImgBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ImageLoadUtil.displayImage(context,list[position],holder.b.img)
    }
}