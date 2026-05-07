package com.example.newmedisync.ui.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medisync.model.Patient
import com.example.newmedisync.adapter.PatientsAdapter
import com.example.newmedisync.databinding.FragmentPatientsBinding

class PatientsFragment : Fragment() {

    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val patients = listOf(
            Patient("JD", "John Doe", "#882193", "555-0123", "10 Oct 2026", "ACTIVE"),
            Patient("AS", "Alice Smith", "#990124", "555-0987", "08 Oct 2026", "PENDING"),
            Patient("RJ", "Robert Johnson", "#774321", "555-4432", "05 Oct 2026", "ACTIVE")
        )

        binding.recyclerPatients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPatients.adapter = PatientsAdapter(patients)

        binding.fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Add Patient Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvAddPatient.setOnClickListener {
            Toast.makeText(requireContext(), "Add Patient Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}