package com.example.newmedisync.ui.doctorSchedule

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.ScheduleExceptionAdapter
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
    private lateinit var weeklyAdapter: WeeklyScheduleAdapter
    private lateinit var exceptionAdapter: ScheduleExceptionAdapter

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

        setupRecyclerViews()
        setupListeners()
        setupObservers()

        viewModel.loadDoctorData()
    }

    private fun setupRecyclerViews() {
        weeklyAdapter = WeeklyScheduleAdapter(mutableListOf()) { position, isStartTime, currentText ->
            showTimePicker(currentText) { formattedTime ->
                weeklyAdapter.updateTime(position, isStartTime, formattedTime)
            }
        }
        binding.rvWeeklySchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeeklySchedule.adapter = weeklyAdapter

        exceptionAdapter = ScheduleExceptionAdapter(emptyList()) { exception ->
            showDeleteExceptionConfirmation(exception)
        }
        binding.rvExceptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExceptions.adapter = exceptionAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveSchedule.setOnClickListener {
            val map = weeklyAdapter.getSchedulesMap()
            viewModel.saveSchedule(map)
        }

        binding.btnAddException.setOnClickListener {
            showAddExceptionDialog()
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
                weeklyAdapter.updateSchedules(list)
            }
        }

        viewModel.exceptionsList.observe(viewLifecycleOwner) { exceptions ->
            exceptionAdapter.updateList(exceptions)
            if (exceptions.isEmpty()) {
                binding.rvExceptions.visibility = View.GONE
                binding.cardNoExceptions.visibility = View.VISIBLE
            } else {
                binding.rvExceptions.visibility = View.VISIBLE
                binding.cardNoExceptions.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveSchedule.isEnabled = !isLoading
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            val (_, message) = result
            if (message.isNotBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveResult()
            }
        }
    }

    private fun showAddExceptionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_exception)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnSelectDate = dialog.findViewById<Button>(R.id.btnSelectDate)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rgType)
        val layoutTimeContainer = dialog.findViewById<View>(R.id.layoutTimeContainer)
        val btnStartTime = dialog.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = dialog.findViewById<Button>(R.id.btnEndTime)
        val etReason = dialog.findViewById<EditText>(R.id.etReason)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        var selectedDateStr = ""
        var selectedStartTimeStr = "01:00 PM"
        var selectedEndTimeStr = "02:00 PM"

        btnSelectDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selCal = Calendar.getInstance()
                    selCal.set(year, month, day)
                    selectedDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selCal.time)
                    btnSelectDate.text = selectedDateStr
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbBlockTime) {
                layoutTimeContainer.visibility = View.VISIBLE
            } else {
                layoutTimeContainer.visibility = View.GONE
            }
        }

        btnStartTime.setOnClickListener {
            showTimePicker(selectedStartTimeStr) { formattedTime ->
                selectedStartTimeStr = formattedTime
                btnStartTime.text = formattedTime
            }
        }

        btnEndTime.setOnClickListener {
            showTimePicker(selectedEndTimeStr) { formattedTime ->
                selectedEndTimeStr = formattedTime
                btnEndTime.text = formattedTime
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val isBlockTime = rgType.checkedRadioButtonId == R.id.rbBlockTime
            val type = if (isBlockTime) "BLOCK_TIME" else "FULL_DAY"
            val reason = etReason.text.toString().trim()

            viewModel.addException(
                dateStr = selectedDateStr,
                type = type,
                startTime = selectedStartTimeStr,
                endTime = selectedEndTimeStr,
                reason = reason,
                onConflict = { conflictMsg, _ ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Existing Appointment Conflict")
                        .setMessage(conflictMsg)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("View Appointments") { _, _ ->
                            dialog.dismiss()
                            findNavController().navigate(R.id.navigation_doctorAppointments)
                        }
                        .show()
                }
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteExceptionConfirmation(exception: com.example.newmedisync.model.ScheduleException) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Exception?")
            .setMessage("Remove this availability exception?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteException(exception)
            }
            .show()
    }

    private fun showTimePicker(currentText: String, onTimeSelected: (String) -> Unit) {
        val (hour, minute) = parseHourAndMinute(currentText)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val formattedTime = formatTime12H(picker.hour, picker.minute)
            onTimeSelected(formattedTime)
        }

        picker.show(childFragmentManager, "ScheduleTimePicker")
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
