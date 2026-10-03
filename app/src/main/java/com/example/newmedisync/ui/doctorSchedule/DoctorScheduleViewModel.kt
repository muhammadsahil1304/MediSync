package com.example.newmedisync.ui.doctorSchedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.model.DaySchedule
import com.example.newmedisync.model.DoctorAvailability
import com.example.newmedisync.model.PracticeLocation
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DoctorScheduleViewModel : ViewModel() {

    private val repository = AppointmentRepository()

    private val _practiceLocations = MutableLiveData<List<PracticeLocation>>()
    val practiceLocations: LiveData<List<PracticeLocation>> = _practiceLocations

    private val _selectedLocation = MutableLiveData<PracticeLocation?>()
    val selectedLocation: LiveData<PracticeLocation?> = _selectedLocation

    private val _currentScheduleList = MutableLiveData<List<DaySchedule>>()
    val currentScheduleList: LiveData<List<DaySchedule>> = _currentScheduleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Pair<Boolean, String>>()
    val saveResult: LiveData<Pair<Boolean, String>> = _saveResult

    private val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    fun loadDoctorData() {
        val doctorUid = repository.getCurrentDoctorUid() ?: return
        _isLoading.value = true

        viewModelScope.launch {
            val doctor = repository.getDoctorById(doctorUid)
            if (doctor != null) {
                val locations = repository.getPracticeLocations(doctor)
                _practiceLocations.value = locations
                if (locations.isNotEmpty()) {
                    selectLocation(locations.first())
                }
            }
            _isLoading.value = false
        }
    }

    fun selectLocation(location: PracticeLocation) {
        _selectedLocation.value = location
        loadAvailabilityForLocation(location)
    }

    private fun loadAvailabilityForLocation(location: PracticeLocation) {
        val doctorUid = repository.getCurrentDoctorUid() ?: return
        _isLoading.value = true

        viewModelScope.launch {
            val savedAvailability = repository.getDoctorAvailability(doctorUid, location.id)
            if (savedAvailability != null && savedAvailability.schedules.isNotEmpty()) {
                val list = daysOfWeek.map { day ->
                    savedAvailability.schedules[day] ?: DaySchedule(dayOfWeek = day, enabled = false)
                }
                _currentScheduleList.value = list
            } else {
                // Default schedule if none saved yet
                val defaultList = daysOfWeek.map { day ->
                    val isDefaultEnabled = day == "Monday" || day == "Wednesday" || day == "Friday"
                    DaySchedule(
                        dayOfWeek = day,
                        enabled = isDefaultEnabled,
                        startTime = "09:00 AM",
                        endTime = "05:00 PM"
                    )
                }
                _currentScheduleList.value = defaultList
            }
            _isLoading.value = false
        }
    }

    fun saveSchedule(schedulesMap: Map<String, DaySchedule>) {
        val doctorUid = repository.getCurrentDoctorUid()
        val location = _selectedLocation.value

        if (doctorUid == null || location == null) {
            _saveResult.value = Pair(false, "Invalid doctor or location")
            return
        }

        val enabledDays = schedulesMap.values.filter { it.enabled }
        if (enabledDays.isEmpty()) {
            _saveResult.value = Pair(false, "Please enable at least one day in your schedule")
            return
        }

        // Validate start time < end time for each enabled day
        for (schedule in enabledDays) {
            val startMin = parseTimeToMinutes(schedule.startTime)
            val endMin = parseTimeToMinutes(schedule.endTime)

            if (startMin == null || endMin == null) {
                _saveResult.value = Pair(false, "Invalid time format for ${schedule.dayOfWeek}")
                return
            }

            if (endMin <= startMin) {
                _saveResult.value = Pair(false, "End time must be later than start time for ${schedule.dayOfWeek}")
                return
            }
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val availability = DoctorAvailability(
                    id = "${doctorUid}_${location.id}",
                    doctorId = doctorUid,
                    locationId = location.id,
                    locationName = location.name,
                    schedules = schedulesMap
                )

                repository.saveDoctorAvailability(availability)
                _isLoading.value = false
                _saveResult.value = Pair(true, "Availability updated successfully.")
            } catch (e: Exception) {
                _isLoading.value = false
                _saveResult.value = Pair(false, "Unable to update availability. Please try again.")
            }
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(timeStr) ?: return null
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        } catch (e: Exception) {
            null
        }
    }

    fun resetSaveResult() {
        _saveResult.value = Pair(false, "")
    }
}
