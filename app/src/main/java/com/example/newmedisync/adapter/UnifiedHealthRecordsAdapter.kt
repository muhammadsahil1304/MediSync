package com.example.newmedisync.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.HealthRecordItem
import com.example.newmedisync.model.HealthRecordType

class UnifiedHealthRecordsAdapter(
    private var records: List<HealthRecordItem>,
    private val onItemClick: (HealthRecordItem) -> Unit
) : RecyclerView.Adapter<UnifiedHealthRecordsAdapter.ViewHolder>() {

    fun updateList(newList: List<HealthRecordItem>) {
        records = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_record_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = records[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgTypeIcon: ImageView = itemView.findViewById(R.id.imgTypeIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)

        fun bind(item: HealthRecordItem) {
            tvTitle.text = item.title.ifBlank { "Health Record" }
            tvSubtitle.text = item.subtitle.ifBlank { "No additional details" }
            tvDate.text = item.dateStr.ifBlank { "Recent" }
            tvBadge.text = item.badgeText.ifBlank { item.type.name }

            val context = itemView.context
            when (item.type) {
                HealthRecordType.PRESCRIPTION -> {
                    imgTypeIcon.setImageResource(R.drawable.pdf)
                    imgTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue))
                    tvBadge.setBackgroundResource(R.drawable.bg_chip_blue)
                    tvBadge.setTextColor(ContextCompat.getColor(context, R.color.primaryBlue))
                }
                HealthRecordType.REPORT -> {
                    imgTypeIcon.setImageResource(R.drawable.ic_scan_ai)
                    imgTypeIcon.setColorFilter(Color.parseColor("#16A34A"))
                    tvBadge.setBackgroundResource(R.drawable.bg_chip_blue)
                    tvBadge.setTextColor(Color.parseColor("#16A34A"))
                }
                HealthRecordType.VISIT -> {
                    imgTypeIcon.setImageResource(R.drawable.calender)
                    imgTypeIcon.setColorFilter(Color.parseColor("#8A5A00"))
                    tvBadge.setBackgroundResource(R.drawable.bg_chip_yellow)
                    tvBadge.setTextColor(Color.parseColor("#8A5A00"))
                }
                else -> {
                    imgTypeIcon.setImageResource(R.drawable.ic_report)
                    imgTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue))
                    tvBadge.setBackgroundResource(R.drawable.bg_chip_blue)
                    tvBadge.setTextColor(ContextCompat.getColor(context, R.color.primaryBlue))
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
