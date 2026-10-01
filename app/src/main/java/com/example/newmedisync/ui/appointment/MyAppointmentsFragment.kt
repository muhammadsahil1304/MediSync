package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.AppointmentAdapter
import com.example.newmedisync.databinding.FragmentMyAppointmentsBinding
import com.google.android.material.tabs.TabLayout

class MyAppointmentsFragment : Fragment() {

    private var _binding: FragmentMyAppointmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: AppointmentAdapter

    private var currentTabStatus = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupTabs()
        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.loadPatientAppointments()
    }

    private fun setupTabs() {
        binding.tabLayoutStatus.removeAllTabs()
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Upcoming"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Completed"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Cancelled"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("All"))

        binding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabStatus = tab?.text?.toString() ?: "All"
                filterAppointments()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(emptyList()) { appointment ->
            val bundle = Bundle().apply { putString("appointmentId", appointment.appointmentId) }
            findNavController().navigate(R.id.navigation_appointmentDetails, bundle)
        }
        binding.rvAppointments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppointments.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.patientAppointments.observe(viewLifecycleOwner) {
            filterAppointments()
        }
    }

    private fun filterAppointments() {
        val allAppointments = viewModel.patientAppointments.value ?: emptyList()
        val filtered = if (currentTabStatus == "All") {
            allAppointments
        } else {
            allAppointments.filter { it.status.equals(currentTabStatus, ignoreCase = true) }
        }

        adapter.updateList(filtered)
        if (filtered.isEmpty() && viewModel.isLoading.value == false) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvAppointments.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvAppointments.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
