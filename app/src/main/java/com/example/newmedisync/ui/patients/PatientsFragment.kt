package com.example.newmedisync.ui.patients

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.PatientEntity
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medisync.model.Patient
import com.example.newmedisync.R
import com.example.newmedisync.adapter.PatientsAdapter
import com.example.newmedisync.databinding.FragmentPatientsBinding
import com.google.firebase.auth.FirebaseAuth

class PatientsFragment : Fragment() {

    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase
            .getDatabase(requireContext())
            .patientDao()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid


        val adapter = PatientsAdapter(emptyList()) { patient ->

            val bundle = Bundle()

            bundle.putString("name", patient.name)
            bundle.putString("phone", patient.phone)
            bundle.putString("age", patient.age)
            bundle.putString("blood", patient.bloodGroup)
            bundle.putString("gender", patient.gender)

            findNavController().navigate(
                R.id.action_navigation_patients_to_navigation_patientProfile,
                bundle
            )
        }

        binding.recyclerPatients.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerPatients.adapter = adapter

        // Default all patients
        dao.getAllPatients(uid).observe(viewLifecycleOwner) { patients ->
            adapter.updateList(patients)
        }

        binding.ivAdd.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_patients_to_navigation_addPatients)
        }
        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                val query = s.toString().trim()

                if (query.isEmpty()) {

                    dao.getAllPatients(uid)
                        .observe(viewLifecycleOwner) {
                            adapter.updateList(it)

                            if (it.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.recyclerPatients.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.recyclerPatients.visibility = View.VISIBLE
                            }
                        }

                } else {

                    dao.searchPatients(query)
                        .observe(viewLifecycleOwner) {
                            adapter.updateList(it)

                            if (it.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.recyclerPatients.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.recyclerPatients.visibility = View.VISIBLE
                            }
                        }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}