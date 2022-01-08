package com.example.datenightv3.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.IdeaItemBinding
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IdeaAdapter(private val onItemClicked: (Idea) -> Unit) : ListAdapter<Idea, IdeaAdapter.IdeaViewHolder>(
    DiffCallback
) {
    private var latitude: Double? = null
    private var longitude: Double? = null

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

    class IdeaViewHolder(private var binding: IdeaItemBinding, private var latitude: Double?, private var longitude: Double?): RecyclerView.ViewHolder(binding.root) {
        fun bind(idea: Idea) {
            binding.ideaNameTextView.text = idea.ideaName

            if (idea.categoryName == "Food") {
                val distance = calcDistance(idea.ideaLatitude, idea.ideaLongitude, this.latitude, this.longitude)
                binding.ideaDistanceTextView.text = String.format("%.1f", distance) + "km"
            }
        }

        private fun calcDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double? {
            var d : Double? = null
            if (lon2 != null && lat2 != null && lat1 != null && lon1 != null) {
                val R = 6371; // Radius of the earth in km
                val dLat = deg2rad(lat2 - lat1);  // deg2rad below
                val dLon = deg2rad(lon2 - lon1);
                val a =
                    sin(dLat / 2) * sin(dLat / 2) +
                            cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                            sin(dLon / 2) * sin(dLon / 2)
                ;
                val c = 2 * atan2(sqrt(a), sqrt(1 - a));
                d = R * c; // Distance in km
            }
            return d
        }

        private fun deg2rad(deg: Double): Double {
            return deg * (Math.PI/180)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        val viewHolder = IdeaViewHolder(
            IdeaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            this.latitude,
            this.longitude
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

    fun getCoordinates(latitude: Double?, longitude: Double?){
        this.latitude = latitude
        this.longitude = longitude
    }
}