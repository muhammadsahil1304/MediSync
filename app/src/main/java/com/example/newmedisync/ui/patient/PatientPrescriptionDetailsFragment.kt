package com.example.newmedisync.ui.patient

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentPatientPrescriptionDetailsBinding
import com.example.newmedisync.model.Medicine
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.PrescriptionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PatientPrescriptionDetailsFragment : Fragment() {

    private var _binding: FragmentPatientPrescriptionDetailsBinding? = null
    private val binding get() = _binding!!

    private var prescriptionId: String = ""
    private var currentPrescription: PrescriptionRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prescriptionId = arguments?.getString("prescriptionId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientPrescriptionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        loadPrescriptionDetails()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnShowPharmacist.setOnClickListener {
            showPharmacistDisplayMode()
        }

        binding.btnShare.setOnClickListener {
            sharePrescriptionText()
        }
    }

    private fun loadPrescriptionDetails() {
        if (prescriptionId.isBlank()) return

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("prescriptions").document(prescriptionId).get().await()
                val prescription = doc.toObject(PrescriptionRecord::class.java)

                binding.progressBar.visibility = View.GONE

                if (prescription != null) {
                    // Security Validation: Ensure prescription belongs to the logged in patient or doctor
                    if (prescription.patientUid.isNotBlank() && prescription.patientUid != currentUid && prescription.doctorUid != currentUid) {
                        Toast.makeText(requireContext(), "Access Denied: You cannot view this prescription.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@launch
                    }

                    currentPrescription = prescription
                    displayPrescription(prescription)
                } else {
                    Toast.makeText(requireContext(), "Prescription not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error loading prescription: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayPrescription(prescription: PrescriptionRecord) {
        val doctorName = if (prescription.doctorName.startsWith("Dr.")) prescription.doctorName else "Dr. ${prescription.doctorName}"
        binding.tvDoctorName.text = doctorName
        binding.tvDoctorSpec.text = if (prescription.doctorSpecialization.isNotBlank()) prescription.doctorSpecialization else "General Physician"
        binding.tvDate.text = prescription.date
        binding.tvPatientName.text = if (prescription.patientName.isNotBlank()) prescription.patientName else "Patient"
        binding.tvPrescribedBy.text = "Prescribed by: $doctorName"

        // Load Patient Age/Gender if available
        if (prescription.patientUid.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("patients").document(prescription.patientUid)
                .get()
                .addOnSuccessListener { patientDoc ->
                    val patientModel = patientDoc.toObject(PatientModel::class.java)
                    if (patientModel != null && (patientModel.age > 0 || patientModel.gender.isNotBlank())) {
                        val ageStr = if (patientModel.age > 0) "${patientModel.age} yrs" else ""
                        val genderStr = patientModel.gender
                        val details = listOf(prescription.patientName, ageStr, genderStr).filter { it.isNotBlank() }.joinToString(" • ")
                        binding.tvPatientName.text = details
                    }
                }
        }

        // Diagnoses
        if (prescription.diagnosis.isNotEmpty()) {
            binding.layoutDiagnosisSection.visibility = View.VISIBLE
            binding.tvDiagnosis.text = prescription.diagnosis.joinToString("\n") { "• $it" }
        } else {
            binding.layoutDiagnosisSection.visibility = View.GONE
        }

        // Medicines List
        binding.layoutMedicines.removeAllViews()
        if (prescription.medicines.isNotEmpty()) {
            prescription.medicines.forEachIndexed { index, med ->
                val cardView = createMedicineCardView(index + 1, med)
                binding.layoutMedicines.addView(cardView)
            }
        } else {
            val tvNoMeds = TextView(requireContext()).apply {
                text = "No medicines listed."
                setTextColor(Color.parseColor("#70757A"))
                textSize = 14f
            }
            binding.layoutMedicines.addView(tvNoMeds)
        }

        // Additional Instructions
        if (prescription.instructions.isNotBlank()) {
            binding.layoutInstructionsSection.visibility = View.VISIBLE
            binding.tvInstructions.text = prescription.instructions
        } else {
            binding.layoutInstructionsSection.visibility = View.GONE
        }

        // Follow-Up
        if (prescription.followUpDate.isNotBlank()) {
            binding.layoutFollowUpSection.visibility = View.VISIBLE
            binding.tvFollowUp.text = "Recommended Follow-up: ${prescription.followUpDate}"
        } else {
            binding.layoutFollowUpSection.visibility = View.GONE
        }
    }

    private fun createMedicineCardView(index: Int, medicine: Medicine): View {
        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(12))
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(1).toFloat()
            setCardBackgroundColor(Color.parseColor("#FAFAFA"))
            strokeColor = Color.parseColor("#E0E0E0")
            strokeWidth = dpToPx(1)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
        }

        val tvName = TextView(requireContext()).apply {
            val str = if (medicine.strength.isNotBlank()) "${medicine.name} ${medicine.strength}" else medicine.name
            text = "$index. $str"
            setTextColor(Color.parseColor("#202124"))
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val detailsStr = listOf(
            "Dosage: ${medicine.dosage}",
            "Frequency: ${medicine.frequency}",
            "Timing: ${medicine.timing}",
            "Duration: ${medicine.durationValue} ${medicine.durationUnit}"
        ).filter { it.isNotBlank() }.joinToString("\n• ")

        val tvDetails = TextView(requireContext()).apply {
            text = "• $detailsStr"
            setTextColor(Color.parseColor("#0A6AA1"))
            textSize = 13f
            setPadding(0, dpToPx(6), 0, 0)
        }

        container.addView(tvName)
        container.addView(tvDetails)

        if (medicine.instructions.isNotBlank()) {
            val tvInstr = TextView(requireContext()).apply {
                text = "Note: ${medicine.instructions}"
                setTextColor(Color.parseColor("#70757A"))
                textSize = 12f
                setPadding(0, dpToPx(4), 0, 0)
            }
            container.addView(tvInstr)
        }

        card.addView(container)
        return card
    }

    private fun showPharmacistDisplayMode() {
        val prescription = currentPrescription ?: return

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_pharmacist_mode)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val tvDoctorName = dialog.findViewById<TextView>(R.id.tvDoctorName)
        val tvPatientAndDate = dialog.findViewById<TextView>(R.id.tvPatientAndDate)
        val layoutMedicinesList = dialog.findViewById<LinearLayout>(R.id.layoutMedicinesList)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)

        val doctorName = if (prescription.doctorName.startsWith("Dr.")) prescription.doctorName else "Dr. ${prescription.doctorName}"
        tvDoctorName.text = doctorName
        tvPatientAndDate.text = "Patient: ${prescription.patientName} • Date: ${prescription.date}"

        layoutMedicinesList.removeAllViews()
        if (prescription.medicines.isNotEmpty()) {
            prescription.medicines.forEachIndexed { index, med ->
                val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, dpToPx(12))
                    }
                    radius = dpToPx(12).toFloat()
                    setCardBackgroundColor(Color.parseColor("#F5F7FA"))
                    strokeColor = Color.parseColor("#B0BEC5")
                    strokeWidth = dpToPx(2)
                }

                val layout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                }

                val tvName = TextView(requireContext()).apply {
                    val str = if (med.strength.isNotBlank()) "${med.name} ${med.strength}" else med.name
                    text = "${index + 1}. ${str.uppercase()}"
                    setTextColor(Color.parseColor("#111111"))
                    textSize = 18f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val details = "${med.dosage}  •  ${med.frequency}  •  ${med.timing}  •  ${med.durationValue} ${med.durationUnit}"
                val tvDetails = TextView(requireContext()).apply {
                    text = details
                    setTextColor(Color.parseColor("#0A6AA1"))
                    textSize = 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setPadding(0, dpToPx(6), 0, 0)
                }

                layout.addView(tvName)
                layout.addView(tvDetails)

                if (med.instructions.isNotBlank()) {
                    val tvInst = TextView(requireContext()).apply {
                        text = "Instructions: ${med.instructions}"
                        setTextColor(Color.parseColor("#555555"))
                        textSize = 13f
                        setPadding(0, dpToPx(4), 0, 0)
                    }
                    layout.addView(tvInst)
                }

                card.addView(layout)
                layoutMedicinesList.addView(card)
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun sharePrescriptionText() {
        val prescription = currentPrescription ?: return

        val doctorName = if (prescription.doctorName.startsWith("Dr.")) prescription.doctorName else "Dr. ${prescription.doctorName}"
        val sb = StringBuilder()
        sb.append("📋 MediSync Digital Prescription\n")
        sb.append("Doctor: $doctorName (${prescription.doctorSpecialization.ifBlank { "General Physician" }})\n")
        sb.append("Patient: ${prescription.patientName}\n")
        sb.append("Date: ${prescription.date}\n\n")

        if (prescription.diagnosis.isNotEmpty()) {
            sb.append("DIAGNOSIS:\n")
            prescription.diagnosis.forEach { sb.append("• $it\n") }
            sb.append("\n")
        }

        if (prescription.medicines.isNotEmpty()) {
            sb.append("PRESCRIBED MEDICINES:\n")
            prescription.medicines.forEachIndexed { i, med ->
                val str = if (med.strength.isNotBlank()) "${med.name} ${med.strength}" else med.name
                sb.append("${i + 1}. $str\n")
                sb.append("   Dosage: ${med.dosage} | ${med.frequency} | ${med.timing} | ${med.durationValue} ${med.durationUnit}\n")
                if (med.instructions.isNotBlank()) {
                    sb.append("   Note: ${med.instructions}\n")
                }
            }
            sb.append("\n")
        }

        if (prescription.instructions.isNotBlank()) {
            sb.append("ADVICE & INSTRUCTIONS:\n${prescription.instructions}\n\n")
        }

        if (prescription.followUpDate.isNotBlank()) {
            sb.append("RECOMMENDED FOLLOW-UP: ${prescription.followUpDate}\n\n")
        }

        sb.append("Prescribed via MediSync Digital Health")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Prescription - ${prescription.patientName}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }

        startActivity(Intent.createChooser(intent, "Share Prescription"))
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
