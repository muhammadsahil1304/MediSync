package com.example.newmedisync.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medisync.model.Patient
import com.example.newmedisync.databinding.ItemPatientBinding
import com.example.newmedisync.room.PatientEntity

class PatientsAdapter(private val list: List<PatientEntity>) :
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
            tvInitials.text =
                item.name.take(2).uppercase()
            tvName.text = item.name
            tvPhone.text = item.phone
            tvLastVisit.text = "Age: ${item.age}"
            tvStatus.text = item.bloodGroup
            tvId.text = "Gender: ${item.gender}"
//            tvStatus.text = item.status
        }
    }

    override fun getItemCount(): Int = list.size
}