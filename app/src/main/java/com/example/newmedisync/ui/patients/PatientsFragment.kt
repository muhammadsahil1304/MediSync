package com.example.newmedisync.ui.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.PatientEntity
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medisync.model.Patient
import com.example.newmedisync.R
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

        AppDatabase
            .getDatabase(requireContext())
            .patientDao()
            .getAllPatients()
            .observe(viewLifecycleOwner) { patients ->

                binding.recyclerPatients.layoutManager =
                    LinearLayoutManager(requireContext())

                binding.recyclerPatients.adapter =
                    PatientsAdapter(patients)
            }

        binding.fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Add Patient Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvAddPatient.setOnClickListener {
            findNavController().navigate(R.id.navigation_addPatients)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}