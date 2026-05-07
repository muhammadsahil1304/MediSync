package com.example.newmedisync.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medisync.model.Patient
import com.example.newmedisync.databinding.ItemPatientBinding

class PatientsAdapter(private val list: List<Patient>) :
    RecyclerView.Adapter<PatientsAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(val binding: ItemPatientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            tvInitials.text = item.initials
            tvName.text = item.name
            tvId.text = "ID: ${item.patientId}"
            tvPhone.text = item.phone
            tvLastVisit.text = "Last visit: ${item.lastVisit}"
            tvStatus.text = item.status
        }
    }

    override fun getItemCount(): Int = list.size
}