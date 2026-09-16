package com.example.newmedisync.ui.patient

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentPatientHomeBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class PatientHomeFragment : Fragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PatientViewModel

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

        setupObservers()
        setupClickListeners()

        viewModel.loadPatient()
    }

    private fun setupClickListeners() {
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

        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }

        binding.cardTimeline.setOnClickListener {
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

    private fun setupObservers() {
        viewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            Log.d("GEMINI REsponse",response)
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("MediSync AI Assistant")
                .setMessage(response)
                .setPositiveButton("OK", null)
                .show()
        }

        viewModel.healthRisk.observe(viewLifecycleOwner) { risk ->
            binding.progressHeart.progress = risk.first
            // Assuming other progress bars have IDs or I'll just use these for now
        }

        viewModel.healthScore.observe(viewLifecycleOwner) { score ->
            binding.tvHealthScore.text = score.first.toString()
            // Status update
        }

        viewModel.aiPrescriptions.observe(viewLifecycleOwner) { suggestions ->
            binding.tvAiSuggestions.text = suggestions
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvPatientName.text = name
        }

        viewModel.patient.observe(viewLifecycleOwner) { patient ->
            viewModel.generateAIInsights()
            // Update Basic Info
            binding.tvPatientInfo.text = "${patient.age} yrs • ${patient.gender} • ${patient.bloodGroup} • ${patient.height}cm • ${patient.weight}kg"

            // Profile Image
            if (patient.profileImageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(patient.profileImageUrl)
                    .placeholder(R.drawable.patients)
                    .into(binding.imgPatient)
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
        _binding = null
    }
}
