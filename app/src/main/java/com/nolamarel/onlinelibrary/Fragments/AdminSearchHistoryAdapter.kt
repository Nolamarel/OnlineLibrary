package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.databinding.ItemAdminSearchHistoryBinding
import com.nolamarel.onlinelibrary.network.AdminSearchHistoryResponse

class AdminSearchHistoryAdapter(
    private val items: List<AdminSearchHistoryResponse>
) : RecyclerView.Adapter<AdminSearchHistoryAdapter.AdminSearchHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminSearchHistoryViewHolder {
        val binding = ItemAdminSearchHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminSearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminSearchHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminSearchHistoryViewHolder(
        private val binding: ItemAdminSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminSearchHistoryResponse) {
            binding.userIdTv.text = "Пользователь ID: ${item.userId}"
            binding.queryTv.text = item.query
            binding.dateTv.text = item.createdAt
        }
    }
}