package com.example.newmedisync.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.model.Appointment
import com.google.android.material.imageview.ShapeableImageView

class AppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onAppointmentClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    fun updateList(newList: List<Appointment>) {
        appointments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDoctor: ShapeableImageView = itemView.findViewById(R.id.imgDoctor)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvSpecialization: TextView = itemView.findViewById(R.id.tvSpecialization)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)

        fun bind(appointment: Appointment) {
            tvDoctorName.text = if (appointment.doctorName.startsWith("Dr.")) appointment.doctorName else "Dr. ${appointment.doctorName}"
            tvSpecialization.text = appointment.doctorSpecialization
            tvDateTime.text = "${appointment.date} • ${appointment.timeSlot}"
            tvLocation.text = appointment.locationName

            tvStatusBadge.text = appointment.status
            when (appointment.status) {
                "Upcoming" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
                "Completed" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
                "Cancelled" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_red)
                }
                else -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
            }

            if (appointment.doctorProfileImageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(appointment.doctorProfileImageUrl)
                    .placeholder(R.drawable.people)
                    .into(imgDoctor)
            } else {
                imgDoctor.setImageResource(R.drawable.people)
            }

            btnViewDetails.setOnClickListener { onAppointmentClick(appointment) }
            itemView.setOnClickListener { onAppointmentClick(appointment) }
        }
    }
}
