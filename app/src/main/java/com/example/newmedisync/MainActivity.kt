package com.example.newmedisync

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newmedisync.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }
        val navView: BottomNavigationView = binding.bottomNavInclude.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_splash, R.id.navigation_login, R.id.navigation_signup,
                R.id.navigation_doctorVerify, R.id.navigation_adminHome, R.id.navigation_completePatientProfile -> {
                    binding.bottomNavContainer.visibility = View.GONE
                }
                R.id.navigation_patientHome, R.id.navigation_visits, R.id.navigation_reports -> {
                    binding.bottomNavContainer.visibility = View.VISIBLE
                    if (navView.menu.findItem(R.id.navigation_patientHome) == null) {
                        navView.menu.clear()
                        navView.inflateMenu(R.menu.patient_bottom_nav_menu)
                        navView.setupWithNavController(navController)
                    }
                }
                R.id.navigation_home, R.id.navigation_patients, R.id.navigation_notifications -> {
                    binding.bottomNavContainer.visibility = View.VISIBLE
                    if (navView.menu.findItem(R.id.navigation_home) == null) {
                        navView.menu.clear()
                        navView.inflateMenu(R.menu.bottom_nav_menu)
                        navView.setupWithNavController(navController)
                    }
                }
                else -> {
                    binding.bottomNavContainer.visibility = View.VISIBLE
                }
            }
        }

        navView.setupWithNavController(navController)
    }
}