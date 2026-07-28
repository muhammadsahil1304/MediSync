package com.example.newmedisync.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.model.DoctorVerification
import com.google.android.material.chip.Chip

class PendingDoctorsAdapter(
    private var doctors: List<DoctorVerification>,
    private val onClick: (DoctorVerification) -> Unit
) : RecyclerView.Adapter<PendingDoctorsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgDoctor: ImageView = view.findViewById(R.id.imgDoctor)
        val tvDoctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvSpecialization: TextView = view.findViewById(R.id.tvSpecialization)
        val tvClinic: TextView = view.findViewById(R.id.tvClinic)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_doctors, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = doctors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val doctor = doctors[position]

        holder.tvDoctorName.text = doctor.fullName
        holder.tvSpecialization.text = doctor.specialization
        holder.tvClinic.text = doctor.clinicName
        holder.chipStatus.text = doctor.status

        holder.imgDoctor.setImageResource(R.drawable.people)

        holder.itemView.setOnClickListener {
            onClick(doctor)
        }
    }

    fun submitList(list: List<DoctorVerification>) {
        doctors = list
        notifyDataSetChanged()
    }
}