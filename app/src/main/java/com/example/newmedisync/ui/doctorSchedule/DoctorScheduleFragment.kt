package com.example.newmedisync.ui.doctorSchedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.adapter.WeeklyScheduleAdapter
import com.example.newmedisync.databinding.FragmentDoctorScheduleBinding
import com.example.newmedisync.model.PracticeLocation
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorScheduleFragment : Fragment() {

    private var _binding: FragmentDoctorScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DoctorScheduleViewModel
    private lateinit var adapter: WeeklyScheduleAdapter
    private var locationsList: List<PracticeLocation> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DoctorScheduleViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.loadDoctorData()
    }

    private fun setupRecyclerView() {
        adapter = WeeklyScheduleAdapter(mutableListOf()) { position, isStartTime, currentText ->
            showTimePicker(position, isStartTime, currentText)
        }
        binding.rvWeeklySchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeeklySchedule.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveSchedule.setOnClickListener {
            val map = adapter.getSchedulesMap()
            viewModel.saveSchedule(map)
        }

        binding.spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in locationsList.indices) {
                    val loc = locationsList[position]
                    viewModel.selectLocation(loc)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.practiceLocations.observe(viewLifecycleOwner) { locations ->
            if (locations.isNullOrEmpty()) return@observe

            locationsList = locations
            val locationNames = locations.map { "${it.name} (${it.type})" }
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, locationNames)
            binding.spinnerLocations.adapter = spinnerAdapter
        }

        viewModel.currentScheduleList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                adapter.updateSchedules(list)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveSchedule.isEnabled = !isLoading
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            val (success, message) = result
            if (message.isNotBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveResult()
            }
        }
    }

    private fun showTimePicker(position: Int, isStartTime: Boolean, currentText: String) {
        val (hour, minute) = parseHourAndMinute(currentText)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(if (isStartTime) "Select Start Time" else "Select End Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val formattedTime = formatTime12H(picker.hour, picker.minute)
            adapter.updateTime(position, isStartTime, formattedTime)
        }

        picker.show(childFragmentManager, "TimePicker_$position")
    }

    private fun parseHourAndMinute(timeStr: String): Pair<Int, Int> {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(timeStr) ?: return Pair(9, 0)
            val cal = Calendar.getInstance()
            cal.time = date
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        } catch (e: Exception) {
            Pair(9, 0)
        }
    }

    private fun formatTime12H(hourOfDay: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(cal.time).uppercase(Locale.getDefault())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
