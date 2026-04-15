package com.nolamarel.onlinelibrary.Adapters.sections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R

class SectionAdapter(
    private var sections: ArrayList<Section>,
    private var listener: ItemClickListener? = null
) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sections_item, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]

        holder.sectionName.text = section.sectionName

        if (!section.sectionIv.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(section.sectionIv)
                .placeholder(R.drawable.books)
                .error(R.drawable.books)
                .into(holder.sectionImage)
        } else {
            holder.sectionImage.setImageResource(R.drawable.books)
        }

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(clickedPosition)
            }
        }
    }

    override fun getItemCount(): Int = sections.size

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionName: TextView = itemView.findViewById(R.id.section_name)
        val sectionImage: ImageView = itemView.findViewById(R.id.section_iv)
    }
}