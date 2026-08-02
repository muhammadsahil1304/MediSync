package com.example.newmedisync.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.ui.admin.adapter.PendingDoctorsAdapter
import com.google.firebase.auth.FirebaseAuth

class AdminHomeFragment : Fragment() {

    private lateinit var rvPendingDoctors: RecyclerView
    private lateinit var adapter: PendingDoctorsAdapter
    private lateinit var viewModel: AdminViewModel
    private lateinit var imgLogout: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_admin_home,
            container,
            false
        )

        rvPendingDoctors = view.findViewById(R.id.rvPendingDoctors)
        imgLogout = view.findViewById(R.id.imgLogout)

        imgLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            findNavController().navigate(
                R.id.action_global_navigation_welcome
            )
        }

        adapter = PendingDoctorsAdapter(emptyList()) { doctor ->

            val bundle = Bundle().apply {
                putString("doctorUid", doctor.uid)
            }

            findNavController().navigate(
                R.id.action_adminHomeFragment_to_doctorVerificationDetailsFragment,
                bundle
            )
        }

        rvPendingDoctors.layoutManager = LinearLayoutManager(requireContext())
        rvPendingDoctors.adapter = adapter

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        viewModel.pendingDoctors.observe(viewLifecycleOwner) { doctors ->
            adapter.submitList(doctors)
        }

        viewModel.loadPendingDoctors()

        return view
    }
}