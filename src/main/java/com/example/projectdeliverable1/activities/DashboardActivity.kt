package com.example.projectdeliverable1.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.data.AuthRepository
import com.example.projectdeliverable1.data.UserPreferenceRepository
import com.example.projectdeliverable1.fragments.DetailFragment
import com.example.projectdeliverable1.fragments.HomeFragment
import com.example.projectdeliverable1.fragments.SummaryFragment
import com.example.projectdeliverable1.models.Device
import com.example.projectdeliverable1.notifications.NotificationHelper
import com.example.projectdeliverable1.workers.DeviceHealthWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private var userName: String = "Guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        NotificationHelper.createChannels(this)
        requestNotificationPermissionIfNeeded()
        scheduleDeviceHealthWorker()

        userName = intent.getStringExtra(EXTRA_USER_NAME) ?: AuthRepository.getCurrentUser()?.email ?: "Guest"

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeTitle)
        val btnHome = findViewById<Button>(R.id.btnHomeFragment)
        val btnSummary = findViewById<Button>(R.id.btnSummaryFragment)
        val btnCompose = findViewById<Button>(R.id.btnComposeInsights)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnTheme = findViewById<Button>(R.id.btnToggleTheme)

        tvWelcome.text = "Welcome, $userName"

        lifecycleScope.launch {
            val dark = UserPreferenceRepository.isDarkMode(this@DashboardActivity)
            btnTheme.text = if (dark) "Theme: Dark" else "Theme: Light"
        }

        if (savedInstanceState == null) openHomeFragment()

        btnHome.setOnClickListener { openHomeFragment() }
        btnSummary.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SummaryFragment.newInstance(userName))
                .commit()
        }
        btnCompose.setOnClickListener {
            startActivity(Intent(this, ComposeInsightsActivity::class.java))
        }
        btnTheme.setOnClickListener {
            lifecycleScope.launch {
                val newValue = !UserPreferenceRepository.isDarkMode(this@DashboardActivity)
                UserPreferenceRepository.setDarkMode(this@DashboardActivity, newValue)
                btnTheme.text = if (newValue) "Theme: Dark" else "Theme: Light"
            }
        }
        btnLogout.setOnClickListener {
            AuthRepository.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun openHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment.newInstance(userName))
            .commit()
    }

    fun openDetailFragment(device: Device) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DetailFragment.newInstance(device, userName))
            .addToBackStack(null)
            .commit()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    private fun scheduleDeviceHealthWorker() {
        val request = PeriodicWorkRequestBuilder<DeviceHealthWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "device_health_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
    }
}
