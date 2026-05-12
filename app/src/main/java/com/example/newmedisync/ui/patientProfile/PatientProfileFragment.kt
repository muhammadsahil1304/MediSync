package com.example.newmedisync.ui.patientProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.adapter.VisitHistoryAdapter
import com.example.newmedisync.databinding.FragmentPatientProfileBinding
import com.example.newmedisync.model.VisitHistory

class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!
    private var patientName = ""
    private var patientPhone = ""
    private var patientAge = ""
    private var patientBlood = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {

            patientName = it.getString("name", "")
            patientPhone = it.getString("phone", "")
            patientAge = it.getString("age", "")
            patientBlood = it.getString("blood", "")
        }

        _binding =
            FragmentPatientProfileBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPatientName.text = patientName
        binding.tvPhone.text = patientPhone
        binding.tvAge.text = patientAge
        binding.tvBlood.text = patientBlood

        val historyList = listOf(

            VisitHistory(
                "Oct 12, 2024",
                "GENERAL CHECKUP",
                "Patient reported mild fatigue and vitamin D deficiency. Prescribed supplements and advised lifestyle adjustments.",
                "COMPLETED"
            ),

            VisitHistory(
                "Aug 24, 2024",
                "IMMUNIZATION",
                "Annual flu shot administered. No adverse reactions observed during the 15-minute wait period.",
                "COMPLETED"
            ),

            VisitHistory(
                "Jun 15, 2024",
                "SPECIALIST REFERRAL",
                "Referred to dermatology for persistent rash and skin irritation.",
                "PENDING"
            )
        )

        binding.recyclerHistory.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerHistory.adapter =
            VisitHistoryAdapter(historyList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}