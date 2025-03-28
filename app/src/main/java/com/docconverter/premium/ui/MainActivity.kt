package com.docconverter.premium.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.docconverter.premium.R
import com.docconverter.premium.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Premium Document Converter app
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupNavigation()

        // Handle incoming document intents
        handleIntent()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup the bottom navigation with the nav controller
        binding.bottomNavigation.setupWithNavController(navController)

        // Setup the ActionBar with the nav controller and configuration
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_convert,
                R.id.navigation_history,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Listen for navigation changes to update the UI
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show/hide bottom navigation based on destination
            when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_convert,
                R.id.navigation_history,
                R.id.navigation_settings -> {
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun handleIntent() {
        // In a real implementation, would handle incoming document intents here
        intent?.data?.let { uri ->
            // Process the incoming document URI
            // For example, navigate to conversion screen with this document
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
