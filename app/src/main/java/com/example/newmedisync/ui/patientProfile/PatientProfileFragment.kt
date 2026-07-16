package com.example.newmedisync.ui.patientProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.newmedisync.R
import com.example.newmedisync.adapter.VisitHistoryAdapter
import com.example.newmedisync.databinding.FragmentPatientProfileBinding
import com.example.newmedisync.model.VisitHistory
import com.example.newmedisync.room.AppDatabase
import android.app.Dialog
import android.net.Uri
import android.util.Log
import android.view.Window
import android.widget.ImageView
import com.example.newmedisync.room.VisitEntity
import java.io.File
class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!
    private var patientName = ""
    private var patientPhone = ""
    private var patientAge = ""
    private var patientBlood = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {

            patientName = it.getString("name", "")
            patientPhone = it.getString("phone", "")
            patientAge = it.getString("age", "")
            patientBlood = it.getString("blood", "")
        }

        _binding =
            FragmentPatientProfileBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPatientName.text = patientName
        binding.tvPhone.text = patientPhone
        binding.tvAge.text = patientAge
        binding.tvBlood.text = patientBlood

        val historyList = listOf(

            VisitHistory(
                "Oct 12, 2024",
                "GENERAL CHECKUP",
                "Patient reported mild fatigue and vitamin D deficiency. Prescribed supplements and advised lifestyle adjustments.",
                "COMPLETED"
            ),

            VisitHistory(
                "Aug 24, 2024",
                "IMMUNIZATION",
                "Annual flu shot administered. No adverse reactions observed during the 15-minute wait period.",
                "COMPLETED"
            ),

            VisitHistory(
                "Jun 15, 2024",
                "SPECIALIST REFERRAL",
                "Referred to dermatology for persistent rash and skin irritation.",
                "PENDING"
            )
        )

        AppDatabase
            .getDatabase(requireContext())
            .prescriptionDao()
            .getPrescriptionsByPatient(patientName)
            .observe(viewLifecycleOwner) { prescriptions ->

                binding.recyclerHistory.layoutManager =
                    LinearLayoutManager(requireContext())

                binding.recyclerHistory.adapter =
                    VisitHistoryAdapter(prescriptions) { prescription ->

                        val bundle = Bundle()

                        bundle.putString(
                            "imagePath",
                            prescription.imagePath
                        )

                        binding.recyclerHistory.adapter =
                            VisitHistoryAdapter(prescriptions) { prescription ->

                                showPrescriptionDialog(
                                    prescription.imagePath
                                )
                            }
                    }
            }
        AppDatabase
            .getDatabase(requireContext())
            .prescriptionDao()
            .getLatestPrescription(patientName)
            .observe(viewLifecycleOwner) { prescription ->

                if (prescription != null) {

                    binding.tvLastVisit.text =
                        prescription.visitDate

                } else {

                    binding.tvLastVisit.text = "--"
                }
            }
        binding.btnVisit.setOnClickListener {

            showAddVisitDialog()
        }
        binding.btnPrescription.setOnClickListener {

            val bundle = Bundle()

            bundle.putString("name", patientName)
            bundle.putString("phone", patientPhone)

            findNavController().navigate(
                R.id.action_patientProfileFragment_to_navigation_prescription,
                bundle
            )
        }
    }
    private fun showPrescriptionDialog(
        imagePath: String
    ) {

        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(
            R.layout.dialog_prescription_preview
        )

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val imagePrescription =
            dialog.findViewById<ImageView>(
                R.id.imagePrescription
            )

        val btnClose =
            dialog.findViewById<ImageView>(
                R.id.btnClose
            )

        imagePrescription.setImageURI(
            Uri.fromFile(File(imagePath))
        )

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showAddVisitDialog() {

        val dialog = Dialog(requireContext())

        dialog.setContentView(
            R.layout.dialog_visit_preview
        )

        val etVisitDate =
            dialog.findViewById<EditText>(
                R.id.etVisitDate
            )

        val etVisitTime =
            dialog.findViewById<EditText>(
                R.id.etVisitTime
            )

        val etPurpose =
            dialog.findViewById<EditText>(
                R.id.etPurpose
            )

        val btnSaveVisit =
            dialog.findViewById<Button>(
                R.id.btnSaveVisit
            )

        val calendar = Calendar.getInstance()

        etVisitDate.setOnClickListener {

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    val selectedDate =
                        "$day/${month + 1}/$year"

                    etVisitDate.setText(selectedDate)

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etVisitTime.setOnClickListener {

            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->

                    etVisitTime.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hour,
                            minute
                        )
                    )

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        btnSaveVisit.setOnClickListener {

            val visit = VisitEntity(

                patientName = patientName,

                patientPhone = patientPhone,

                visitDate = etVisitDate.text.toString(),

                visitTime = etVisitTime.text.toString(),

                purpose = etPurpose.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {

                AppDatabase
                    .getDatabase(requireContext())
                    .visitDao()
                    .insertVisit(visit)
            }
            Log.d("VISIT", "Inserted: $visit")
            dialog.dismiss()
        }

        dialog.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}