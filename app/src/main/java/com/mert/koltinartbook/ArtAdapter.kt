package com.mert.koltinartbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mert.koltinartbook.databinding.ActivityDetailsBinding
import com.mert.koltinartbook.databinding.RecylerRowBinding

class ArtAdapter ( val artList : ArrayList<Arts>):RecyclerView.Adapter<ArtAdapter.ArtHolder>() {
    class ArtHolder(val binding: RecylerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecylerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recylerViewTextView.text = artList.get(position).name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id",artList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}