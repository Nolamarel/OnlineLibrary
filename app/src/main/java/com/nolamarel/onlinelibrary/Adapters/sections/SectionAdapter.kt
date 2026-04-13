package com.nolamarel.onlinelibrary.Adapters.sections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter.SectionViewHolder
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R

class SectionAdapter : RecyclerView.Adapter<SectionViewHolder> {
    private var sections = ArrayList<Section>()
    private var listener: ItemClickListener? = null

    constructor(sections: ArrayList<Section>, listener: ItemClickListener?) {
        this.listener = listener
        this.sections = sections
    }

    constructor(sections: ArrayList<Section>) {
        this.sections = sections
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.sections_item, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]

        holder.section_name.text = section.sectionName
        Glide.with(holder.itemView.context).load(section.sectionIv).into(holder.section_iv)

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                listener!!.onItemClick(clickedPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var section_name: TextView = itemView.findViewById(R.id.section_name)
        var section_iv: ImageView = itemView.findViewById(R.id.section_iv)
    }
}
