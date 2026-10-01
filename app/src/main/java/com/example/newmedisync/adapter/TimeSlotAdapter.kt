package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.TimeSlot
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private var slots: List<TimeSlot>,
    private val onSlotSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    private var selectedPosition = -1

    fun updateSlots(newSlots: List<TimeSlot>) {
        slots = newSlots
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.bind(slot, position == selectedPosition)
    }

    override fun getItemCount(): Int = slots.size

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardSlot: MaterialCardView = itemView.findViewById(R.id.cardSlot)
        private val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)

        fun bind(slot: TimeSlot, isSelected: Boolean) {
            tvSlotTime.text = slot.time

            if (!slot.isAvailable) {
                cardSlot.isEnabled = false
                cardSlot.alpha = 0.4f
                cardSlot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))
                cardSlot.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_bg)
                tvSlotTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.textSecondary))
                return
            }

            cardSlot.isEnabled = true
            cardSlot.alpha = 1.0f

            if (isSelected) {
                cardSlot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primaryBlue))
                cardSlot.strokeColor = ContextCompat.getColor(itemView.context, R.color.primaryBlue)
                tvSlotTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                cardSlot.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cardWhite))
                cardSlot.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_bg)
                tvSlotTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.textPrimary))
            }

            itemView.setOnClickListener {
                if (!slot.isAvailable) return@setOnClickListener
                val oldPos = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onSlotSelected(slot)
            }
        }
    }
}
