package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.databinding.ItemTimelineVisitBinding
import com.example.newmedisync.model.VisitModel

class TimelineAdapter(private val visits: List<VisitModel>) : RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTimelineVisitBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimelineVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visit = visits[position]
        
        // Format date from dd/MM/yyyy to MMM dd, yyyy
        val formattedDate = try {
            val inputSdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val outputSdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputSdf.parse(visit.date)
            date?.let { outputSdf.format(it) } ?: visit.date
        } catch (e: Exception) {
            visit.date
        }

        holder.binding.tvDate.text = formattedDate.uppercase()
        holder.binding.tvTitle.text = visit.purpose
        holder.binding.tvDescription.text = "Clinical interaction with Dr. ${visit.doctorName}. ${visit.time}"
        
        val isPast = System.currentTimeMillis() > visit.timestamp
        holder.binding.tvStatus.text = if (isPast) "COMPLETED" else "UPCOMING"
        holder.binding.tvStatus.background = holder.itemView.context.getDrawable(
            if (isPast) com.example.newmedisync.R.drawable.bg_chip_blue else com.example.newmedisync.R.drawable.bg_chip_yellow
        )
        
        // Hide line for last item
        holder.binding.timelineLine.visibility = if (position == visits.size - 1) View.INVISIBLE else View.VISIBLE
    }

    override fun getItemCount() = visits.size
}