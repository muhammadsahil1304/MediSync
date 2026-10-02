package com.example.newmedisync.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.Appointment
import com.google.android.material.imageview.ShapeableImageView

class DoctorAppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onAppointmentClick: (Appointment) -> Unit
) : RecyclerView.Adapter<DoctorAppointmentAdapter.DoctorAppointmentViewHolder>() {

    fun updateList(newList: List<Appointment>) {
        appointments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorAppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_appointment_card, parent, false)
        return DoctorAppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorAppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class DoctorAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPatient: ShapeableImageView = itemView.findViewById(R.id.imgPatient)
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)

        fun bind(appointment: Appointment) {
            tvPatientName.text = if (appointment.patientName.isNotBlank()) appointment.patientName else "Patient"
            tvLocationName.text = appointment.locationName
            tvDateTime.text = "${appointment.date} • ${appointment.timeSlot}"

            tvStatusBadge.text = appointment.status
            when (appointment.status.uppercase()) {
                "UPCOMING" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
                "COMPLETED" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
                "CANCELLED" -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_red)
                }
                else -> {
                    tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                    tvStatusBadge.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_chip_blue)
                }
            }

            imgPatient.setImageResource(R.drawable.patients)

            btnViewDetails.setOnClickListener { onAppointmentClick(appointment) }
            itemView.setOnClickListener { onAppointmentClick(appointment) }
        }
    }
}
