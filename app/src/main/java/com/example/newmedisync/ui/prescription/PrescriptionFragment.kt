package com.example.newmedisync.ui.prescription

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment

import android.widget.ArrayAdapter
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.PatientEntity
import com.example.newmedisync.R
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import com.example.newmedisync.databinding.FragmentPrescriptionBinding

class PrescriptionBoardFragment : Fragment() {

    private var _binding: FragmentPrescriptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppDatabase
            .getDatabase(requireContext())
            .patientDao()
            .getAllPatients()
            .observe(viewLifecycleOwner) { patients ->

                setupPatientsSpinner(patients)
            }

        binding.blueColor.setOnClickListener {

            selectBlue()
            selectPen()

            binding.drawingView.setBrushColor(
                Color.parseColor("#0A70A2")
            )
        }

        binding.redColor.setOnClickListener {

            selectRed()
            selectPen()

            binding.drawingView.setBrushColor(Color.RED)
        }

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
        }

        binding.btnErase.setOnClickListener {

            selectEraser()

            binding.drawingView.setBrushColor(Color.WHITE)
        }

        binding.btnFullscreen.setOnClickListener {

            binding.fullscreenContainer.visibility = View.VISIBLE

            binding.fullscreenDrawingView.setDrawPath(
                binding.drawingView.getDrawPath()
            )

            binding.fullscreenDrawingView.setBrushColor(
                Color.parseColor("#0A70A2")
            )
        }
        binding.btnCloseFullscreen.setOnClickListener {

            binding.drawingView.setDrawPath(
                binding.fullscreenDrawingView.getDrawPath()
            )

            binding.fullscreenContainer.visibility = View.GONE
        }
        requireActivity().findViewById<View>(R.id.nav_view)
            ?.visibility = View.GONE

        requireActivity().findViewById<View>(R.id.nav_view)
            ?.visibility = View.VISIBLE

        binding.btnSaveGallery.setOnClickListener {
            if(binding.spPatients.selectedItemPosition == 0) {
                Toast.makeText(
                    requireContext(),
                    "Please select a patient",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            savePrescriptionAsPdf()
        }

        selectPen()
        selectBlue()
    }
    private fun selectPen() {

        binding.btnPen.setBackgroundResource(
            R.drawable.bg_tool_selected
        )

        binding.btnErase.setBackgroundResource(
            R.drawable.bg_tool_unselected
        )

        binding.btnPen.setColorFilter(Color.WHITE)

        binding.btnErase.setColorFilter(
            Color.parseColor("#777777")
        )
    }
    private fun selectEraser() {

        binding.btnErase.setBackgroundResource(
            R.drawable.bg_tool_selected
        )

        binding.btnPen.setBackgroundResource(
            R.drawable.bg_tool_unselected
        )

        binding.btnErase.setColorFilter(Color.WHITE)

        binding.btnPen.setColorFilter(
            Color.parseColor("#777777")
        )
    }
    private fun selectRed() {

        binding.redColor.setBackgroundResource(
            R.drawable.bg_red_selected
        )

        binding.blueColor.setBackgroundResource(
            R.drawable.bg_blue_unselected
        )
    }
    private fun selectBlue() {

        binding.blueColor.setBackgroundResource(
            R.drawable.bg_blue_selected
        )

        binding.redColor.setBackgroundResource(
            R.drawable.bg_red_unselected
        )
    }
    private fun setupPatientsSpinner(
        patients: List<PatientEntity>
    ) {

        val patientNames = mutableListOf<String>()

        patientNames.add("Select Patient")

        patientNames.addAll(
            patients.map { it.name }
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_selected,
            patientNames
        )

        adapter.setDropDownViewResource(
            R.layout.item_spinner_dropdown
        )

        binding.spPatients.adapter = adapter

        binding.spPatients.adapter = adapter

        binding.spPatients.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position == 0) {
                        binding.etPatientPhone.setText("")
                        return
                    }

                    val selectedPatient =
                        patients[position - 1]

                    binding.etPatientPhone.setText(
                        selectedPatient.phone
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }
    private fun savePrescriptionAsPdf() {

        try {

            val content = binding.drawingView

            val bitmap = Bitmap.createBitmap(
                content.width,
                content.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)

            content.draw(canvas)

            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()

            val page = pdfDocument.startPage(pageInfo)

            val pdfCanvas = page.canvas

            pdfCanvas.drawBitmap(
                bitmap,
                0f,
                0f,
                null
            )

            pdfDocument.finishPage(page)

            val directory = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ),
                "MediSync"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(
                directory,
                "Prescription_${System.currentTimeMillis()}.pdf"
            )

            pdfDocument.writeTo(
                FileOutputStream(file)
            )

            pdfDocument.close()

            Toast.makeText(
                requireContext(),
                "PDF Saved Successfully",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                requireContext(),
                "Failed to save PDF",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}