package com.example.newmedisync.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.firebase.AIRepository
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.PrescriptionRecord
import com.example.newmedisync.model.VisitModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientViewModel : ViewModel() {

    private val repository = PatientRepository()
    private val aiRepository = AIRepository()

    private val _patient = MutableLiveData<PatientModel>()
    val patient: LiveData<PatientModel> = _patient

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _lastVisit = MutableLiveData<VisitModel?>()
    val lastVisit: LiveData<VisitModel?> = _lastVisit

    private val _followUp = MutableLiveData<VisitModel?>()
    val followUp: LiveData<VisitModel?> = _followUp

    private val _allVisits = MutableLiveData<List<VisitModel>>()
    val allVisits: LiveData<List<VisitModel>> = _allVisits

    private val _prescriptions = MutableLiveData<List<PrescriptionRecord>>()
    val prescriptions: LiveData<List<PrescriptionRecord>> = _prescriptions

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    private val _healthRisk = MutableLiveData<Triple<Int, Int, Int>>() // Heart, Diabetes, Kidney
    val healthRisk: LiveData<Triple<Int, Int, Int>> = _healthRisk

    private val _healthScore = MutableLiveData<Pair<Int, String>>() // Score, Status
    val healthScore: LiveData<Pair<Int, String>> = _healthScore

    private val _aiPrescriptions = MutableLiveData<String>()
    val aiPrescriptions: LiveData<String> = _aiPrescriptions

    fun askAI(prompt: String) {
        viewModelScope.launch {
            _aiResponse.value = "Thinking..."
            _aiResponse.value = aiRepository.getClinicalAdvice(prompt)
        }
    }

    fun generateAIInsights() {
        val p = _patient.value ?: return
        val data = "Age: ${p.age}, Gender: ${p.gender}, Blood: ${p.bloodGroup}, Height: ${p.height}, Weight: ${p.weight}, Allergies: ${p.allergies}, Diseases: ${p.diseases}"
        
        viewModelScope.launch {
            // Risk Matrix
            val riskJson = aiRepository.analyzeHealthRisk(data)
            try {
                val heart = riskJson.substringAfter("\"heart\":").substringBefore(",").trim().filter { it.isDigit() }.toInt()
                val diabetes = riskJson.substringAfter("\"diabetes\":").substringBefore(",").trim().filter { it.isDigit() }.toInt()
                val kidney = riskJson.substringAfter("\"kidney\":").substringBefore("}").trim().filter { it.isDigit() }.toInt()
                _healthRisk.postValue(Triple(heart, diabetes, kidney))
            } catch (e: Exception) {}

            // Health Score
            val scoreResult = aiRepository.getHealthScore(data)
            try {
                val parts = scoreResult.split(",")
                val score = parts[0].trim().filter { it.isDigit() }.toInt()
                val status = parts[1].trim()
                _healthScore.postValue(Pair(score, status))
            } catch (e: Exception) {}

            // Prescription Suggestions
            _aiPrescriptions.postValue(aiRepository.getPrescriptionSuggestions(data))
        }
    }

    fun loadPatient() {
        viewModelScope.launch {
            try {
                val user = repository.getCurrentUser()
                _userName.value = user.name
                
                loadVisits(user.uid)
                loadPrescriptions(user.uid)
            } catch (e: Exception) {
                // Handle error
            }
        }

        repository.getCurrentPatient(
            onSuccess = {
                _patient.value = it
            },
            onFailure = {
            }
        )
    }

    private fun loadVisits(uid: String) {
        viewModelScope.launch {
            // Sort locally to avoid Firestore index requirement
            val visits = repository.getPatientVisits(uid).sortedByDescending { it.timestamp }
            _allVisits.value = visits
            val now = System.currentTimeMillis()
            
            android.util.Log.d("PatientViewModel", "Loaded ${visits.size} visits for $uid. Current time: $now")

            val pastVisits = visits.filter { it.timestamp <= now }
            val futureVisits = visits.filter { it.timestamp > now }.sortedBy { it.timestamp }

            _lastVisit.value = pastVisits.firstOrNull()
            _followUp.value = futureVisits.firstOrNull()
        }
    }

    private fun loadPrescriptions(uid: String) {
        viewModelScope.launch {
            // Sort locally to avoid Firestore index requirement
            val prescriptions = repository.getPatientPrescriptions(uid).sortedByDescending { it.timestamp }
            _prescriptions.value = prescriptions
        }
    }
}