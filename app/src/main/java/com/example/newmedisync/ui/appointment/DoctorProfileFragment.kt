package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.adapter.PracticeLocationAdapter
import com.example.newmedisync.databinding.FragmentDoctorProfileBinding

class DoctorProfileFragment : Fragment() {

    private var _binding: FragmentDoctorProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var locationAdapter: PracticeLocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupListeners()
        setupObservers()

        val doctorId = arguments?.getString("doctorId")
        if (!doctorId.isNullOrEmpty() && viewModel.selectedDoctor.value?.uid != doctorId) {
            viewModel.selectDoctorById(doctorId)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBookAppointment.setOnClickListener {
            val location = viewModel.selectedLocation.value
            if (location == null) {
                Toast.makeText(requireContext(), "Please select a practice location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.navigation_selectSlot)
        }
    }

    private fun setupObservers() {
        viewModel.selectedDoctor.observe(viewLifecycleOwner) { doctor ->
            if (doctor == null) return@observe

            val name = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}"
            binding.tvDoctorName.text = name
            binding.tvSpecialization.text = doctor.specialization
            binding.tvQualification.text = if (doctor.qualification.isNotBlank()) doctor.qualification else "Medical Specialist"
            binding.tvExperience.text = if (doctor.experience.isNotBlank()) "${doctor.experience} Yrs" else "Experienced"
            binding.tvRegistration.text = if (doctor.registrationNumber.isNotBlank()) doctor.registrationNumber else "Verified"
            binding.tvBio.text = if (doctor.bio.isNotBlank()) doctor.bio else "Experienced medical professional dedicated to providing compassionate, evidence-based care."

            if (doctor.status == "APPROVED") {
                binding.tvVerifiedBadge.visibility = View.VISIBLE
            } else {
                binding.tvVerifiedBadge.visibility = View.GONE
            }

            if (doctor.profileImageUrl.isNotBlank()) {
                Glide.with(this)
                    .load(doctor.profileImageUrl)
                    .placeholder(R.drawable.people)
                    .into(binding.imgDoctor)
            } else {
                binding.imgDoctor.setImageResource(R.drawable.people)
            }
        }

        viewModel.practiceLocations.observe(viewLifecycleOwner) { locations ->
            if (locations.isNullOrEmpty()) return@observe

            locationAdapter = PracticeLocationAdapter(locations) { selectedLoc ->
                viewModel.selectLocation(selectedLoc)
            }
            binding.rvPracticeLocations.layoutManager = LinearLayoutManager(requireContext())
            binding.rvPracticeLocations.adapter = locationAdapter

            if (viewModel.selectedLocation.value == null && locations.isNotEmpty()) {
                viewModel.selectLocation(locations.first())
            }
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                binding.tvFeeBottom.text = "₹${location.consultationFee.toInt()}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
