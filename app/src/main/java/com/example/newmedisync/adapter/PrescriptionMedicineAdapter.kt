package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.Medicine

class PrescriptionMedicineAdapter(
    private var medicineList: MutableList<Medicine>,
    private val onEditRequested: (position: Int, medicine: Medicine) -> Unit,
    private val onDeleteRequested: (position: Int, medicine: Medicine) -> Unit
) : RecyclerView.Adapter<PrescriptionMedicineAdapter.MedicineViewHolder>() {

    fun updateList(newList: List<Medicine>) {
        medicineList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun getMedicines(): List<Medicine> = medicineList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescription_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(medicineList[position], position)
    }

    override fun getItemCount(): Int = medicineList.size

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMedicineName: TextView = itemView.findViewById(R.id.tvMedicineName)
        private val tvDosageDetails: TextView = itemView.findViewById(R.id.tvDosageDetails)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvInstructions: TextView = itemView.findViewById(R.id.tvInstructions)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(medicine: Medicine, position: Int) {
            val nameAndStrength = if (medicine.strength.isNotBlank()) "${medicine.name} ${medicine.strength}" else medicine.name
            tvMedicineName.text = nameAndStrength

            val dosageStr = listOf(medicine.dosage, medicine.frequency, medicine.timing).filter { it.isNotBlank() }.joinToString(" • ")
            tvDosageDetails.text = if (dosageStr.isNotBlank()) dosageStr else "Daily"

            tvDuration.text = "Duration: ${medicine.durationValue} ${medicine.durationUnit}"

            if (medicine.instructions.isNotBlank()) {
                tvInstructions.visibility = View.VISIBLE
                tvInstructions.text = "Instructions: ${medicine.instructions}"
            } else {
                tvInstructions.visibility = View.GONE
            }

            btnEdit.setOnClickListener { onEditRequested(position, medicine) }
            btnDelete.setOnClickListener { onDeleteRequested(position, medicine) }
        }
    }
}
