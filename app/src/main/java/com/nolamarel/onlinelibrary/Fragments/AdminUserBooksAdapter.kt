package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.databinding.ItemAdminUserBookBinding
import com.nolamarel.onlinelibrary.network.AdminUserBookResponse

class AdminUserBooksAdapter(
    private val items: List<AdminUserBookResponse>,
    private val onEditClick: (AdminUserBookResponse) -> Unit,
    private val onDeleteClick: (AdminUserBookResponse) -> Unit
) : RecyclerView.Adapter<AdminUserBooksAdapter.AdminUserBooksViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUserBooksViewHolder {
        val binding = ItemAdminUserBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminUserBooksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminUserBooksViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminUserBooksViewHolder(
        private val binding: ItemAdminUserBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminUserBookResponse) {
            binding.userNameTv.text = "Пользователь: ${item.userName} (${item.userId})"
            binding.bookTitleTv.text = "Книга: ${item.bookTitle}"
            binding.statusTv.text = "Статус: ${item.status}"
            binding.progressTv.text = "Прогресс: ${item.progress}%"
            binding.pageTv.text = "Текущая страница: ${item.currentPage ?: 0}"

            binding.editBtn.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}