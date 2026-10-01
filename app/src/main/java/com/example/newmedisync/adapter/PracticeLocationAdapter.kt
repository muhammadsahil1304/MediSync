package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.PracticeLocation
import com.google.android.material.card.MaterialCardView

class PracticeLocationAdapter(
    private var locations: List<PracticeLocation>,
    private val onLocationSelected: (PracticeLocation) -> Unit
) : RecyclerView.Adapter<PracticeLocationAdapter.LocationViewHolder>() {

    private var selectedPosition = 0

    fun getSelectedLocation(): PracticeLocation? {
        return if (locations.isNotEmpty() && selectedPosition in locations.indices) {
            locations[selectedPosition]
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_practice_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location, position == selectedPosition)
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardLocation: MaterialCardView = itemView.findViewById(R.id.cardLocation)
        private val rbSelect: RadioButton = itemView.findViewById(R.id.rbSelect)
        private val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        private val tvLocationAddress: TextView = itemView.findViewById(R.id.tvLocationAddress)
        private val tvDaysAndTiming: TextView = itemView.findViewById(R.id.tvDaysAndTiming)
        private val tvFee: TextView = itemView.findViewById(R.id.tvFee)

        fun bind(location: PracticeLocation, isSelected: Boolean) {
            tvLocationName.text = location.name
            tvLocationAddress.text = location.address
            val daysStr = if (location.availableDays.isNotEmpty()) location.availableDays.joinToString(" / ") else "Mon - Sat"
            tvDaysAndTiming.text = "$daysStr • ${location.timing}"
            tvFee.text = "₹${location.consultationFee.toInt()}"

            rbSelect.isChecked = isSelected

            if (isSelected) {
                cardLocation.strokeColor = ContextCompat.getColor(itemView.context, R.color.primaryBlue)
                cardLocation.strokeWidth = 4
                cardLocation.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.lightBlue))
            } else {
                cardLocation.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_bg)
                cardLocation.strokeWidth = 2
                cardLocation.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cardWhite))
            }

            itemView.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onLocationSelected(location)
            }
        }
    }
}
