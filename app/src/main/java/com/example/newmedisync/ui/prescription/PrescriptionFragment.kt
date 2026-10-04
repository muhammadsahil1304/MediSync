package com.example.newmedisync.ui.prescription

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.PrescriptionMedicineAdapter
import com.example.newmedisync.databinding.FragmentPrescriptionBinding
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.Medicine
import com.example.newmedisync.model.PrescriptionRecord
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.PatientEntity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrescriptionBoardFragment : Fragment() {

    private var _binding: FragmentPrescriptionBinding? = null
    private val binding get() = _binding!!

    private var selectedPatientName = ""
    private var selectedPatientPhone = ""
    private var selectedPatientUid = ""
    private var appointmentId = ""

    private val diagnosisList = mutableListOf<String>()
    private val medicinesList = mutableListOf<Medicine>()
    private lateinit var medicineAdapter: PrescriptionMedicineAdapter
    private val patientRepository = PatientRepository()

    private var isIssued = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(inflater, container, false)
        arguments?.let {
            selectedPatientName = it.getString("name", "")
            selectedPatientPhone = it.getString("phone", "")
            selectedPatientUid = it.getString("patientUid", "")
            appointmentId = it.getString("appointmentId", "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupHeaderInfo()
        setupMedicineRecyclerView()
        setupListeners()
        loadExistingPrescriptionIfAny()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (hasUnsavedChanges() && !isIssued) {
                        showUnsavedChangesDialog()
                    } else {
                        remove()
                        findNavController().popBackStack()
                    }
                }
            }
        )
    }

    private fun hasUnsavedChanges(): Boolean {
        return diagnosisList.isNotEmpty() || medicinesList.isNotEmpty() ||
                binding.etAdditionalInstructions.text.toString().isNotBlank()
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unissued prescription changes. Are you sure you want to leave?")
            .setNegativeButton("Stay", null)
            .setPositiveButton("Leave") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun setupHeaderInfo() {
        binding.btnBack.setOnClickListener {
            if (hasUnsavedChanges() && !isIssued) {
                showUnsavedChangesDialog()
            } else {
                findNavController().popBackStack()
            }
        }

        if (selectedPatientName.isNotBlank()) {
            binding.layoutPatientSelector.visibility = View.GONE
            binding.tvPatientHeaderDetails.visibility = View.VISIBLE
            val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            binding.tvPatientHeaderDetails.text = "Patient: $selectedPatientName • Date: $todayStr"
        } else {
            binding.layoutPatientSelector.visibility = View.VISIBLE
            binding.tvPatientHeaderDetails.visibility = View.GONE
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isNotBlank()) {
                AppDatabase.getDatabase(requireContext())
                    .patientDao()
                    .getAllPatients(uid)
                    .observe(viewLifecycleOwner) { patients ->
                        setupPatientsSpinner(patients)
                    }
            }
        }
    }

    private fun loadExistingPrescriptionIfAny() {
        if (appointmentId.isBlank()) return

        lifecycleScope.launch {
            try {
                val existing = patientRepository.getPrescriptionByAppointmentId(appointmentId)
                if (existing != null) {
                    if (existing.diagnosis.isNotEmpty()) {
                        diagnosisList.clear()
                        diagnosisList.addAll(existing.diagnosis)
                        renderDiagnosisChips()
                    }
                    if (existing.medicines.isNotEmpty()) {
                        medicinesList.clear()
                        medicinesList.addAll(existing.medicines)
                        medicineAdapter.updateList(medicinesList)
                        updateMedicinesEmptyState()
                    }
                    if (existing.instructions.isNotBlank()) {
                        binding.etAdditionalInstructions.setText(existing.instructions)
                    }
                    if (existing.followUpDate.isNotBlank()) {
                        binding.etFollowUp.setText(existing.followUpDate)
                    }
                    Toast.makeText(requireContext(), "Loaded existing prescription for this appointment.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
    }

    private fun setupPatientsSpinner(patients: List<PatientEntity>) {
        val patientNames = mutableListOf("Select Patient")
        patientNames.addAll(patients.map { it.name })

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, patientNames)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spPatients.adapter = adapter

        binding.spPatients.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedPatientName = ""
                    selectedPatientPhone = ""
                    binding.etPatientPhone.setText("")
                    return
                }
                val patient = patients[position - 1]
                selectedPatientName = patient.name
                selectedPatientPhone = patient.phone
                binding.etPatientPhone.setText(patient.phone)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupMedicineRecyclerView() {
        medicineAdapter = PrescriptionMedicineAdapter(
            medicinesList,
            onEditRequested = { position, medicine ->
                showAddOrEditMedicineDialog(position, medicine)
            },
            onDeleteRequested = { position, medicine ->
                showDeleteMedicineConfirmation(position, medicine)
            }
        )
        binding.rvMedicines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicines.adapter = medicineAdapter
        updateMedicinesEmptyState()
    }

    private fun setupListeners() {
        binding.btnAddDiagnosis.setOnClickListener {
            showAddDiagnosisDialog()
        }

        binding.btnAddMedicine.setOnClickListener {
            showAddOrEditMedicineDialog(-1, null)
        }

        binding.btnPreview.setOnClickListener {
            showPrescriptionPreviewDialog()
        }

        binding.btnIssuePrescription.setOnClickListener {
            issuePrescription()
        }
    }

    private fun showAddDiagnosisDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "e.g. Viral Fever, Hypertension"
            setPadding(32, 24, 32, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Diagnosis")
            .setView(editText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val diagText = editText.text.toString().trim()
                if (diagText.isNotBlank()) {
                    diagnosisList.add(diagText)
                    renderDiagnosisChips()
                }
            }
            .show()
    }

    private fun renderDiagnosisChips() {
        binding.cgDiagnosis.removeAllViews()
        diagnosisList.forEachIndexed { index, diag ->
            val chip = Chip(requireContext()).apply {
                text = diag
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    diagnosisList.removeAt(index)
                    renderDiagnosisChips()
                }
            }
            binding.cgDiagnosis.addView(chip)
        }
    }

    private fun showAddOrEditMedicineDialog(editPosition: Int, existingMedicine: Medicine?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_medicine)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etMedicineName = dialog.findViewById<EditText>(R.id.etMedicineName)
        val etStrength = dialog.findViewById<EditText>(R.id.etStrength)
        val etDosage = dialog.findViewById<EditText>(R.id.etDosage)
        val cgFrequency = dialog.findViewById<ChipGroup>(R.id.cgFrequency)
        val cgTiming = dialog.findViewById<ChipGroup>(R.id.cgTiming)
        val etDurationValue = dialog.findViewById<EditText>(R.id.etDurationValue)
        val spDurationUnit = dialog.findViewById<Spinner>(R.id.spDurationUnit)
        val etInstructions = dialog.findViewById<EditText>(R.id.etInstructions)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSaveMedicine = dialog.findViewById<Button>(R.id.btnSaveMedicine)

        val durationUnits = listOf("Days", "Weeks", "Months")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, durationUnits)
        spDurationUnit.adapter = spinnerAdapter

        if (existingMedicine != null) {
            tvDialogTitle.text = "Edit Medicine"
            etMedicineName.setText(existingMedicine.name)
            etStrength.setText(existingMedicine.strength)
            etDosage.setText(existingMedicine.dosage)
            etDurationValue.setText(existingMedicine.durationValue.toString())
            etInstructions.setText(existingMedicine.instructions)
            val unitIdx = durationUnits.indexOf(existingMedicine.durationUnit).coerceAtLeast(0)
            spDurationUnit.setSelection(unitIdx)
        } else {
            tvDialogTitle.text = "Add Medicine"
            etDurationValue.setText("5")
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSaveMedicine.setOnClickListener {
            val name = etMedicineName.text.toString().trim()
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Please enter medicine name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val strength = etStrength.text.toString().trim()
            val dosage = etDosage.text.toString().trim()
            val instructions = etInstructions.text.toString().trim()
            val durVal = etDurationValue.text.toString().toIntOrNull() ?: 5
            val durUnit = spDurationUnit.selectedItem?.toString() ?: "Days"

            val selectedFreqChipId = cgFrequency.checkedChipId
            val frequencyStr = when (selectedFreqChipId) {
                R.id.chipOnceDaily -> "Once daily"
                R.id.chipTwiceDaily -> "Twice daily"
                R.id.chipThreeDaily -> "Three times daily"
                R.id.chipAsNeeded -> "As needed"
                else -> "Once daily"
            }

            val selectedTimingChipId = cgTiming.checkedChipId
            val timingStr = when (selectedTimingChipId) {
                R.id.chipAfterFood -> "After food"
                R.id.chipBeforeFood -> "Before food"
                R.id.chipWithFood -> "With food"
                R.id.chipBedtime -> "At bedtime"
                else -> "After food"
            }

            val medicine = Medicine(
                medicineId = if (existingMedicine != null) existingMedicine.medicineId else System.currentTimeMillis().toString(),
                name = name,
                strength = strength,
                dosage = if (dosage.isNotBlank()) dosage else "1 tablet",
                frequency = frequencyStr,
                timing = timingStr,
                durationValue = durVal,
                durationUnit = durUnit,
                instructions = instructions
            )

            if (editPosition >= 0 && editPosition < medicinesList.size) {
                medicinesList[editPosition] = medicine
            } else {
                medicinesList.add(medicine)
            }

            medicineAdapter.updateList(medicinesList)
            updateMedicinesEmptyState()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteMedicineConfirmation(position: Int, medicine: Medicine) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Medicine?")
            .setMessage("Remove ${medicine.name} from this prescription?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove") { _, _ ->
                if (position in medicinesList.indices) {
                    medicinesList.removeAt(position)
                    medicineAdapter.updateList(medicinesList)
                    updateMedicinesEmptyState()
                }
            }
            .show()
    }

    private fun updateMedicinesEmptyState() {
        if (medicinesList.isEmpty()) {
            binding.rvMedicines.visibility = View.GONE
            binding.cardEmptyMedicines.visibility = View.VISIBLE
        } else {
            binding.rvMedicines.visibility = View.VISIBLE
            binding.cardEmptyMedicines.visibility = View.GONE
        }
    }

    private fun showPrescriptionPreviewDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_prescription_preview)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvPreviewDoctorName = dialog.findViewById<TextView>(R.id.tvPreviewDoctorName)
        val tvPreviewPatientInfo = dialog.findViewById<TextView>(R.id.tvPreviewPatientInfo)
        val tvPreviewDiagnosis = dialog.findViewById<TextView>(R.id.tvPreviewDiagnosis)
        val layoutPreviewMedicines = dialog.findViewById<LinearLayout>(R.id.layoutPreviewMedicines)
        val tvPreviewInstructions = dialog.findViewById<TextView>(R.id.tvPreviewInstructions)
        val tvPreviewFollowUp = dialog.findViewById<TextView>(R.id.tvPreviewFollowUp)
        val btnClosePreview = dialog.findViewById<Button>(R.id.btnClosePreview)
        val btnConfirmIssue = dialog.findViewById<Button>(R.id.btnConfirmIssue)

        val auth = FirebaseAuth.getInstance()
        val doctorName = auth.currentUser?.displayName ?: "Doctor"
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        tvPreviewDoctorName.text = if (doctorName.startsWith("Dr.")) doctorName else "Dr. $doctorName"
        tvPreviewPatientInfo.text = "Patient: ${if (selectedPatientName.isNotBlank()) selectedPatientName else "Patient"} • Date: $todayStr"
        tvPreviewDiagnosis.text = if (diagnosisList.isNotEmpty()) diagnosisList.joinToString(", ") else "General Consultation"

        layoutPreviewMedicines.removeAllViews()
        if (medicinesList.isNotEmpty()) {
            medicinesList.forEachIndexed { index, med ->
                val tvMed = TextView(requireContext()).apply {
                    val str = if (med.strength.isNotBlank()) "${med.name} ${med.strength}" else med.name
                    text = "${index + 1}. $str\n   Dosage: ${med.dosage} • ${med.frequency} • ${med.timing} (${med.durationValue} ${med.durationUnit})"
                    textSize = 13f
                    setTextColor(android.graphics.Color.parseColor("#202124"))
                    setPadding(0, 4, 0, 8)
                }
                layoutPreviewMedicines.addView(tvMed)
            }
        } else {
            val tvMed = TextView(requireContext()).apply {
                text = "No medicines listed."
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#70757A"))
            }
            layoutPreviewMedicines.addView(tvMed)
        }

        val addInstr = binding.etAdditionalInstructions.text.toString().trim()
        tvPreviewInstructions.text = if (addInstr.isNotBlank()) addInstr else "Take rest and drink fluids."

        val followUp = binding.etFollowUp.text.toString().trim()
        if (followUp.isNotBlank()) {
            tvPreviewFollowUp.visibility = View.VISIBLE
            tvPreviewFollowUp.text = "Recommended Follow-up: $followUp"
        } else {
            tvPreviewFollowUp.visibility = View.GONE
        }

        btnClosePreview.setOnClickListener { dialog.dismiss() }
        btnConfirmIssue.setOnClickListener {
            dialog.dismiss()
            issuePrescription()
        }

        dialog.show()
    }

    private fun issuePrescription() {
        if (selectedPatientName.isBlank()) {
            Toast.makeText(requireContext(), "Please select a patient first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (medicinesList.isEmpty() && diagnosisList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one diagnosis or medicine.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val doctorUid = auth.currentUser?.uid ?: ""
                val doctorName = auth.currentUser?.displayName ?: "Doctor"
                val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                var pUid = selectedPatientUid
                if (pUid.isBlank() && selectedPatientPhone.isNotBlank()) {
                    pUid = patientRepository.getUidByPhone(selectedPatientPhone) ?: ""
                }

                val prescriptionRecord = PrescriptionRecord(
                    appointmentId = appointmentId,
                    patientUid = pUid,
                    patientName = selectedPatientName,
                    doctorUid = doctorUid,
                    doctorName = doctorName,
                    date = todayStr,
                    timestamp = System.currentTimeMillis(),
                    diagnosis = diagnosisList,
                    medicines = medicinesList,
                    instructions = binding.etAdditionalInstructions.text.toString().trim(),
                    followUpDate = binding.etFollowUp.text.toString().trim(),
                    status = "ISSUED"
                )

                patientRepository.savePrescription(prescriptionRecord)
                isIssued = true

                Toast.makeText(requireContext(), "Prescription Issued Successfully!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.localizedMessage ?: "Failed to issue prescription.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
