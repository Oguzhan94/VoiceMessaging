package com.example.instantvoice.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.databinding.UserListCardBinding

class UserAdapter(
    private val onItemClick: (UserDTO) -> Unit
) : ListAdapter<UserDTO, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    private var selectedItemPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            UserListCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, position)
    }

    inner class UserViewHolder(
        private val binding: UserListCardBinding,
        private val onItemClick: (UserDTO) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userDTO: UserDTO, position: Int) {
            binding.tvUserName.text = userDTO.name
            binding.tvUserSurName.text = userDTO.surname
            binding.checkBox.isChecked = userDTO.inRoom

            binding.root.setOnClickListener {
                val previousSelectedPosition = selectedItemPosition
                selectedItemPosition = if (selectedItemPosition == position) null else position
                notifyItemChanged(previousSelectedPosition ?: -1)
                notifyItemChanged(position)
                onItemClick(userDTO)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserDTO>() {
        override fun areItemsTheSame(oldItem: UserDTO, newItem: UserDTO): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserDTO, newItem: UserDTO): Boolean {
            return oldItem == newItem
        }
    }
}

