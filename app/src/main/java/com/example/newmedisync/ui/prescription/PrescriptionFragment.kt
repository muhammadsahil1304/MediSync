package com.example.newmedisync.ui.prescription

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.example.newmedisync.room.PrescriptionEntity
import com.google.firebase.auth.FirebaseAuth

class PrescriptionBoardFragment : Fragment() {

    private var _binding: FragmentPrescriptionBinding? = null
    private val binding get() = _binding!!
    private var currentColor = Color.parseColor("#0A70A2")
    private var selectedPatientName = ""
    private var selectedPatientPhone = ""
    private var isEraserSelected = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(inflater, container, false)
        arguments?.let {

            selectedPatientName =
                it.getString("name", "")

            selectedPatientPhone =
                it.getString("phone", "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        AppDatabase
            .getDatabase(requireContext())
            .patientDao()
            .getAllPatients(uid)
            .observe(viewLifecycleOwner) { patients ->

                setupPatientsSpinner(patients)
            }

        binding.blueColor.setOnClickListener {

            currentColor = Color.parseColor("#0A70A2")

            isEraserSelected = false

            updateColorSelection(true)
            updateToolSelection(true)

            binding.drawingView.setBrushColor(currentColor)

            binding.fullscreenDrawingView.setBrushColor(currentColor)
        }

        binding.redColor.setOnClickListener {

            currentColor = Color.RED

            isEraserSelected = false

            updateColorSelection(false)
            updateToolSelection(true)

            binding.drawingView.setBrushColor(currentColor)

            binding.fullscreenDrawingView.setBrushColor(currentColor)
        }

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
        }
        binding.fullClear.setOnClickListener {

            binding.fullscreenDrawingView.clearCanvas()
        }

        binding.btnErase.setOnClickListener {

            isEraserSelected = true

            updateToolSelection(false)

            binding.drawingView.enableEraser()

            binding.fullscreenDrawingView.enableEraser()
        }

        binding.fullBlue.setOnClickListener {

            updateColorSelection(true)
            updateToolSelection(true)

            binding.fullscreenDrawingView.setBrushColor(
                Color.parseColor("#0A70A2")
            )

            binding.drawingView.setBrushColor(
                Color.parseColor("#0A70A2")
            )
        }
        binding.fullRed.setOnClickListener {

            updateColorSelection(false)
            updateToolSelection(true)

            binding.fullscreenDrawingView.setBrushColor(Color.RED)

            binding.drawingView.setBrushColor(Color.RED)
        }

        binding.fullErase.setOnClickListener {

            updateToolSelection(false)

            binding.fullscreenDrawingView.enableEraser()

            binding.drawingView.enableEraser()
        }
        binding.btnUndo.setOnClickListener {

            binding.drawingView.undoLastStroke()
        }

        binding.fullUndo.setOnClickListener {

            binding.fullscreenDrawingView.undoLastStroke()
        }

        binding.btnFullscreen.setOnClickListener {

            binding.fullscreenContainer.visibility = View.VISIBLE

            binding.fullscreenDrawingView.setStrokes(
                binding.drawingView.getStrokes()
            )

            if (isEraserSelected) {

                binding.fullscreenDrawingView.enableEraser()

            } else {

                binding.fullscreenDrawingView.setBrushColor(
                    currentColor
                )
            }
        }
        binding.btnFullPen.setOnClickListener {

            isEraserSelected = false

            updateToolSelection(true)

            binding.fullscreenDrawingView.setBrushColor(currentColor)

            binding.drawingView.setBrushColor(currentColor)
        }
        binding.btnPen.setOnClickListener {

            isEraserSelected = false

            updateToolSelection(true)

            binding.drawingView.setBrushColor(currentColor)

            binding.fullscreenDrawingView.setBrushColor(currentColor)
        }
        binding.btnCloseFullscreen.setOnClickListener {

            binding.drawingView.setStrokes(
                binding.fullscreenDrawingView.getStrokes()
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
            savePrescriptionAsImage()
        }

        updateToolSelection(true)
        updateColorSelection(true)
    }
//    private fun selectPen() {
//
//        binding.btnPen.setBackgroundResource(
//            R.drawable.bg_tool_selected
//        )
//
//        binding.btnErase.setBackgroundResource(
//            R.drawable.bg_tool_unselected
//        )
//
//        binding.btnPen.setColorFilter(Color.WHITE)
//
//        binding.btnErase.setColorFilter(
//            Color.parseColor("#777777")
//        )
//    }
//    private fun selectEraser() {
//
//        binding.btnErase.setBackgroundResource(
//            R.drawable.bg_tool_selected
//        )
//
//        binding.btnPen.setBackgroundResource(
//            R.drawable.bg_tool_unselected
//        )
//
//        binding.btnErase.setColorFilter(Color.WHITE)
//
//        binding.btnPen.setColorFilter(
//            Color.parseColor("#777777")
//        )
//    }
//    private fun selectRed() {
//
//        binding.redColor.setBackgroundResource(
//            R.drawable.bg_red_selected
//        )
//
//        binding.blueColor.setBackgroundResource(
//            R.drawable.bg_blue_unselected
//        )
//    }
//    private fun selectBlue() {
//
//        binding.blueColor.setBackgroundResource(
//            R.drawable.bg_blue_selected
//        )
//
//        binding.redColor.setBackgroundResource(
//            R.drawable.bg_red_unselected
//        )
//    }
private fun updateToolSelection(
    isPenSelected: Boolean
) {

    binding.btnPen.background =
        if (isPenSelected)
            resources.getDrawable(R.drawable.bg_tool_selected)
        else null

    binding.btnFullPen.background =
        if (isPenSelected)
            resources.getDrawable(R.drawable.bg_tool_selected)
        else null

    binding.btnErase.background =
        if (!isPenSelected)
            resources.getDrawable(R.drawable.bg_tool_selected)
        else null

    binding.fullErase.background =
        if (!isPenSelected)
            resources.getDrawable(R.drawable.bg_tool_selected)
        else null
}
    private fun updateColorSelection(
        isBlueSelected: Boolean
    ) {

        binding.blueColor.background =
            if (isBlueSelected)
                resources.getDrawable(R.drawable.bg_blue_selected)
            else
                resources.getDrawable(R.drawable.bg_blue_dot)

        binding.redColor.background =
            if (!isBlueSelected)
                resources.getDrawable(R.drawable.bg_red_selected)
            else
                resources.getDrawable(R.drawable.bg_red_dot)

        binding.fullBlue.background =
            if (isBlueSelected)
                resources.getDrawable(R.drawable.bg_blue_selected)
            else
                resources.getDrawable(R.drawable.bg_blue_dot)

        binding.fullRed.background =
            if (!isBlueSelected)
                resources.getDrawable(R.drawable.bg_red_selected)
            else
                resources.getDrawable(R.drawable.bg_red_dot)
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
        if (selectedPatientName.isNotEmpty()) {

            val patientIndex =
                patientNames.indexOf(selectedPatientName)

            if (patientIndex != -1) {

                binding.spPatients.setSelection(patientIndex)

                binding.etPatientPhone.setText(
                    selectedPatientPhone
                )
            }
        }

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
    private fun savePrescriptionAsImage() {

        try {

            val content = binding.drawingView

            val bitmap = Bitmap.createBitmap(
                content.width,
                content.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)

            content.draw(canvas)

            val directory = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ),
                "MediSync"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(
                directory,
                "Prescription_${System.currentTimeMillis()}.png"
            )

            val outputStream = FileOutputStream(file)

            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                outputStream
            )

            outputStream.flush()
            outputStream.close()

            savePrescriptionToRoom(file.absolutePath)

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                requireContext(),
                "Failed to save prescription",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun savePrescriptionToRoom(
        imagePath: String
    ) {

        val prescription = PrescriptionEntity(
            patientName = selectedPatientName,
            patientPhone = selectedPatientPhone,
            visitDate = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date()),
            imagePath = imagePath
        )

        CoroutineScope(Dispatchers.IO).launch {

            AppDatabase
                .getDatabase(requireContext())
                .prescriptionDao()
                .insertPrescription(prescription)

            Handler(Looper.getMainLooper()).post {

                Toast.makeText(
                    requireContext(),
                    "Prescription Saved",
                    Toast.LENGTH_LONG
                ).show()
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