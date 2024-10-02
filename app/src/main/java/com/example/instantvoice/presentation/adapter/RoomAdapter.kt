package com.example.instantvoice.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instantvoice.core.data.model.RoomDTO
import com.example.instantvoice.databinding.RoomCardBinding

class RoomAdapter(
    private val onItemClick: (RoomDTO) -> Unit
) : ListAdapter<RoomDTO, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = RoomCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = getItem(position)
        holder.bind(room)
    }

    class RoomViewHolder(
        private val binding: RoomCardBinding,
        private val onItemClick: (RoomDTO) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(roomDTO: RoomDTO) {
            binding.tvRoomName.text = roomDTO.name
            binding.root.setOnClickListener { onItemClick(roomDTO) }
        }
    }

    class RoomDiffCallback : DiffUtil.ItemCallback<RoomDTO>() {
        override fun areItemsTheSame(oldItem: RoomDTO, newItem: RoomDTO): Boolean {
            return oldItem.roomID == newItem.roomID
        }

        override fun areContentsTheSame(oldItem: RoomDTO, newItem: RoomDTO): Boolean {
            return oldItem == newItem
        }
    }
}
