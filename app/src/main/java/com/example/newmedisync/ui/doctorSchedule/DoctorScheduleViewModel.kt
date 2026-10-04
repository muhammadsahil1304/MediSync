package com.example.newmedisync.ui.doctorSchedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.model.Appointment
import com.example.newmedisync.model.DaySchedule
import com.example.newmedisync.model.DoctorAvailability
import com.example.newmedisync.model.PracticeLocation
import com.example.newmedisync.model.ScheduleException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorScheduleViewModel : ViewModel() {

    private val repository = AppointmentRepository()

    private val _practiceLocations = MutableLiveData<List<PracticeLocation>>()
    val practiceLocations: LiveData<List<PracticeLocation>> = _practiceLocations

    private val _selectedLocation = MutableLiveData<PracticeLocation?>()
    val selectedLocation: LiveData<PracticeLocation?> = _selectedLocation

    private val _currentScheduleList = MutableLiveData<List<DaySchedule>>()
    val currentScheduleList: LiveData<List<DaySchedule>> = _currentScheduleList

    private val _exceptionsList = MutableLiveData<List<ScheduleException>>()
    val exceptionsList: LiveData<List<ScheduleException>> = _exceptionsList

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
        loadExceptionsForLocation(location)
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

    fun loadExceptionsForLocation(location: PracticeLocation) {
        val doctorUid = repository.getCurrentDoctorUid() ?: return
        viewModelScope.launch {
            val list = repository.getDoctorExceptions(doctorUid, location.id)
            _exceptionsList.value = list
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

    fun addException(
        dateStr: String,
        type: String,
        startTime: String,
        endTime: String,
        reason: String,
        onConflict: (String, List<Appointment>) -> Unit
    ) {
        val doctorUid = repository.getCurrentDoctorUid() ?: return
        val location = _selectedLocation.value ?: return

        if (dateStr.isBlank()) {
            _saveResult.value = Pair(false, "Please select a date")
            return
        }

        // Validate date is not in the past
        val dateVal = try {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }

        if (dateVal == null) {
            _saveResult.value = Pair(false, "Invalid date format")
            return
        }

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val selectedCal = Calendar.getInstance().apply { time = dateVal }

        if (selectedCal.before(todayCal)) {
            _saveResult.value = Pair(false, "Please select a future date.")
            return
        }

        var startMin = 0
        var endMin = 0

        if (type.equals("BLOCK_TIME", ignoreCase = true)) {
            val sMin = parseTimeToMinutes(startTime)
            val eMin = parseTimeToMinutes(endTime)

            if (sMin == null || eMin == null) {
                _saveResult.value = Pair(false, "Invalid time range")
                return
            }

            if (eMin <= sMin) {
                _saveResult.value = Pair(false, "End time must be later than start time.")
                return
            }

            startMin = sMin
            endMin = eMin
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Conflict detection with existing upcoming appointments
                val upcomingAppts = repository.getUpcomingAppointmentsForDate(doctorUid, location.id, dateStr)

                if (type.equals("FULL_DAY", ignoreCase = true) && upcomingAppts.isNotEmpty()) {
                    _isLoading.value = false
                    onConflict(
                        "This date has ${upcomingAppts.size} existing appointment(s). Please handle or cancel these appointments before marking the day unavailable.",
                        upcomingAppts
                    )
                    return@launch
                }

                if (type.equals("BLOCK_TIME", ignoreCase = true) && upcomingAppts.isNotEmpty()) {
                    val conflictingAppt = upcomingAppts.firstOrNull { appt ->
                        val apptMin = parseTimeToMinutes(appt.timeSlot)
                        apptMin != null && apptMin >= startMin && apptMin < endMin
                    }

                    if (conflictingAppt != null) {
                        _isLoading.value = false
                        onConflict(
                            "This time period has an existing appointment at ${conflictingAppt.timeSlot} with ${conflictingAppt.patientName}. You cannot block this time until the appointment is handled.",
                            listOf(conflictingAppt)
                        )
                        return@launch
                    }
                }

                val exception = ScheduleException(
                    doctorId = doctorUid,
                    locationId = location.id,
                    locationName = location.name,
                    date = dateStr,
                    type = type,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason
                )

                repository.saveDoctorException(exception)
                loadExceptionsForLocation(location)
                _isLoading.value = false
                _saveResult.value = Pair(true, "Schedule exception saved successfully.")
            } catch (e: Exception) {
                _isLoading.value = false
                _saveResult.value = Pair(false, "Failed to save schedule exception.")
            }
        }
    }

    fun deleteException(exception: ScheduleException) {
        val doctorUid = repository.getCurrentDoctorUid() ?: return
        val location = _selectedLocation.value ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteDoctorException(doctorUid, exception.id)
                loadExceptionsForLocation(location)
                _isLoading.value = false
                _saveResult.value = Pair(true, "Schedule exception removed successfully.")
            } catch (e: Exception) {
                _isLoading.value = false
                _saveResult.value = Pair(false, "Failed to remove schedule exception.")
            }
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(timeStr) ?: return null
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            null
        }
    }

    fun resetSaveResult() {
        _saveResult.value = Pair(false, "")
    }
}
