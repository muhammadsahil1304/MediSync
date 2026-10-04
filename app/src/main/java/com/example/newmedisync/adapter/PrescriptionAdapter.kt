package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
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
        holder.binding.tvDoctorName.text = if (prescription.doctorName.startsWith("Dr.")) prescription.doctorName else "Dr. ${prescription.doctorName}"
        holder.binding.tvDate.text = prescription.date

        if (prescription.medicines.isNotEmpty() || prescription.diagnosis.isNotEmpty() || prescription.instructions.isNotBlank()) {
            holder.binding.layoutStructuredDetails.visibility = View.VISIBLE
            holder.binding.ivPrescription.visibility = View.GONE

            if (prescription.diagnosis.isNotEmpty()) {
                holder.binding.tvDiagnosis.visibility = View.VISIBLE
                holder.binding.tvDiagnosis.text = "Diagnosis: ${prescription.diagnosis.joinToString(", ")}"
            } else {
                holder.binding.tvDiagnosis.visibility = View.GONE
            }

            if (prescription.medicines.isNotEmpty()) {
                holder.binding.tvMedicinesSummary.visibility = View.VISIBLE
                val medsStr = prescription.medicines.joinToString("\n") { med ->
                    val str = if (med.strength.isNotBlank()) "${med.name} ${med.strength}" else med.name
                    val details = listOf(med.dosage, med.frequency, med.timing).filter { it.isNotBlank() }.joinToString(" • ")
                    val durationStr = if (med.durationValue > 0) " (${med.durationValue} ${med.durationUnit})" else ""
                    "• $str - $details$durationStr"
                }
                holder.binding.tvMedicinesSummary.text = medsStr
            } else {
                holder.binding.tvMedicinesSummary.visibility = View.GONE
            }

            if (prescription.instructions.isNotBlank()) {
                holder.binding.tvInstructionsSummary.visibility = View.VISIBLE
                holder.binding.tvInstructionsSummary.text = "Advice: ${prescription.instructions}"
            } else {
                holder.binding.tvInstructionsSummary.visibility = View.GONE
            }
        } else if (prescription.imageUrl.isNotBlank()) {
            // Legacy image prescription fallback
            holder.binding.layoutStructuredDetails.visibility = View.GONE
            holder.binding.ivPrescription.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(prescription.imageUrl)
                .into(holder.binding.ivPrescription)
        } else {
            holder.binding.layoutStructuredDetails.visibility = View.VISIBLE
            holder.binding.ivPrescription.visibility = View.GONE
            holder.binding.tvDiagnosis.visibility = View.GONE
            holder.binding.tvMedicinesSummary.text = "General Prescription Issued"
            holder.binding.tvInstructionsSummary.visibility = View.GONE
        }

        holder.binding.btnDownload.setOnClickListener {
            onDownloadClick(prescription)
        }
    }

    override fun getItemCount() = prescriptions.size
}
