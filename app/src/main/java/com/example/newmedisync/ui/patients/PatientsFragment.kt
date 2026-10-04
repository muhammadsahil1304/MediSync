package com.example.newmedisync.ui.patients

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.PatientsAdapter
import com.example.newmedisync.databinding.FragmentPatientsBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.model.DoctorPatientItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PatientsFragment : Fragment() {

    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!

    private val appointmentRepository = AppointmentRepository()
    private lateinit var adapter: PatientsAdapter

    private var allPatients: List<DoctorPatientItem> = emptyList()
    private var searchQuery: String = ""

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

        setupRecyclerView()
        setupListeners()
        loadDoctorPatients()
    }

    private fun setupRecyclerView() {
        adapter = PatientsAdapter(emptyList()) { patient ->
            val bundle = Bundle().apply {
                putString("patientUid", patient.patientUid)
                putString("name", patient.name)
                putString("phone", patient.phone)
                putString("age", if (patient.age > 0) "${patient.age}" else "")
                putString("blood", patient.bloodGroup)
                putString("gender", patient.gender)
            }
            findNavController().navigate(
                R.id.action_navigation_patients_to_navigation_patientProfile,
                bundle
            )
        }

        binding.recyclerPatients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPatients.adapter = adapter
    }

    private fun setupListeners() {
        binding.ivAdd.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_patients_to_navigation_addPatients)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterAndDisplayPatients()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDoctorPatients() {
        val doctorUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val list = appointmentRepository.getDoctorPatients(doctorUid)
                _binding?.let {
                    allPatients = list
                    filterAndDisplayPatients()
                }
            } catch (e: Exception) {
                _binding?.let {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.recyclerPatients.visibility = View.GONE
                }
            }
        }
    }

    private fun filterAndDisplayPatients() {
        val filtered = if (searchQuery.isBlank()) {
            allPatients
        } else {
            allPatients.filter { p ->
                p.name.contains(searchQuery, ignoreCase = true) ||
                        p.phone.contains(searchQuery, ignoreCase = true) ||
                        p.email.contains(searchQuery, ignoreCase = true) ||
                        p.bloodGroup.contains(searchQuery, ignoreCase = true)
            }
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerPatients.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerPatients.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
