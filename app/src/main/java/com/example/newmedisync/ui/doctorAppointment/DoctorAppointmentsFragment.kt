package com.example.newmedisync.ui.doctorAppointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.DoctorAppointmentAdapter
import com.example.newmedisync.databinding.FragmentDoctorAppointmentsBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.model.Appointment
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorAppointmentsFragment : Fragment() {

    private var _binding: FragmentDoctorAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val repository = AppointmentRepository()
    private lateinit var adapter: DoctorAppointmentAdapter

    private var allAppointments: List<Appointment> = emptyList()
    private var currentTab = "Today"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupRecyclerView()
        setupListeners()
        loadDoctorAppointments()
    }

    private fun setupTabs() {
        binding.tabLayoutStatus.removeAllTabs()
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Today"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Upcoming"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Completed"))
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText("Cancelled"))

        binding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.text?.toString() ?: "Today"
                filterAndDisplayAppointments()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = DoctorAppointmentAdapter(emptyList()) { appointment ->
            val bundle = Bundle().apply { putString("appointmentId", appointment.appointmentId) }
            findNavController().navigate(R.id.navigation_doctorAppointmentDetails, bundle)
        }
        binding.rvAppointments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppointments.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadDoctorAppointments() {
        val doctorUid = repository.getCurrentDoctorUid()
        if (doctorUid == null) {
            Toast.makeText(requireContext(), "Doctor authentication required", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        repository.listenDoctorAppointments(
            doctorId = doctorUid,
            onUpdate = { list ->
                _binding?.let {
                    binding.progressBar.visibility = View.GONE
                    allAppointments = list
                    filterAndDisplayAppointments()
                }
            },
            onError = { exception ->
                _binding?.let {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load appointments: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun filterAndDisplayAppointments() {
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

        val filteredList = when (currentTab) {
            "Today" -> {
                allAppointments.filter { it.date.equals(todayStr, ignoreCase = true) }
                    .sortedBy { it.timeSlot }
            }
            "Upcoming" -> {
                allAppointments.filter { it.status.equals("Upcoming", ignoreCase = true) }
                    .sortedWith(compareBy({ it.timestamp }, { it.timeSlot }))
            }
            "Completed" -> {
                allAppointments.filter { it.status.equals("Completed", ignoreCase = true) }
                    .sortedByDescending { it.timestamp }
            }
            "Cancelled" -> {
                allAppointments.filter { it.status.equals("Cancelled", ignoreCase = true) }
                    .sortedByDescending { it.timestamp }
            }
            else -> allAppointments
        }

        adapter.updateList(filteredList)

        if (filteredList.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvAppointments.visibility = View.GONE

            when (currentTab) {
                "Today" -> binding.tvEmptyTitle.text = "No appointments today"
                "Upcoming" -> binding.tvEmptyTitle.text = "No upcoming appointments"
                "Completed" -> binding.tvEmptyTitle.text = "No completed appointments"
                "Cancelled" -> binding.tvEmptyTitle.text = "No cancelled appointments"
            }
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
