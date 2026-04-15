package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.UserResponse
import com.nolamarel.onlinelibrary.databinding.ItemAdminUserBinding

class AdminUsersAdapter(
    private val items: List<UserResponse>,
    private val onEditClick: (UserResponse) -> Unit,
    private val onDeleteClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.AdminUsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUsersViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminUsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminUsersViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminUsersViewHolder(
        private val binding: ItemAdminUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserResponse) {
            binding.userNameTv.text = item.name
            binding.userEmailTv.text = item.email
            binding.userRoleTv.text = "Роль: ${item.role}"
            binding.userStatusTv.text = if (item.isActive) "Активен" else "Неактивен"

            binding.editUserBtn.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteUserBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}