package com.example.newmedisync.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.PrescriptionAdapter
import com.example.newmedisync.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]

        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())

        viewModel.prescriptions.observe(viewLifecycleOwner) { prescriptions ->
            if (prescriptions.isNullOrEmpty()) {
                binding.rvReports.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.rvReports.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE

                binding.rvReports.adapter = PrescriptionAdapter(prescriptions) { record ->
                    val bundle = Bundle().apply {
                        putString("prescriptionId", record.id)
                    }
                    findNavController().navigate(R.id.action_reportsFragment_to_patientPrescriptionDetails, bundle)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
