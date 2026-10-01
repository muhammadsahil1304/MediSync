package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.DoctorAdapter
import com.example.newmedisync.databinding.FragmentDoctorSearchBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class DoctorSearchFragment : Fragment() {

    private var _binding: FragmentDoctorSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: DoctorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()

        val argSpec = arguments?.getString("specialization")
        if (!argSpec.isNullOrEmpty()) {
            viewModel.filterSpecialization = argSpec
        }

        viewModel.loadDoctors()
    }

    private fun setupRecyclerView() {
        adapter = DoctorAdapter(emptyList()) { doctor ->
            viewModel.selectDoctor(doctor)
            val bundle = Bundle().apply { putString("doctorId", doctor.uid) }
            findNavController().navigate(R.id.navigation_doctorProfile, bundle)
        }
        binding.rvDoctors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoctors.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.applySearch(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.filteredDoctors.observe(viewLifecycleOwner) { doctors ->
            adapter.updateList(doctors)
            if (doctors.isEmpty() && viewModel.isLoading.value == false) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvDoctors.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvDoctors.visibility = View.VISIBLE
            }
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_doctor_filter, null)
        dialog.setContentView(view)

        val spinnerSpec = view.findViewById<Spinner>(R.id.spinnerSpecialization)
        val etHospital = view.findViewById<EditText>(R.id.etFilterHospital)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val btnReset = view.findViewById<Button>(R.id.btnResetFilter)

        val specs = mutableListOf("All")
        specs.addAll(resources.getStringArray(R.array.doctor_specializations))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, specs)
        spinnerSpec.adapter = adapter

        val currentSpecIndex = specs.indexOf(viewModel.filterSpecialization).coerceAtLeast(0)
        spinnerSpec.setSelection(currentSpecIndex)
        etHospital.setText(viewModel.filterHospital)

        btnApply.setOnClickListener {
            val selectedSpec = spinnerSpec.selectedItem?.toString() ?: "All"
            val hosp = etHospital.text.toString().trim()
            viewModel.applyFilter(specialization = selectedSpec, hospital = hosp)
            dialog.dismiss()
        }

        btnReset.setOnClickListener {
            viewModel.applyFilter(specialization = "All", hospital = "")
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
