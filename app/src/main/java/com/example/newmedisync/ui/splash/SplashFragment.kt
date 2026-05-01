package com.example.newmedisync.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R


class SplashFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        val btnSignIn = view.findViewById<View>(R.id.btnSignIn)

        btnSignIn.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_splash_to_navigation_login
            )
        }

        return view
    }


}