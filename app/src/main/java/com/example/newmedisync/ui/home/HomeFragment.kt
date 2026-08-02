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
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentHomeBinding
import com.example.newmedisync.firebase.DoctorVerificationRepository
import com.example.newmedisync.room.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var isVerified = false

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val firebaseUser =
            FirebaseAuth.getInstance().currentUser

        val doctorName =
            firebaseUser?.displayName ?: "Doctor"
        Log.d("HomeFragment", "Doctor Name: $doctorName")

        binding.tvDoctorName.text =
            "DR. ${doctorName.uppercase()}"

        val currentHour =
            java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY)

        val greeting = when {

            currentHour in 5..11 ->
                "Good Morning,"

            currentHour in 12..16 ->
                "Good Afternoon,"

            currentHour in 17..20 ->
                "Good Evening,"

            else ->
                "Good Night,"
        }

        binding.greetingText.text = greeting
        val database =
            AppDatabase.getDatabase(requireContext())
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        database
            .visitDao()
            .getLatestVisit(uid)
            .observe(viewLifecycleOwner) { visit ->

                if (visit != null) {

                    binding.tvUpcomingPatientName.text =
                        visit.patientName

                    binding.tvUpcomingVisitType.text =
                        "Upcoming Visit"

                    binding.tvUpcomingPurpose.text =
                        visit.purpose

                    binding.tvUpcomingTime.text =
                        visit.visitTime
                }
            }

        database
            .patientDao()
            .getPatientsCount(uid)
            .observe(viewLifecycleOwner) { count ->

                binding.tvTotalPatients.text =
                    count.toString()
            }

        database
            .visitDao()
            .getVisitsCount(uid)
            .observe(viewLifecycleOwner) { count ->
                binding.tvAppointmentsToday.text =
                    "You have $count appointments today"
                binding.tvTodayVisits.text =
                    count.toString()
            }

        setupLogout()
        binding.viewPatients.setOnClickListener {
            checkAccess {
                findNavController().navigate(
                    R.id.action_navigation_home_to_navigation_patient
                )
            }
        }

        binding.cvPrescription.setOnClickListener {
            checkAccess {
                findNavController().navigate(
                    R.id.action_navigation_home_to_navigation_patients_prescription
                )
            }
        }
        binding.addPatients.setOnClickListener {
            checkAccess {
                findNavController().navigate(
                    R.id.action_navigation_home_to_navigation_Add_patients
                )
            }
        }
//        binding.viewPatients.setOnClickListener{
//            findNavController().navigate(
//                R.id.action_navigation_home_to_navigation_patient
//            )
//        }
        binding.btnGetVerified.setOnClickListener {

            findNavController().navigate(
                R.id.action_navigation_home_to_navigation_doctorVerification
            )

        }
        checkVerificationStatus()
        return binding.root
    }

    private fun setupLogout() {

        binding.btnLogout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)

                .setPositiveButton("OK") { dialog, _ ->

                    FirebaseAuth.getInstance().signOut()

                    findNavController().navigate(
                        R.id.action_global_navigation_welcome
                    )

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

                        binding.tvVerificationTitle.text =
                            "🟡 Verification Pending"

                        binding.tvVerificationMessage.text =
                            "Your documents are under review."

                        binding.btnGetVerified.visibility = View.GONE
                    }

                    "APPROVED" -> {
                        isVerified = true
                        binding.cardVerification.visibility = View.GONE
                    }

                    "REJECTED" -> {

                        isVerified = false

                        binding.tvVerificationTitle.text =
                            "🔴 Verification Rejected"

                        binding.tvVerificationMessage.text =
                            "Please update your information and submit again."

                        binding.btnGetVerified.text = "Resubmit"
                    }
                }

            } catch (e: Exception) {

                // No verification submitted yet
                binding.cardVerification.visibility = View.VISIBLE

                binding.tvVerificationTitle.text =
                    "🔴 Account Not Verified"

                binding.tvVerificationMessage.text =
                    "Complete your professional verification."

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
                    findNavController().navigate(
                        R.id.action_navigation_home_to_navigation_doctorVerification
                    )
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