package com.example.newmedisync.ui.verifyEmail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

            auth.currentUser
                ?.sendEmailVerification()
                ?.addOnSuccessListener {

                    SnackbarUtils.showTopSnackbar(
                        requireView(),
                        "Verification email sent again.",
                        true
                    )

                }
                ?.addOnFailureListener {

                    SnackbarUtils.showTopSnackbar(
                        requireView(),
                        it.localizedMessage ?: "Failed",
                        false
                    )

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
}