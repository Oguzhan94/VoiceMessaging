package com.example.instantvoice.presentation.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instantvoice.core.data.model.MessageDTO
import com.example.instantvoice.databinding.MessageCardBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MessageAdapter(
    private val onItemClick: (MessageDTO, Boolean) -> Unit
) : ListAdapter<MessageDTO, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private var playingPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding, onItemClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message, position == playingPosition)
    }

    fun updatePlayingPosition(newPosition: Int?) {
        val oldPosition = playingPosition
        playingPosition = newPosition
        oldPosition?.let { notifyItemChanged(it) }
        newPosition?.let { notifyItemChanged(it) }
    }

    class MessageViewHolder(
        private val binding: MessageCardBinding,
        private val onItemClick: (MessageDTO, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(messageDTO: MessageDTO, isPlaying: Boolean) {
            val formattedTimestamp: String? = messageDTO.timestamp?.toDate()?.toInstant()?.let {
                val formatter = DateTimeFormatter.ofPattern("HH:mm dd MMM yyyy")
                    .withZone(ZoneId.systemDefault())
                formatter.format(it)
            }

            binding.tvName.text = messageDTO.userName
            binding.tvDate.text = formattedTimestamp

            binding.playButton.visibility = if (isPlaying) View.GONE else View.VISIBLE
            binding.stopButton.visibility = if (isPlaying) View.VISIBLE else View.GONE

            binding.playButton.setOnClickListener {
                onItemClick(messageDTO, true)
            }

            binding.stopButton.setOnClickListener {
                onItemClick(messageDTO, false)
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageDTO>() {
        override fun areItemsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
            return oldItem.voiceMessageUrl == newItem.voiceMessageUrl
        }

        override fun areContentsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
            return oldItem == newItem
        }
    }
}
