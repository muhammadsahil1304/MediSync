package com.example.newmedisync.ui.patient

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentReportScannerBinding
import com.example.newmedisync.firebase.AIRepository
import com.example.newmedisync.firebase.ReportRepository
import com.example.newmedisync.model.MedicalReport
import com.example.newmedisync.model.ReportAnalysis
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import org.json.JSONObject

class ReportScannerFragment : Fragment() {

    private var _binding: FragmentReportScannerBinding? = null
    private val binding get() = _binding!!
    private var selectedUri: Uri? = null
    private var isPdf = false

    private val aiRepository = AIRepository()
    private val reportRepository = ReportRepository()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            isPdf = false
            binding.ivPreview.setImageURI(it)
            binding.tvFileName.text = "Selected Image"
            binding.cardPreview.visibility = View.VISIBLE
            binding.cardResult.visibility = View.GONE
        }
    }

    private val pickPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            isPdf = true
            binding.ivPreview.setImageResource(R.drawable.pdf) // Assuming you have a pdf icon
            binding.tvFileName.text = "Selected PDF"
            binding.cardPreview.visibility = View.VISIBLE
            binding.cardResult.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUploadImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnUploadPdf.setOnClickListener { pickPdf.launch("application/pdf") }

        binding.btnAnalyze.setOnClickListener {
            if (isPdf) {
                Toast.makeText(requireContext(), "PDF text extraction is limited in this demo. Please use images.", Toast.LENGTH_LONG).show()
                // In a full version, we'd use PdfRenderer to convert to images or PdfBox for text
            } else {
                extractTextFromImage()
            }
        }
    }

    private fun extractTextFromImage() {
        val uri = selectedUri ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false

        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText.text.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = true
                        Toast.makeText(requireContext(), "No text found in image.", Toast.LENGTH_SHORT).show()
                    } else {
                        analyzeWithAI(visionText.text)
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnAnalyze.isEnabled = true
                    Toast.makeText(requireContext(), "OCR Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true
            Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeWithAI(extractedText: String) {
        lifecycleScope.launch {
            val aiResponse = aiRepository.analyzeMedicalReport(extractedText)
            binding.progressBar.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true

            if (aiResponse.isNotEmpty()) {
                parseAndShowResult(aiResponse, extractedText)
            } else {
                Toast.makeText(requireContext(), "AI Analysis failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseAndShowResult(jsonStr: String, rawText: String) {
        try {
            // Clean JSON string (Gemini sometimes adds ```json ... ```)
            val cleanedJson = jsonStr.substringAfter("```json").substringBeforeLast("```").trim()
            val finalJson = if (cleanedJson.isEmpty()) jsonStr.trim() else cleanedJson
            
            val json = JSONObject(finalJson)
            val analysis = ReportAnalysis(
                summary = json.optString("summary"),
                abnormalFindings = json.optJSONArray("abnormalFindings")?.let { arr ->
                    List(arr.length()) { arr.getString(it) }
                } ?: emptyList(),
                normalFindings = json.optJSONArray("normalFindings")?.let { arr ->
                    List(arr.length()) { arr.getString(it) }
                } ?: emptyList(),
                recommendations = json.optJSONArray("recommendations")?.let { arr ->
                    List(arr.length()) { arr.getString(it) }
                } ?: emptyList(),
                severity = json.optString("severity"),
                followUp = json.optString("followUp")
            )

            displayAnalysis(analysis, rawText)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Parsing Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayAnalysis(analysis: ReportAnalysis, rawText: String) {
        binding.tvSummary.text = analysis.summary
        binding.tvAbnormal.text = analysis.abnormalFindings.joinToString("\n• ", "• ")
        binding.tvNormal.text = analysis.normalFindings.joinToString("\n• ", "• ")
        binding.tvRecommendations.text = analysis.recommendations.joinToString("\n✔ ", "✔ ")
        binding.tvSeverity.text = analysis.severity
        
        // Update chip color based on severity
        when (analysis.severity.lowercase()) {
            "low" -> {
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_chip_blue)
                binding.tvSeverity.setTextColor(resources.getColor(R.color.primary))
            }
            "moderate" -> {
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_chip_yellow)
                binding.tvSeverity.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            "high" -> {
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_chip_red)
                binding.tvSeverity.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        }

        binding.cardResult.visibility = View.VISIBLE
        
        binding.btnSaveReport.setOnClickListener {
            saveToFirestore(analysis, rawText)
        }
    }

    private fun saveToFirestore(analysis: ReportAnalysis, rawText: String) {
        lifecycleScope.launch {
            val report = MedicalReport(
                reportName = binding.tvFileName.text.toString(),
                extractedText = rawText,
                analysis = analysis
            )
            reportRepository.saveReport(report)
            Toast.makeText(requireContext(), "Report saved to history!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
