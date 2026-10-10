package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.model.MyDoctorItem
import com.google.android.material.imageview.ShapeableImageView

class MyDoctorsAdapter(
    private var doctorsList: List<MyDoctorItem>,
    private val onBookAgainClick: (MyDoctorItem) -> Unit
) : RecyclerView.Adapter<MyDoctorsAdapter.ViewHolder>() {

    fun updateList(newList: List<MyDoctorItem>) {
        doctorsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_doctor_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = doctorsList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = doctorsList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDoctor: ShapeableImageView = itemView.findViewById(R.id.imgDoctor)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvSpecialization: TextView = itemView.findViewById(R.id.tvSpecialization)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvLastVisit: TextView = itemView.findViewById(R.id.tvLastVisit)
        private val btnBookAgain: Button = itemView.findViewById(R.id.btnBookAgain)

        fun bind(doctor: MyDoctorItem) {
            tvDoctorName.text = doctor.doctorName.ifBlank { "Doctor" }
            tvSpecialization.text = doctor.specialization.ifBlank { "Specialist" }
            tvLocation.text = "📍 ${doctor.lastLocationName.ifBlank { "Practice Location" }}"
            tvLastVisit.text = if (doctor.lastVisitDate.isNotBlank()) "Last Visit: ${doctor.lastVisitDate}" else "Previous Consultation"

            if (doctor.profileImageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(doctor.profileImageUrl)
                    .placeholder(R.drawable.people)
                    .into(imgDoctor)
            } else {
                imgDoctor.setImageResource(R.drawable.people)
            }

            btnBookAgain.setOnClickListener { onBookAgainClick(doctor) }
            itemView.setOnClickListener { onBookAgainClick(doctor) }
        }
    }
}
