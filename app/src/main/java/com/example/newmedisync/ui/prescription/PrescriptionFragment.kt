package com.example.newmedisync.ui.prescription

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.newmedisync.R
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

        binding.blueColor.setOnClickListener {
            binding.drawingView.setBrushColor(Color.parseColor("#0A70A2"))
        }

        binding.redColor.setOnClickListener {
            binding.drawingView.setBrushColor(Color.RED)
        }

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
        }

        binding.btnErase.setOnClickListener {
            binding.drawingView.setBrushColor(Color.WHITE)
        }

        binding.btnFullscreen.setOnClickListener {

            binding.fullscreenContainer.visibility = View.VISIBLE

            binding.fullscreenDrawingView.setBrushColor(
                Color.parseColor("#0A70A2")
            )
        }
        binding.btnCloseFullscreen.setOnClickListener {
            binding.fullscreenContainer.visibility = View.GONE
        }
        requireActivity().findViewById<View>(R.id.nav_view)
            ?.visibility = View.GONE

        requireActivity().findViewById<View>(R.id.nav_view)
            ?.visibility = View.VISIBLE

//        binding.sav.setOnClickListener {
//            Toast.makeText(requireContext(), "Prescription Saved", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.btnGeneratePdf.setOnClickListener {
//            Toast.makeText(requireContext(), "PDF Generated", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}