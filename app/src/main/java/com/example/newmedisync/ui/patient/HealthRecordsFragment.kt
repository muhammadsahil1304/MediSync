package com.example.newmedisync.ui.patient

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.UnifiedHealthRecordsAdapter
import com.example.newmedisync.databinding.FragmentHealthRecordsBinding
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.firebase.ReportRepository
import com.example.newmedisync.model.HealthRecordItem
import com.example.newmedisync.model.HealthRecordType
import com.example.newmedisync.model.VisitModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRecordsFragment : Fragment() {

    private var _binding: FragmentHealthRecordsBinding? = null
    private val binding get() = _binding!!

    private val patientRepository = PatientRepository()
    private val reportRepository = ReportRepository()

    private lateinit var adapter: UnifiedHealthRecordsAdapter

    private var allHealthRecords: List<HealthRecordItem> = emptyList()
    private var currentFilter = HealthRecordType.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadUnifiedHealthRecords()
    }

    private fun setupRecyclerView() {
        adapter = UnifiedHealthRecordsAdapter(emptyList()) { item ->
            onRecordClicked(item)
        }
        binding.rvHealthRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHealthRecords.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnScanReport.setOnClickListener {
            findNavController().navigate(R.id.navigation_reportScanner)
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chipPrescriptions) -> HealthRecordType.PRESCRIPTION
                checkedIds.contains(R.id.chipReports) -> HealthRecordType.REPORT
                checkedIds.contains(R.id.chipVisits) -> HealthRecordType.VISIT
                else -> HealthRecordType.ALL
            }
            filterAndDisplayRecords()
        }
    }

    private fun loadUnifiedHealthRecords() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val prescriptions = patientRepository.getPatientPrescriptions(uid)
                val reports = reportRepository.getPatientReports()
                val visits = patientRepository.getPatientVisits(uid)

                val recordItems = mutableListOf<HealthRecordItem>()

                // Process Prescriptions
                prescriptions.forEach { presc ->
                    val docName = if (presc.doctorName.startsWith("Dr.")) presc.doctorName else "Dr. ${presc.doctorName}"
                    val diagStr = if (presc.diagnosis.isNotEmpty()) "Diagnosis: ${presc.diagnosis.joinToString(", ")}" else "Prescription"
                    val medCountStr = if (presc.medicines.isNotEmpty()) " • ${presc.medicines.size} Medicines" else ""

                    recordItems.add(
                        HealthRecordItem(
                            id = presc.id,
                            type = HealthRecordType.PRESCRIPTION,
                            title = docName,
                            subtitle = "$diagStr$medCountStr",
                            dateStr = presc.date,
                            timestamp = presc.timestamp,
                            badgeText = "PRESCRIPTION",
                            rawPrescription = presc
                        )
                    )
                }

                // Process Scanned Reports
                reports.forEach { report ->
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val dateStr = if (report.createdAt > 0) sdf.format(Date(report.createdAt)) else "Scanned Report"
                    val summaryStr = report.analysis?.summary?.ifBlank {
                        report.extractedText.take(80).replace("\n", " ")
                    } ?: "Scanned Medical Report Analysis"

                    recordItems.add(
                        HealthRecordItem(
                            id = report.reportId,
                            type = HealthRecordType.REPORT,
                            title = report.reportName.ifBlank { "Medical Lab Report" },
                            subtitle = summaryStr,
                            dateStr = dateStr,
                            timestamp = report.createdAt,
                            badgeText = "SCANNED REPORT",
                            rawReport = report
                        )
                    )
                }

                // Process Visits
                visits.forEach { visit ->
                    val docName = if (visit.doctorName.startsWith("Dr.")) visit.doctorName else "Dr. ${visit.doctorName}"
                    val purposeStr = if (visit.diagnosis.isNotBlank()) "Diagnosis: ${visit.diagnosis}" else if (visit.purpose.isNotBlank()) visit.purpose else "Consultation Visit"

                    recordItems.add(
                        HealthRecordItem(
                            id = visit.id,
                            type = HealthRecordType.VISIT,
                            title = purposeStr,
                            subtitle = "$docName • ${visit.time}",
                            dateStr = visit.date,
                            timestamp = visit.timestamp,
                            badgeText = "VISIT",
                            rawVisit = visit
                        )
                    )
                }

                // Sort newest first
                allHealthRecords = recordItems.sortedByDescending { it.timestamp }

                _binding?.let {
                    binding.progressBar.visibility = View.GONE

                    // Update Overview Counts
                    binding.tvPrescriptionCount.text = prescriptions.size.toString()
                    binding.tvReportCount.text = reports.size.toString()
                    binding.tvVisitCount.text = visits.size.toString()

                    filterAndDisplayRecords()
                }

            } catch (e: Exception) {
                _binding?.let {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error loading health records: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterAndDisplayRecords() {
        val filtered = if (currentFilter == HealthRecordType.ALL) {
            allHealthRecords
        } else {
            allHealthRecords.filter { it.type == currentFilter }
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.rvHealthRecords.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE

            binding.tvEmptyTitle.text = when (currentFilter) {
                HealthRecordType.PRESCRIPTION -> "No Prescriptions Found"
                HealthRecordType.REPORT -> "No Scanned Reports Found"
                HealthRecordType.VISIT -> "No Visit Records Found"
                else -> "No Health Records Found"
            }
        } else {
            binding.rvHealthRecords.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun onRecordClicked(item: HealthRecordItem) {
        when (item.type) {
            HealthRecordType.PRESCRIPTION -> {
                val presc = item.rawPrescription ?: return
                val bundle = Bundle().apply {
                    putString("prescriptionId", presc.id)
                }
                findNavController().navigate(R.id.navigation_patientPrescriptionDetails, bundle)
            }
            HealthRecordType.REPORT -> {
                val report = item.rawReport ?: return
                showReportDetailsDialog(report)
            }
            HealthRecordType.VISIT -> {
                val visit = item.rawVisit ?: return
                showVisitDetailsDialog(visit)
            }
            else -> {}
        }
    }

    private fun showReportDetailsDialog(report: com.example.newmedisync.model.MedicalReport) {
        val sb = StringBuilder()
        sb.append("Report Name: ${report.reportName.ifBlank { "Medical Report" }}\n")

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        if (report.createdAt > 0) {
            sb.append("Uploaded: ${sdf.format(Date(report.createdAt))}\n\n")
        }

        if (report.analysis != null) {
            val a = report.analysis
            if (a.summary.isNotBlank()) sb.append("AI SUMMARY:\n${a.summary}\n\n")
            if (a.abnormalFindings.isNotEmpty()) sb.append("ABNORMAL FINDINGS:\n${a.abnormalFindings.joinToString("\n• ", "• ")}\n\n")
            if (a.recommendations.isNotEmpty()) sb.append("RECOMMENDATIONS:\n${a.recommendations.joinToString("\n• ", "• ")}\n\n")
            if (a.followUp.isNotBlank()) sb.append("FOLLOW-UP: ${a.followUp}\n")
        } else if (report.extractedText.isNotBlank()) {
            sb.append("EXTRACTED TEXT:\n${report.extractedText}")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Scanned Report Details")
            .setMessage(sb.toString())
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showVisitDetailsDialog(visit: VisitModel) {
        val docName = if (visit.doctorName.startsWith("Dr.")) visit.doctorName else "Dr. ${visit.doctorName}"
        val sb = StringBuilder()
        sb.append("Doctor: $docName\n")
        sb.append("Date & Time: ${visit.date} ${visit.time}\n\n")

        if (visit.chiefComplaint.isNotBlank()) sb.append("CHIEF COMPLAINT:\n${visit.chiefComplaint}\n\n")
        if (visit.symptoms.isNotBlank()) sb.append("SYMPTOMS:\n${visit.symptoms}\n\n")
        if (visit.diagnosis.isNotBlank()) sb.append("DIAGNOSIS:\n${visit.diagnosis}\n\n")
        if (visit.treatmentPlan.isNotBlank()) sb.append("TREATMENT PLAN & ADVICE:\n${visit.treatmentPlan}\n\n")

        val vitalsList = mutableListOf<String>()
        if (visit.bpSystolic > 0 && visit.bpDiastolic > 0) vitalsList.add("BP: ${visit.bpSystolic}/${visit.bpDiastolic} mmHg")
        if (visit.pulseRate > 0) vitalsList.add("Pulse: ${visit.pulseRate} bpm")
        if (visit.temperature > 0.0) vitalsList.add("Temp: ${visit.temperature} °F")
        if (visit.spO2 > 0) vitalsList.add("SpO2: ${visit.spO2}%")
        if (visit.respiratoryRate > 0) vitalsList.add("Resp Rate: ${visit.respiratoryRate} bpm")
        if (visit.height > 0.0) vitalsList.add("Height: ${visit.height} cm")
        if (visit.weight > 0.0) vitalsList.add("Weight: ${visit.weight} kg")

        if (vitalsList.isNotEmpty()) {
            sb.append("RECORDED VITALS:\n${vitalsList.joinToString("\n• ", "• ")}\n\n")
        }

        if (visit.followUpDate.isNotBlank()) {
            sb.append("RECOMMENDED FOLLOW-UP: ${visit.followUpDate}\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Consultation Visit Details")
            .setMessage(sb.toString().trim())
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
