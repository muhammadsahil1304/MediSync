package com.example.newmedisync.ui.appointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newmedisync.adapter.DateItem
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.Appointment
import com.example.newmedisync.model.DoctorVerification
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.PracticeLocation
import com.example.newmedisync.model.TimeSlot
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppointmentViewModel : ViewModel() {

    private val appointmentRepository = AppointmentRepository()
    private val patientRepository = PatientRepository()

    // All registered doctors
    private val _doctors = MutableLiveData<List<DoctorVerification>>()
    val doctors: LiveData<List<DoctorVerification>> = _doctors

    // Filtered doctors
    private val _filteredDoctors = MutableLiveData<List<DoctorVerification>>()
    val filteredDoctors: LiveData<List<DoctorVerification>> = _filteredDoctors

    // Selected Doctor
    private val _selectedDoctor = MutableLiveData<DoctorVerification?>()
    val selectedDoctor: LiveData<DoctorVerification?> = _selectedDoctor

    // Practice Locations for selected doctor
    private val _practiceLocations = MutableLiveData<List<PracticeLocation>>()
    val practiceLocations: LiveData<List<PracticeLocation>> = _practiceLocations

    // Selected Practice Location
    private val _selectedLocation = MutableLiveData<PracticeLocation?>()
    val selectedLocation: LiveData<PracticeLocation?> = _selectedLocation

    // Available Dates
    private val _availableDates = MutableLiveData<List<DateItem>>()
    val availableDates: LiveData<List<DateItem>> = _availableDates

    // Selected Date
    private val _selectedDate = MutableLiveData<DateItem?>()
    val selectedDate: LiveData<DateItem?> = _selectedDate

    // Available Time Slots for selected date
    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots: LiveData<List<TimeSlot>> = _timeSlots

    private val _slotEmptyMessage = MutableLiveData<String>()
    val slotEmptyMessage: LiveData<String> = _slotEmptyMessage

    // Selected Time Slot
    private val _selectedTimeSlot = MutableLiveData<TimeSlot?>()
    val selectedTimeSlot: LiveData<TimeSlot?> = _selectedTimeSlot

    // Current Patient Model
    private val _patientModel = MutableLiveData<PatientModel?>()
    val patientModel: LiveData<PatientModel?> = _patientModel

    private val _patientName = MutableLiveData<String>()
    val patientName: LiveData<String> = _patientName

    // Patient Appointments List
    private val _patientAppointments = MutableLiveData<List<Appointment>>()
    val patientAppointments: LiveData<List<Appointment>> = _patientAppointments

    // Upcoming Appointment for Home
    private val _upcomingAppointment = MutableLiveData<Appointment?>()
    val upcomingAppointment: LiveData<Appointment?> = _upcomingAppointment

    // Selected Single Appointment Details
    private val _selectedAppointment = MutableLiveData<Appointment?>()
    val selectedAppointment: LiveData<Appointment?> = _selectedAppointment

    // Loading & Operation State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _bookingResult = MutableLiveData<Pair<Boolean, String>>()
    val bookingResult: LiveData<Pair<Boolean, String>> = _bookingResult

    // Active Search Query & Filters
    var currentQuery: String = ""
    var filterSpecialization: String = "All"
    var filterHospital: String = ""

    fun loadDoctors() {
        _isLoading.value = true
        viewModelScope.launch {
            val list = appointmentRepository.getRegisteredDoctors()
            _doctors.value = list
            applyFilter()
            _isLoading.value = false
        }
    }

    fun applySearch(query: String) {
        currentQuery = query
        applyFilter()
    }

    fun applyFilter(specialization: String = filterSpecialization, hospital: String = filterHospital) {
        filterSpecialization = specialization
        filterHospital = hospital

        val all = _doctors.value ?: emptyList()
        val filtered = all.filter { doc ->
            val matchesQuery = if (currentQuery.isBlank()) true else {
                doc.fullName.contains(currentQuery, ignoreCase = true) ||
                        doc.specialization.contains(currentQuery, ignoreCase = true) ||
                        doc.clinicName.contains(currentQuery, ignoreCase = true) ||
                        doc.clinicAddress.contains(currentQuery, ignoreCase = true)
            }

            val matchesSpec = if (filterSpecialization == "All" || filterSpecialization.isBlank()) true else {
                doc.specialization.equals(filterSpecialization, ignoreCase = true)
            }

            val matchesHosp = if (filterHospital.isBlank()) true else {
                doc.clinicName.contains(filterHospital, ignoreCase = true) ||
                        doc.clinicAddress.contains(filterHospital, ignoreCase = true)
            }

            matchesQuery && matchesSpec && matchesHosp
        }
        _filteredDoctors.value = filtered
    }

    fun selectDoctor(doctor: DoctorVerification) {
        _selectedDoctor.value = doctor
        _selectedLocation.value = null
        _selectedDate.value = null
        _selectedTimeSlot.value = null
        loadPracticeLocations(doctor)
    }

    fun selectDoctorById(doctorId: String) {
        viewModelScope.launch {
            val doctor = appointmentRepository.getDoctorById(doctorId)
            if (doctor != null) {
                selectDoctor(doctor)
            }
        }
    }

    private fun loadPracticeLocations(doctor: DoctorVerification) {
        viewModelScope.launch {
            val locations = appointmentRepository.getPracticeLocations(doctor)
            _practiceLocations.value = locations
            if (locations.size == 1) {
                _selectedLocation.value = locations.first()
            }
        }
    }

    fun selectLocation(location: PracticeLocation) {
        _selectedLocation.value = location
    }

    fun generateAvailableDates() {
        val doctor = _selectedDoctor.value ?: return
        val location = _selectedLocation.value ?: return

        viewModelScope.launch {
            val availability = appointmentRepository.getDoctorAvailability(doctor.uid, location.id)
            val exceptions = appointmentRepository.getDoctorExceptions(doctor.uid, location.id)

            val dateItems = mutableListOf<DateItem>()
            val calendar = Calendar.getInstance()

            val fullSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
            val fullDaySdf = SimpleDateFormat("EEEE", Locale.getDefault())
            val dayNumSdf = SimpleDateFormat("dd", Locale.getDefault())
            val monthSdf = SimpleDateFormat("MMM", Locale.getDefault())

            val availableDays = location.availableDays

            for (i in 0 until 14) {
                val date = calendar.time
                val dateString = fullSdf.format(date)
                val dayOfWeek = dayOfWeekSdf.format(date).uppercase(Locale.getDefault())
                val fullDayName = fullDaySdf.format(date)
                val dayNum = dayNumSdf.format(date)
                val monthStr = monthSdf.format(date).uppercase(Locale.getDefault())

                val normDateStr = normalizeDateString(dateString)

                val fullDayExc = exceptions.firstOrNull { exc ->
                    normalizeDateString(exc.date) == normDateStr && exc.type.equals("FULL_DAY", ignoreCase = true)
                }

                val isDayAvailable = if (fullDayExc != null) {
                    false
                } else if (availability != null && availability.schedules.isNotEmpty()) {
                    val daySched = availability.schedules[fullDayName]
                    daySched?.enabled == true
                } else if (availableDays.isNotEmpty()) {
                    availableDays.any { dayOfWeek.startsWith(it, ignoreCase = true) }
                } else {
                    true
                }

                dateItems.add(
                    DateItem(
                        date = date,
                        dateString = dateString,
                        dayOfWeek = dayOfWeek,
                        dayNum = dayNum,
                        monthStr = monthStr,
                        isAvailable = isDayAvailable
                    )
                )
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            _availableDates.value = dateItems

            if (_selectedDate.value == null) {
                val firstAvail = dateItems.firstOrNull { it.isAvailable }
                if (firstAvail != null) {
                    selectDate(firstAvail)
                }
            }
        }
    }

    fun selectDate(dateItem: DateItem) {
        _selectedDate.value = dateItem
        _selectedTimeSlot.value = null
        loadAvailableTimeSlots(dateItem)
    }

    private fun loadAvailableTimeSlots(dateItem: DateItem) {
        val doctor = _selectedDoctor.value ?: return
        val location = _selectedLocation.value ?: return
        val dateStr = dateItem.dateString
        val normDateStr = normalizeDateString(dateStr)

        val fullDaySdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val fullDayName = fullDaySdf.format(dateItem.date)

        viewModelScope.launch {
            val availability = appointmentRepository.getDoctorAvailability(doctor.uid, location.id)
            val exceptions = appointmentRepository.getDoctorExceptions(doctor.uid, location.id)

            val fullDayExc = exceptions.firstOrNull { exc ->
                normalizeDateString(exc.date) == normDateStr && exc.type.equals("FULL_DAY", ignoreCase = true)
            }

            if (fullDayExc != null) {
                _timeSlots.value = emptyList()
                val reasonSuffix = if (fullDayExc.reason.isNotBlank()) " (${fullDayExc.reason})" else ""
                _slotEmptyMessage.value = "Doctor is unavailable on $normDateStr$reasonSuffix."
                return@launch
            }

            val daySched = availability?.schedules?.get(fullDayName)

            if (availability != null && (daySched == null || !daySched.enabled)) {
                _timeSlots.value = emptyList()
                _slotEmptyMessage.value = "No appointments available on $fullDayName."
                return@launch
            }

            val startStr = daySched?.startTime ?: "09:00 AM"
            val endStr = daySched?.endTime ?: "05:00 PM"

            val startMin = parseTimeToMinutes(startStr) ?: 540
            val endMin = parseTimeToMinutes(endStr) ?: 1020

            val isToday = isTodayDate(dateItem.date)
            val currentMin = if (isToday) {
                val now = Calendar.getInstance()
                now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            } else {
                -1
            }

            val blockTimeExceptions = exceptions.filter { exc ->
                normalizeDateString(exc.date) == normDateStr && exc.type.equals("BLOCK_TIME", ignoreCase = true)
            }

            val candidateSlots = mutableListOf<String>()
            var currMin = startMin
            val slotDuration = 30

            while (currMin + slotDuration <= endMin) {
                val slotEndMin = currMin + slotDuration

                val isPast = isToday && currMin <= currentMin

                val isBlockedByException = blockTimeExceptions.any { exc ->
                    val excStartMin = parseTimeToMinutes(exc.startTime) ?: -1
                    val excEndMin = parseTimeToMinutes(exc.endTime) ?: -1
                    if (excStartMin != -1 && excEndMin != -1) {
                        currMin < excEndMin && slotEndMin > excStartMin
                    } else {
                        false
                    }
                }

                if (!isPast && !isBlockedByException) {
                    candidateSlots.add(formatMinutesTo12H(currMin))
                }

                currMin += slotDuration
            }

            if (candidateSlots.isEmpty()) {
                _timeSlots.value = emptyList()
                _slotEmptyMessage.value = if (isToday) "No remaining slots for today." else "No appointments available on this day."
                return@launch
            }

            val bookedSlots = appointmentRepository.getBookedTimeSlots(doctor.uid, dateStr)

            val slots = candidateSlots.map { time ->
                TimeSlot(
                    time = time,
                    isAvailable = !bookedSlots.contains(time)
                )
            }

            val hasAvailableSlot = slots.any { it.isAvailable }
            if (!hasAvailableSlot) {
                _slotEmptyMessage.value = "No available slots for this day."
            } else {
                _slotEmptyMessage.value = ""
            }

            _timeSlots.value = slots
        }
    }

    private fun normalizeDateString(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr.trim()
            sdf.format(date)
        } catch (e: Exception) {
            dateStr.trim()
        }
    }

    private fun isTodayDate(date: java.util.Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
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

    private fun formatMinutesTo12H(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, mins)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(cal.time).uppercase(Locale.getDefault())
    }

    fun selectTimeSlot(slot: TimeSlot) {
        _selectedTimeSlot.value = slot
    }

    fun loadPatientProfile() {
        viewModelScope.launch {
            try {
                val user = patientRepository.getCurrentUser()
                _patientName.value = user.name
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        patientRepository.getCurrentPatient(
            onSuccess = { _patientModel.value = it },
            onFailure = { }
        )
    }

    fun confirmBooking() {
        val doctor = _selectedDoctor.value
        val location = _selectedLocation.value
        val date = _selectedDate.value
        val slot = _selectedTimeSlot.value
        val patientUid = appointmentRepository.getCurrentPatientUid()

        if (doctor == null || location == null || date == null || slot == null || patientUid == null) {
            _bookingResult.value = Pair(false, "Incomplete appointment details")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val exceptions = appointmentRepository.getDoctorExceptions(doctor.uid, location.id)
                val normDateStr = normalizeDateString(date.dateString)

                val fullDayExc = exceptions.firstOrNull { exc ->
                    normalizeDateString(exc.date) == normDateStr && exc.type.equals("FULL_DAY", ignoreCase = true)
                }

                if (fullDayExc != null) {
                    _isLoading.value = false
                    _bookingResult.value = Pair(false, "Doctor is unavailable on this date. Please select another date.")
                    generateAvailableDates()
                    return@launch
                }

                val slotMin = parseTimeToMinutes(slot.time) ?: -1
                val blockExc = exceptions.firstOrNull { exc ->
                    if (normalizeDateString(exc.date) == normDateStr && exc.type.equals("BLOCK_TIME", ignoreCase = true)) {
                        val sMin = parseTimeToMinutes(exc.startTime) ?: -1
                        val eMin = parseTimeToMinutes(exc.endTime) ?: -1
                        slotMin >= sMin && slotMin < eMin
                    } else {
                        false
                    }
                }

                if (blockExc != null) {
                    _isLoading.value = false
                    _bookingResult.value = Pair(false, "This time slot is no longer available. Please select another slot.")
                    loadAvailableTimeSlots(date)
                    return@launch
                }

                // Double-booking check: verify slot is still free in Firestore
                val currentBooked = appointmentRepository.getBookedTimeSlots(doctor.uid, date.dateString)
                if (currentBooked.contains(slot.time)) {
                    _isLoading.value = false
                    _bookingResult.value = Pair(false, "This time slot has just been booked by another patient. Please select a different time slot.")
                    loadAvailableTimeSlots(date)
                    return@launch
                }

                val appointment = Appointment(
                    patientId = patientUid,
                    patientName = _patientName.value ?: "Patient",
                    doctorId = doctor.uid,
                    doctorName = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}",
                    doctorSpecialization = doctor.specialization,
                    doctorProfileImageUrl = doctor.profileImageUrl,
                    practiceLocationId = location.id,
                    locationName = location.name,
                    locationAddress = location.address,
                    date = date.dateString,
                    timeSlot = slot.time,
                    timestamp = date.date.time,
                    status = "Upcoming",
                    consultationFee = location.consultationFee
                )

                val appointmentId = appointmentRepository.bookAppointment(appointment)

                // Sync linked follow-up recommendation
                try {
                    val followUpRepo = com.example.newmedisync.firebase.FollowUpRepository()
                    val pendingFollowUps = followUpRepo.getPatientFollowUps(patientUid)
                        .filter { it.doctorId == doctor.uid && it.status == "RECOMMENDED" }

                    val nearestFollowUp = pendingFollowUps.firstOrNull()
                    if (nearestFollowUp != null) {
                        followUpRepo.updateFollowUpStatus(nearestFollowUp.followUpId, "BOOKED", appointmentId)
                    }

                    // Persistent Notification for Doctor
                    val notif = com.example.newmedisync.model.NotificationItem(
                        recipientUid = doctor.uid,
                        recipientRole = "doctor",
                        title = "Follow-up Appointment Booked 📅",
                        message = "${_patientName.value ?: "A patient"} has booked a follow-up appointment for ${date.dateString} at ${slot.time}.",
                        type = "APPOINTMENT_BOOKED",
                        relatedAppointmentId = appointmentId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    com.example.newmedisync.firebase.NotificationRepository().saveNotification(notif)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _isLoading.value = false
                _bookingResult.value = Pair(true, appointmentId)
            } catch (e: Exception) {
                _isLoading.value = false
                _bookingResult.value = Pair(false, e.localizedMessage ?: "Failed to confirm appointment")
            }
        }
    }

    fun loadPatientAppointments() {
        val uid = appointmentRepository.getCurrentPatientUid() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val list = appointmentRepository.getPatientAppointments(uid)
            _patientAppointments.value = list
            _upcomingAppointment.value = list.firstOrNull { it.status == "Upcoming" }
            _isLoading.value = false
        }
    }

    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            val appointment = appointmentRepository.getAppointmentById(appointmentId)
            _selectedAppointment.value = appointment
        }
    }

    fun resetBookingState() {
        _bookingResult.value = Pair(false, "")
    }
}
