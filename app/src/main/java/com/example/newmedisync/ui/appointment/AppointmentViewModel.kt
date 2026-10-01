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
        val dateItems = mutableListOf<DateItem>()
        val calendar = Calendar.getInstance()

        val fullSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumSdf = SimpleDateFormat("dd", Locale.getDefault())
        val monthSdf = SimpleDateFormat("MMM", Locale.getDefault())

        val location = _selectedLocation.value
        val availableDays = location?.availableDays ?: emptyList()

        for (i in 0 until 14) {
            val date = calendar.time
            val dateString = fullSdf.format(date)
            val dayOfWeek = dayOfWeekSdf.format(date).uppercase(Locale.getDefault())
            val dayNum = dayNumSdf.format(date)
            val monthStr = monthSdf.format(date).uppercase(Locale.getDefault())

            val isDayAvailable = if (availableDays.isEmpty()) true else {
                availableDays.any { dayOfWeek.startsWith(it, ignoreCase = true) }
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
    }

    fun selectDate(dateItem: DateItem) {
        _selectedDate.value = dateItem
        _selectedTimeSlot.value = null
        loadAvailableTimeSlots(dateItem)
    }

    private fun loadAvailableTimeSlots(dateItem: DateItem) {
        val doctor = _selectedDoctor.value ?: return
        val dateStr = dateItem.dateString

        viewModelScope.launch {
            val bookedSlots = appointmentRepository.getBookedTimeSlots(doctor.uid, dateStr)

            val baseSlots = listOf(
                "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
                "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM"
            )

            val slots = baseSlots.map { time ->
                TimeSlot(
                    time = time,
                    isAvailable = !bookedSlots.contains(time)
                )
            }
            _timeSlots.value = slots
        }
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
