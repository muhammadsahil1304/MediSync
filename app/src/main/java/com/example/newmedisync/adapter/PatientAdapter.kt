package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.model.DoctorPatientItem
import com.google.android.material.imageview.ShapeableImageView

class PatientsAdapter(
    private var list: List<DoctorPatientItem>,
    private val onPatientClick: (DoctorPatientItem) -> Unit
) : RecyclerView.Adapter<PatientsAdapter.PatientViewHolder>() {

    fun updateList(newList: List<DoctorPatientItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_patient_card, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPatient: ShapeableImageView = itemView.findViewById(R.id.imgPatient)
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvPatientDetails: TextView = itemView.findViewById(R.id.tvPatientDetails)
        private val tvPatientContact: TextView = itemView.findViewById(R.id.tvPatientContact)
        private val tvApptCountBadge: TextView = itemView.findViewById(R.id.tvApptCountBadge)
        private val tvLastVisit: TextView = itemView.findViewById(R.id.tvLastVisit)
        private val tvNextAppt: TextView = itemView.findViewById(R.id.tvNextAppt)
        private val btnViewProfile: Button = itemView.findViewById(R.id.btnViewProfile)

        fun bind(patient: DoctorPatientItem) {
            tvPatientName.text = patient.name.ifBlank { "Patient" }

            val ageStr = if (patient.age > 0) "${patient.age} yrs" else ""
            val genderStr = patient.gender
            val bloodStr = patient.bloodGroup
            val detailsList = listOf(ageStr, genderStr, bloodStr).filter { it.isNotBlank() }
            tvPatientDetails.text = if (detailsList.isNotEmpty()) detailsList.joinToString(" • ") else "Patient Profile"

            val contactStr = if (patient.phone.isNotBlank()) patient.phone else patient.email
            tvPatientContact.text = if (contactStr.isNotBlank()) contactStr else "No contact info"

            tvApptCountBadge.text = if (patient.appointmentCount > 0) "${patient.appointmentCount} Visits" else "Patient"

            tvLastVisit.text = "Last Visit: ${patient.lastVisitDate}"

            if (patient.nextAppointmentDate.isNotBlank()) {
                tvNextAppt.visibility = View.VISIBLE
                tvNextAppt.text = "Upcoming: ${patient.nextAppointmentDate}"
            } else {
                tvNextAppt.visibility = View.GONE
            }

            if (patient.profileImageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(patient.profileImageUrl)
                    .placeholder(R.drawable.patients)
                    .into(imgPatient)
            } else {
                imgPatient.setImageResource(R.drawable.patients)
            }

            btnViewProfile.setOnClickListener { onPatientClick(patient) }
            itemView.setOnClickListener { onPatientClick(patient) }
        }
    }
}
