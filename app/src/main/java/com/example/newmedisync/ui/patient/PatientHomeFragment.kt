package com.example.newmedisync.ui.patient

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.adapter.MyDoctorsAdapter
import com.example.newmedisync.databinding.FragmentPatientHomeBinding
import com.example.newmedisync.firebase.NotificationRepository
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.VisitModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class PatientHomeFragment : Fragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PatientViewModel
    private lateinit var myDoctorsAdapter: MyDoctorsAdapter
    private var currentPatient: PatientModel? = null

    private val notificationRepository = NotificationRepository()
    private var notifListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]

        setupMyDoctorsRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadPatient()
    }

    private fun setupMyDoctorsRecyclerView() {
        myDoctorsAdapter = MyDoctorsAdapter(emptyList()) { doctor ->
            val bundle = Bundle().apply {
                putString("doctorId", doctor.doctorId)
            }
            findNavController().navigate(R.id.navigation_doctorProfile, bundle)
        }
        binding.rvMyDoctors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyDoctors.adapter = myDoctorsAdapter
    }

    private fun setupClickListeners() {
        binding.btnNotificationInbox.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_patientHomeFragment_to_editPatientProfile)
        }

        binding.imgPatient.setOnClickListener {
            findNavController().navigate(R.id.action_patientHomeFragment_to_editPatientProfile)
        }

        binding.btnEditEmergency.setOnClickListener {
            findNavController().navigate(R.id.action_patientHomeFragment_to_editPatientProfile)
        }

        binding.btnAddEmergency.setOnClickListener {
            findNavController().navigate(R.id.action_patientHomeFragment_to_editPatientProfile)
        }

        binding.btnCallEmergency.setOnClickListener {
            confirmAndCallEmergencyContact()
        }

        binding.btnFindMoreDoctors.setOnClickListener {
            findNavController().navigate(R.id.navigation_doctorSearch)
        }

        binding.btnBookFollowUp.setOnClickListener {
            val fu = viewModel.followUpItem.value
            if (fu != null && fu.status == "RECOMMENDED") {
                val bundle = Bundle().apply {
                    putString("doctorId", fu.doctorId)
                }
                findNavController().navigate(R.id.navigation_doctorProfile, bundle)
            } else {
                findNavController().navigate(R.id.navigation_myAppointments)
            }
        }

        binding.btnLogout.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_global_navigation_welcome)
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.cardMedicalHistory.setOnClickListener {
            findNavController().navigate(R.id.action_patientHomeFragment_to_medicalHistory)
        }

        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }

        binding.btnGeneratePrescription.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }

        binding.cardBookAppointments.setOnClickListener {
            findNavController().navigate(R.id.navigation_appointmentHome)
        }

        binding.cardTimeline.setOnClickListener {
            findNavController().navigate(R.id.navigation_visits)
        }

        binding.btnViewTimeline.setOnClickListener {
            findNavController().navigate(R.id.navigation_visits)
        }

        binding.btnAskAI.setOnClickListener {
            val prompt = binding.etAiPrompt.text.toString()
            if (prompt.isNotEmpty()) {
                viewModel.askAI(prompt)
                binding.etAiPrompt.text.clear()
            }
        }

        binding.btnScanNow.setOnClickListener {
            findNavController().navigate(R.id.navigation_reportScanner)
        }

        binding.cardAiScan.setOnClickListener {
            findNavController().navigate(R.id.navigation_reportScanner)
        }
    }

    private fun confirmAndCallEmergencyContact() {
        val patient = currentPatient
        val phone = patient?.emergencyPhone?.trim() ?: ""
        val name = patient?.emergencyName?.ifBlank { "Emergency Contact" } ?: "Emergency Contact"

        if (phone.isBlank()) {
            Toast.makeText(requireContext(), "No emergency phone number available.", Toast.LENGTH_SHORT).show()
            return
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Call Emergency Contact")
            .setMessage("Are you sure you want to call $name ($phone)?\n\nThis will open your phone dialer.")
            .setPositiveButton("Call Now") { _, _ ->
                initiatePhoneDial(phone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun initiatePhoneDial(phoneNumber: String) {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(requireContext(), "Invalid phone number format.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
            }
            startActivity(dialIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No dialer application found on this device.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open phone dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uid.isNotBlank()) {
            notifListenerRegistration = notificationRepository.listenToNotifications(uid) { list ->
                _binding?.let {
                    val unreadCount = list.count { !it.isRead }
                    if (unreadCount > 0) {
                        binding.tvUnreadBadge.visibility = View.VISIBLE
                        binding.tvUnreadBadge.text = unreadCount.toString()
                    } else {
                        binding.tvUnreadBadge.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            Log.d("GEMINI REsponse", response)
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("MediSync AI Assistant")
                .setMessage(response)
                .setPositiveButton("OK", null)
                .show()
        }

        viewModel.myDoctors.observe(viewLifecycleOwner) { doctors ->
            if (doctors.isNullOrEmpty()) {
                binding.rvMyDoctors.visibility = View.GONE
                binding.tvNoMyDoctors.visibility = View.VISIBLE
            } else {
                binding.rvMyDoctors.visibility = View.VISIBLE
                binding.tvNoMyDoctors.visibility = View.GONE
                myDoctorsAdapter.updateList(doctors)
            }
        }

        viewModel.followUpItem.observe(viewLifecycleOwner) { fu ->
            if (fu != null) {
                binding.tvFollowUpDate.text = fu.recommendedDate
                binding.tvFollowUpAction.text = "${fu.doctorName} • ${fu.reason.ifBlank { "Recommended Follow-up" }}"
                binding.tvFollowUpStatusBadge.text = fu.status
                if (fu.status == "RECOMMENDED") {
                    binding.btnBookFollowUp.text = "Book Follow-up Appointment"
                    binding.btnBookFollowUp.visibility = View.VISIBLE
                } else if (fu.status == "BOOKED") {
                    binding.btnBookFollowUp.text = "View Appointments"
                    binding.btnBookFollowUp.visibility = View.VISIBLE
                } else {
                    binding.btnBookFollowUp.visibility = View.GONE
                }
            } else {
                binding.tvFollowUpDate.text = "--"
                binding.tvFollowUpAction.text = "No pending follow-up recommendations"
                binding.tvFollowUpStatusBadge.text = "NONE"
                binding.btnBookFollowUp.visibility = View.GONE
            }
        }

        viewModel.healthRisk.observe(viewLifecycleOwner) { risk ->
            binding.progressHeart.progress = risk.first
            binding.progressDiabetes.progress = risk.second
            binding.progressKidney.progress = risk.third
        }

        viewModel.healthScore.observe(viewLifecycleOwner) { score ->
            if (score.first > 0) {
                binding.tvHealthScore.text = score.first.toString()
                binding.tvHealthStatus.text = score.second
            } else {
                binding.tvHealthScore.text = "--"
                binding.tvHealthStatus.text = "Assessment Pending"
            }
        }

        viewModel.aiPrescriptions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotBlank()) {
                binding.tvAiSuggestions.text = suggestions
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvPatientName.text = name
        }

        viewModel.patient.observe(viewLifecycleOwner) { patient ->
            currentPatient = patient
            viewModel.generateAIInsights()

            binding.tvPatientInfo.text = "${patient.age} yrs • ${patient.gender} • ${patient.bloodGroup} • ${patient.height}cm • ${patient.weight}kg"

            if (patient.profileImageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(patient.profileImageUrl)
                    .placeholder(R.drawable.patients)
                    .into(binding.imgPatient)
            }

            // Emergency Contact Card Display
            if (patient.emergencyName.isNotBlank() || patient.emergencyPhone.isNotBlank()) {
                binding.layoutContactInfo.visibility = View.VISIBLE
                binding.btnEditEmergency.visibility = View.VISIBLE
                binding.layoutNoContact.visibility = View.GONE
                binding.tvEmergencyName.text = if (patient.emergencyName.isNotBlank()) patient.emergencyName else "Emergency Contact"
                binding.tvEmergencyPhone.text = if (patient.emergencyPhone.isNotBlank()) patient.emergencyPhone else "No phone provided"
            } else {
                binding.layoutContactInfo.visibility = View.GONE
                binding.btnEditEmergency.visibility = View.GONE
                binding.layoutNoContact.visibility = View.VISIBLE
            }

            // Allergies
            binding.layoutAllergies.removeAllViews()
            if (patient.allergies.isNotEmpty()) {
                patient.allergies.forEach { allergy ->
                    binding.layoutAllergies.addView(createChipView("⚠ $allergy", R.drawable.bg_chip_red, "#D04545"))
                }
                binding.layoutAllergies.visibility = View.VISIBLE
            } else {
                binding.layoutAllergies.visibility = View.GONE
            }

            // Diseases
            binding.layoutDiseases.removeAllViews()
            if (patient.diseases.isNotEmpty()) {
                patient.diseases.forEach { disease ->
                    binding.layoutDiseases.addView(createChipView("⌁ $disease", R.drawable.bg_chip_yellow, "#8A5A00"))
                }
                binding.layoutDiseases.visibility = View.VISIBLE
            } else {
                binding.layoutDiseases.visibility = View.GONE
            }

            // Medications
            if (patient.medications.isNotEmpty()) {
                binding.layoutMedications.visibility = View.VISIBLE
                binding.tvMedication.text = patient.medications.firstOrNull() ?: ""
                binding.tvMedicationFrequency.text = if (patient.medications.size > 1) {
                    "+ ${patient.medications.size - 1} more"
                } else {
                    "Currently Prescribed"
                }
            } else {
                binding.layoutMedications.visibility = View.GONE
            }
        }

        viewModel.allVisits.observe(viewLifecycleOwner) { visits ->
            renderDynamicTimeline(visits)
        }

        viewModel.lastVisit.observe(viewLifecycleOwner) { visit ->
            if (visit != null) {
                binding.tvLastVisitDate.text = formatDate(visit.date)
                binding.tvLastVisitPurpose.text = visit.purpose
            } else {
                binding.tvLastVisitDate.text = "None"
                binding.tvLastVisitPurpose.text = "No past records found"
            }
        }

        viewModel.followUp.observe(viewLifecycleOwner) { visit ->
            if (visit != null) {
                binding.tvFollowUpDate.text = formatDate(visit.date)
                binding.tvFollowUpAction.text = "Upcoming: ${visit.purpose} →"
            } else {
                binding.tvFollowUpDate.text = "--"
                binding.tvFollowUpAction.text = "No scheduled syncs"
            }
        }
    }

    private fun renderDynamicTimeline(visits: List<VisitModel>) {
        binding.layoutDynamicTimeline.removeAllViews()
        if (visits.isEmpty()) {
            binding.layoutDynamicTimeline.visibility = View.GONE
            binding.tvNoVisits.visibility = View.VISIBLE
        } else {
            binding.layoutDynamicTimeline.visibility = View.VISIBLE
            binding.tvNoVisits.visibility = View.GONE

            val topVisits = visits.take(3)
            topVisits.forEach { visit ->
                val dateFormatted = formatDate(visit.date)
                val tvRow = TextView(requireContext()).apply {
                    text = "● $dateFormatted   ${visit.purpose} (Dr. ${visit.doctorName})"
                    setTextColor(Color.parseColor("#0A6AA1"))
                    textSize = 14f
                    setPadding(0, dpToPx(6), 0, dpToPx(6))
                }
                binding.layoutDynamicTimeline.addView(tvRow)
            }
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputSdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputSdf.parse(dateStr)
            date?.let { outputSdf.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun createChipView(text: String, backgroundRes: Int, textColorStr: String): TextView {
        val textView = TextView(context)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(32)
        )
        params.setMargins(0, 0, dpToPx(8), 0)
        textView.layoutParams = params
        textView.background = context?.let { androidx.core.content.ContextCompat.getDrawable(it, backgroundRes) }
        textView.gravity = Gravity.CENTER
        textView.setPadding(dpToPx(14), 0, dpToPx(14), 0)
        textView.text = text
        textView.setTextColor(Color.parseColor(textColorStr))
        textView.textSize = 12f
        return textView
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPatient()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notifListenerRegistration?.remove()
        notifListenerRegistration = null
        _binding = null
    }
}
