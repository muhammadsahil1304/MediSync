package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.model.DoctorVerification
import com.google.android.material.imageview.ShapeableImageView

class DoctorAdapter(
    private var doctors: List<DoctorVerification>,
    private val onDoctorClick: (DoctorVerification) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    fun updateList(newList: List<DoctorVerification>) {
        doctors = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_card, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.bind(doctor)
    }

    override fun getItemCount(): Int = doctors.size

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDoctor: ShapeableImageView = itemView.findViewById(R.id.imgDoctor)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvVerifiedBadge: TextView = itemView.findViewById(R.id.tvVerifiedBadge)
        private val tvSpecialization: TextView = itemView.findViewById(R.id.tvSpecialization)
        private val tvHospitalClinic: TextView = itemView.findViewById(R.id.tvHospitalClinic)
        private val tvExperience: TextView = itemView.findViewById(R.id.tvExperience)
        private val tvFee: TextView = itemView.findViewById(R.id.tvFee)
        private val btnViewProfile: Button = itemView.findViewById(R.id.btnViewProfile)

        fun bind(doctor: DoctorVerification) {
            tvDoctorName.text = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}"
            tvSpecialization.text = doctor.specialization
            tvHospitalClinic.text = if (doctor.clinicName.isNotBlank()) doctor.clinicName else "Clinic"
            tvExperience.text = if (doctor.experience.isNotBlank()) "${doctor.experience} yrs exp." else "Experienced"
            tvFee.text = if (doctor.consultationFee.isNotBlank()) "₹${doctor.consultationFee}" else "₹500"

            if (doctor.status == "APPROVED") {
                tvVerifiedBadge.visibility = View.VISIBLE
            } else {
                tvVerifiedBadge.visibility = View.GONE
            }

            if (doctor.profileImageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(doctor.profileImageUrl)
                    .placeholder(R.drawable.people)
                    .into(imgDoctor)
            } else {
                imgDoctor.setImageResource(R.drawable.people)
            }

            btnViewProfile.setOnClickListener { onDoctorClick(doctor) }
            itemView.setOnClickListener { onDoctorClick(doctor) }
        }
    }
}
