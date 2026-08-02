package com.example.newmedisync.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
            binding.rvReports.adapter = PrescriptionAdapter(prescriptions) { record ->
                Toast.makeText(context, "Opening prescription from ${record.date}", Toast.LENGTH_SHORT).show()
                // Implementation for full screen view or download
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
