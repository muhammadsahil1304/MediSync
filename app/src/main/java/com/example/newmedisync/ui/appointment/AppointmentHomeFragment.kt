package com.example.newmedisync.ui.appointment

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentAppointmentHomeBinding

class AppointmentHomeFragment : Fragment() {

    private var _binding: FragmentAppointmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupSpecializationChips()
        setupClickListeners()
        setupObservers()

        viewModel.loadPatientAppointments()
    }

    private fun setupClickListeners() {
        binding.cardSearchBar.setOnClickListener {
            findNavController().navigate(R.id.navigation_doctorSearch)
        }

        binding.cardFindDoctor.setOnClickListener {
            findNavController().navigate(R.id.navigation_doctorSearch)
        }

        binding.btnFindDoctor.setOnClickListener {
            findNavController().navigate(R.id.navigation_doctorSearch)
        }

        binding.btnMyAppointments.setOnClickListener {
            findNavController().navigate(R.id.navigation_myAppointments)
        }
    }

    private fun setupSpecializationChips() {
        val specializations = listOf(
            "Cardiology", "Dermatology", "Orthopedics", "Neurology",
            "ENT", "Dentistry", "Pediatrics", "General Physician"
        )

        binding.layoutSpecializationChips.removeAllViews()
        specializations.forEach { spec ->
            val textView = TextView(requireContext())
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(38)
            )
            params.setMargins(0, 0, dpToPx(10), 0)
            textView.layoutParams = params
            textView.background = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
            textView.gravity = Gravity.CENTER
            textView.setPadding(dpToPx(16), 0, dpToPx(16), 0)
            textView.text = spec
            textView.setTextColor(Color.parseColor("#0A6AA1"))
            textView.textSize = 13f

            textView.setOnClickListener {
                viewModel.applyFilter(specialization = spec)
                val bundle = Bundle().apply { putString("specialization", spec) }
                findNavController().navigate(R.id.navigation_doctorSearch, bundle)
            }

            binding.layoutSpecializationChips.addView(textView)
        }
    }

    private fun setupObservers() {
        viewModel.upcomingAppointment.observe(viewLifecycleOwner) { appointment ->
            if (appointment != null) {
                binding.cardUpcoming.root.visibility = View.VISIBLE
                binding.cardEmptyUpcoming.visibility = View.GONE

                val doctorName = if (appointment.doctorName.startsWith("Dr.")) appointment.doctorName else "Dr. ${appointment.doctorName}"
                binding.cardUpcoming.tvDoctorName.text = doctorName
                binding.cardUpcoming.tvSpecialization.text = appointment.doctorSpecialization
                binding.cardUpcoming.tvDateTime.text = "${appointment.date} • ${appointment.timeSlot}"
                binding.cardUpcoming.tvLocation.text = appointment.locationName
                binding.cardUpcoming.tvStatusBadge.text = appointment.status

                binding.cardUpcoming.btnViewDetails.setOnClickListener {
                    val bundle = Bundle().apply { putString("appointmentId", appointment.appointmentId) }
                    findNavController().navigate(R.id.navigation_appointmentDetails, bundle)
                }
            } else {
                binding.cardUpcoming.root.visibility = View.GONE
                binding.cardEmptyUpcoming.visibility = View.VISIBLE
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
