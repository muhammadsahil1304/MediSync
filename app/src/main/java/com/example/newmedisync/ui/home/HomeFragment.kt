package com.example.newmedisync.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.DoctorAppointmentAdapter
import com.example.newmedisync.databinding.FragmentHomeBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.firebase.DoctorVerificationRepository
import com.example.newmedisync.room.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var isVerified = false

    private val binding get() = _binding!!
    private val appointmentRepository = AppointmentRepository()
    private lateinit var todayAdapter: DoctorAppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val doctorName = firebaseUser?.displayName ?: "Doctor"
        Log.d("HomeFragment", "Doctor Name: $doctorName")

        binding.tvDoctorName.text = "DR. ${doctorName.uppercase()}"

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            currentHour in 5..11 -> "Good Morning,"
            currentHour in 12..16 -> "Good Afternoon,"
            currentHour in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }

        binding.greetingText.text = greeting

        val database = AppDatabase.getDatabase(requireContext())
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        database.patientDao().getPatientsCount(uid).observe(viewLifecycleOwner) { count ->
            binding.tvTotalPatients.text = count.toString()
        }

        setupRecyclerViewAndListeners()
        setupLogout()

        binding.viewPatients.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_patient)
            }
        }

        binding.cvPrescription.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_patients_prescription)
            }
        }

        binding.cvAppointments.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_doctorAppointments)
            }
        }

        binding.btnSeeAllAppointments.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_doctorAppointments)
            }
        }

        binding.cvSchedule.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_doctorSchedule)
            }
        }

        binding.addPatients.setOnClickListener {
            checkAccess {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_Add_patients)
            }
        }

        binding.btnGetVerified.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_doctorVerification)
        }

        checkVerificationStatus()
        loadTodayAppointments()

        return binding.root
    }

    private fun setupRecyclerViewAndListeners() {
        todayAdapter = DoctorAppointmentAdapter(emptyList()) { appointment ->
            checkAccess {
                val bundle = Bundle().apply { putString("appointmentId", appointment.appointmentId) }
                findNavController().navigate(R.id.navigation_doctorAppointmentDetails, bundle)
            }
        }
        binding.rvTodayAppointments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayAppointments.adapter = todayAdapter
    }

    private fun loadTodayAppointments() {
        val doctorUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        appointmentRepository.listenDoctorAppointments(
            doctorId = doctorUid,
            onUpdate = { allAppointments ->
                _binding?.let {
                    val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                    val todayAppts = allAppointments.filter { appt ->
                        appt.date.equals(todayStr, ignoreCase = true) && appt.status.equals("Upcoming", ignoreCase = true)
                    }.sortedBy { it.timeSlot }

                    binding.tvAppointmentsToday.text = "You have ${todayAppts.size} appointments today"
                    binding.tvTodayVisits.text = todayAppts.size.toString()

                    todayAdapter.updateList(todayAppts)

                    if (todayAppts.isEmpty()) {
                        binding.rvTodayAppointments.visibility = View.GONE
                        binding.cardNoTodayAppointments.visibility = View.VISIBLE
                    } else {
                        binding.rvTodayAppointments.visibility = View.VISIBLE
                        binding.cardNoTodayAppointments.visibility = View.GONE
                    }
                }
            },
            onError = { }
        )
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_global_navigation_welcome)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun checkVerificationStatus() {
        val repository = DoctorVerificationRepository()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val doctor = repository.getDoctorVerification(uid)

                when (doctor.status) {
                    "PENDING" -> {
                        isVerified = false
                        binding.tvVerificationTitle.text = "🟡 Verification Pending"
                        binding.tvVerificationMessage.text = "Your documents are under review."
                        binding.btnGetVerified.visibility = View.GONE
                    }
                    "APPROVED" -> {
                        isVerified = true
                        binding.cardVerification.visibility = View.GONE
                    }
                    "REJECTED" -> {
                        isVerified = false
                        binding.tvVerificationTitle.text = "🔴 Verification Rejected"
                        binding.tvVerificationMessage.text = "Please update your information and submit again."
                        binding.btnGetVerified.text = "Resubmit"
                    }
                }
            } catch (e: Exception) {
                binding.cardVerification.visibility = View.VISIBLE
                binding.tvVerificationTitle.text = "🔴 Account Not Verified"
                binding.tvVerificationMessage.text = "Complete your professional verification."
                binding.btnGetVerified.visibility = View.VISIBLE
                binding.btnGetVerified.text = "Get Verified"
            }
        }
    }

    private fun checkAccess(action: () -> Unit) {
        if (isVerified) {
            action()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Verification Required")
                .setMessage("Your account must be verified before you can use this feature.")
                .setPositiveButton("Get Verified") { _, _ ->
                    findNavController().navigate(R.id.action_navigation_home_to_navigation_doctorVerification)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
