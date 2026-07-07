package com.example.newmedisync.ui.verifyEmail

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.utils.SnackbarUtils
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var tvEmail: TextView
    private lateinit var btnVerified: Button
    private lateinit var tvResend: TextView
    private lateinit var tvBack: TextView
    private var resendTimer: CountDownTimer? = null
    private var canResend = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_verify_email,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        tvEmail = view.findViewById(R.id.tvEmail)
        btnVerified = view.findViewById(R.id.btnVerified)
        tvResend = view.findViewById(R.id.tvResend)
        tvBack = view.findViewById(R.id.tvBack)

        val email = arguments?.getString("email") ?: ""

        tvEmail.text = email
        startResendTimer()

        btnVerified.setOnClickListener {

            auth.currentUser?.reload()?.addOnCompleteListener {

                val user = auth.currentUser

                if (user != null && user.isEmailVerified) {

                    SnackbarUtils.showTopSnackbar(
                        requireView(),
                        "Email Verified Successfully",
                        true
                    )

                    auth.signOut()

                    findNavController().navigate(
                        R.id.navigation_login
                    )

                } else {

                    SnackbarUtils.showTopSnackbar(
                        requireView(),
                        "Email not verified yet.",
                        false
                    )

                }

            }

        }

        tvResend.setOnClickListener {

            if (!canResend) return@setOnClickListener

            auth.currentUser
                ?.sendEmailVerification()
                ?.addOnSuccessListener {

                    Toast.makeText(
                        requireContext(),
                        "Verification email sent.",
                        Toast.LENGTH_SHORT
                    ).show()

                    startResendTimer()

                }
                ?.addOnFailureListener {

                    Toast.makeText(
                        requireContext(),
                        it.localizedMessage ?: "Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        tvBack.setOnClickListener {

            auth.signOut()

            findNavController().navigate(
                R.id.navigation_login
            )

        }

        return view
    }
    private fun startResendTimer() {

        canResend = false
        tvResend.isEnabled = false

        resendTimer?.cancel()

        resendTimer = object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val seconds = millisUntilFinished / 1000

                tvResend.text = "Resend Email (${seconds}s)"
            }

            override fun onFinish() {

                canResend = true
                tvResend.isEnabled = true
                tvResend.text = "Resend Email"
            }

        }.start()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        resendTimer?.cancel()
    }
}