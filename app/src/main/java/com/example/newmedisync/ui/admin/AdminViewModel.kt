package com.example.newmedisync.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newmedisync.firebase.DoctorVerificationRepository
import com.example.newmedisync.model.DoctorVerification
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repository = DoctorVerificationRepository()

    private val _pendingDoctors =
        MutableLiveData<List<DoctorVerification>>()

    val pendingDoctors: LiveData<List<DoctorVerification>>
        get() = _pendingDoctors

    fun loadPendingDoctors() {

        viewModelScope.launch {

            try {

                val doctors = repository.getPendingDoctors()

                _pendingDoctors.value = doctors

            } catch (e: Exception) {

                _pendingDoctors.value = emptyList()

            }

        }

    }

}