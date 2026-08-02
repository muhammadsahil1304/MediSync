package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.databinding.ItemPrescriptionRecordBinding
import com.example.newmedisync.model.PrescriptionRecord

class PrescriptionAdapter(
    private val prescriptions: List<PrescriptionRecord>,
    private val onDownloadClick: (PrescriptionRecord) -> Unit
) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPrescriptionRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrescriptionRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prescription = prescriptions[position]
        holder.binding.tvDoctorName.text = "Dr. ${prescription.doctorName}"
        holder.binding.tvDate.text = prescription.date
        
        Glide.with(holder.itemView.context)
            .load(prescription.imageUrl)
            .into(holder.binding.ivPrescription)

        holder.binding.btnDownload.setOnClickListener {
            onDownloadClick(prescription)
        }
    }

    override fun getItemCount() = prescriptions.size
}
