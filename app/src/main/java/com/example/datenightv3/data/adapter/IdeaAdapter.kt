package com.example.datenightv3.data.adapter

import android.app.ActionBar
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.IdeaItemBinding
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IdeaAdapter(private var latitude: Double?,
                  private var longitude: Double?,
                  private val requireLocation: Boolean,
                  private val onItemClicked: (Idea) -> Unit, ) : ListAdapter<Idea, IdeaAdapter.IdeaViewHolder>(DiffCallback) {

    var distanceList: MutableList<Double> = ArrayList(Collections.nCopies(50,0.0)) //refine the list initialisation

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
        var distance : Double = -1.0
        fun bind(idea: Idea, latitude: Double?, longitude: Double?, requireLocation: Boolean) {
            binding.ideaNameTextView.text = idea.name
            if (requireLocation) {
                val ideaLatitude = idea.latitude
                val ideaLongitude = idea.longitude
                distance = calcDistance(ideaLatitude, ideaLongitude, latitude, longitude)
                if (distance != -1.0) binding.ideaDistanceTextView.text = String.format("%.1f", distance) + "km"
                else binding.ideaDistanceTextView.text = "-"
            } else {
                binding.ideaNameLayout.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f))
            }
        }

        private fun calcDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double {
            var d : Double = -1.0
            if (lon2 != null && lat2 != null && lat1 != null && lon1 != null) {
                val R = 6371; // Radius of the earth in km
                val dLat = deg2rad(lat2 - lat1)
                val dLon = deg2rad(lon2 - lon1)
                val a =
                    sin(dLat / 2) * sin(dLat / 2) +
                            cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                            sin(dLon / 2) * sin(dLon / 2)
                val c = 2 * atan2(sqrt(a), sqrt(1 - a))
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
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        val idea: Idea = getItem(position)
        holder.bind(idea, this.latitude, this.longitude, requireLocation)
        distanceList[position] = holder.distance
    }


}