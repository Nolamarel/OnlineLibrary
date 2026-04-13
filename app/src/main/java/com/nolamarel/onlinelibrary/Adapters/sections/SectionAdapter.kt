package com.nolamarel.onlinelibrary.Adapters.sections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;

import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
private ArrayList<Section> sections = new ArrayList<>();
    private OnItemClickListener.ItemClickListener listener;

    public SectionAdapter(ArrayList<Section> sections, OnItemClickListener.ItemClickListener listener) {
        this.listener = (OnItemClickListener.ItemClickListener) listener;
        this.sections = sections;
    }

public SectionAdapter(ArrayList<Section> sections){
    this.sections = sections;
}

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sections_item, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sections.get(position);

        holder.section_name.setText(section.sectionName);
        Glide.with(holder.itemView.getContext()).load(section.sectionIv).into(holder.section_iv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int clickedPosition = holder.getAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(clickedPosition);
                    }

            }
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public class SectionViewHolder extends RecyclerView.ViewHolder{

    TextView section_name;
    ImageView section_iv;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            section_name = itemView.findViewById(R.id.section_name);
            section_iv = itemView.findViewById(R.id.section_iv);
        }
    }

}
