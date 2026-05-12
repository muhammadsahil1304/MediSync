package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.databinding.ItemVisitHistoryBinding
import com.example.newmedisync.model.VisitHistory

class VisitHistoryAdapter(
    private val list: List<VisitHistory>
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

        holder.binding.tvDate.text = item.date
        holder.binding.tvType.text = item.type
        holder.binding.tvDescription.text = item.description
        holder.binding.tvStatus.text = item.status
    }

    override fun getItemCount(): Int = list.size
}