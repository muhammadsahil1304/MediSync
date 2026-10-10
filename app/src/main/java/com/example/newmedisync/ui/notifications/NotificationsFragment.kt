package com.example.newmedisync.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.NotificationAdapter
import com.example.newmedisync.databinding.FragmentNotificationsBinding
import com.example.newmedisync.firebase.NotificationRepository
import com.example.newmedisync.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val repository = NotificationRepository()
    private lateinit var adapter: NotificationAdapter
    private var listenerRegistration: ListenerRegistration? = null

    private var allNotifications: List<NotificationItem> = emptyList()
    private var showOnlyUnread = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        startNotificationListener()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { item ->
            onNotificationClicked(item)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnMarkAllRead.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            lifecycleScope.launch {
                repository.markAllAsRead(uid)
                Toast.makeText(requireContext(), "All marked as read", Toast.LENGTH_SHORT).show()
            }
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            showOnlyUnread = checkedIds.contains(R.id.chipUnread)
            filterAndDisplay()
        }
    }

    private fun startNotificationListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        listenerRegistration = repository.listenToNotifications(uid) { list ->
            _binding?.let {
                binding.progressBar.visibility = View.GONE
                allNotifications = list
                filterAndDisplay()
            }
        }
    }

    private fun filterAndDisplay() {
        val filtered = if (showOnlyUnread) {
            allNotifications.filter { !it.isRead }
        } else {
            allNotifications
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.rvNotifications.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvNotifications.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun onNotificationClicked(item: NotificationItem) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            if (!item.isRead) {
                repository.markAsRead(uid, item.notificationId)
            }

            if (item.relatedAppointmentId.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                    val role = userDoc.getString("role") ?: "patient"
                    val bundle = Bundle().apply {
                        putString("appointmentId", item.relatedAppointmentId)
                    }
                    try {
                        if (role == "doctor") {
                            findNavController().navigate(R.id.navigation_doctorAppointmentDetails, bundle)
                        } else {
                            findNavController().navigate(R.id.navigation_appointmentDetails, bundle)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        listenerRegistration = null
        _binding = null
    }
}
