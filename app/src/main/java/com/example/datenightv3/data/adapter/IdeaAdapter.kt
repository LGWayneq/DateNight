package com.example.datenightv3.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.IdeaItemBinding

class IdeaAdapter(private val onItemClicked: (Idea) -> Unit) : ListAdapter<Idea, IdeaAdapter.IdeaViewHolder>(
    DiffCallback
) {
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Idea>() {
            override fun areItemsTheSame(oldItem: Idea, newItem: Idea): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Idea, newItem: Idea): Boolean {
                return oldItem == newItem
            }
        }
    }

    class IdeaViewHolder(private var binding: IdeaItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(idea: Idea) {
            binding.ideaNameTextView.text = idea.ideaName
            if (idea.ideaLocationDistance != null) binding.ideaDistanceTextView.text = String.format("%.1f", idea.ideaLocationDistance) + "km"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        val viewHolder = IdeaViewHolder(
            IdeaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}