package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DateItem(
    val date: Date,
    val dateString: String, // e.g. "28 Sep 2026"
    val dayOfWeek: String, // e.g. "MON"
    val dayNum: String, // e.g. "28"
    val monthStr: String, // e.g. "SEP"
    val isAvailable: Boolean = true
)

class DateAdapter(
    private val dateItems: List<DateItem>,
    private val onDateSelected: (DateItem) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition = dateItems.indexOfFirst { it.isAvailable }.coerceAtLeast(0)

    init {
        if (selectedPosition in dateItems.indices) {
            onDateSelected(dateItems[selectedPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dateItems[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = dateItems.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardDate: MaterialCardView = itemView.findViewById(R.id.cardDate)
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        private val tvDayNum: TextView = itemView.findViewById(R.id.tvDayNum)
        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)

        fun bind(item: DateItem, isSelected: Boolean) {
            tvDayOfWeek.text = item.dayOfWeek
            tvDayNum.text = item.dayNum
            tvMonth.text = item.monthStr

            if (!item.isAvailable) {
                cardDate.isEnabled = false
                cardDate.alpha = 0.4f
                cardDate.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))
                cardDate.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_bg)
                return
            }

            cardDate.isEnabled = true
            cardDate.alpha = 1.0f

            if (isSelected) {
                cardDate.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primaryBlue))
                cardDate.strokeColor = ContextCompat.getColor(itemView.context, R.color.primaryBlue)
                tvDayOfWeek.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                tvDayNum.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                cardDate.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cardWhite))
                cardDate.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_bg)
                tvDayOfWeek.setTextColor(ContextCompat.getColor(itemView.context, R.color.textSecondary))
                tvDayNum.setTextColor(ContextCompat.getColor(itemView.context, R.color.textPrimary))
                tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.textSecondary))
            }

            itemView.setOnClickListener {
                if (!item.isAvailable) return@setOnClickListener
                val oldPos = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onDateSelected(item)
            }
        }
    }
}
