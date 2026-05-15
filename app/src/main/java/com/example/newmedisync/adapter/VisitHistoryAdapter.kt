package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.databinding.ItemVisitHistoryBinding
import com.example.newmedisync.model.VisitHistory
import com.example.newmedisync.room.PrescriptionEntity

class VisitHistoryAdapter(
    private val list: List<PrescriptionEntity>,
    private val onItemClick: (PrescriptionEntity) -> Unit
) : RecyclerView.Adapter<VisitHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemVisitHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemVisitHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.binding.tvDate.text = item.visitDate
        holder.binding.tvType.text = "PRESCRIPTION"
        holder.binding.tvDescription.text = item.patientPhone
        holder.binding.tvStatus.text = "VIEW"
    }

    override fun getItemCount(): Int = list.size
}