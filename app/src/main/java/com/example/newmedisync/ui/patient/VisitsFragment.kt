package com.example.newmedisync.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.adapter.TimelineAdapter
import com.example.newmedisync.databinding.FragmentVisitsBinding

class VisitsFragment : Fragment() {

    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]

        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        
        viewModel.allVisits.observe(viewLifecycleOwner) { visits ->
            binding.rvTimeline.adapter = TimelineAdapter(visits)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}