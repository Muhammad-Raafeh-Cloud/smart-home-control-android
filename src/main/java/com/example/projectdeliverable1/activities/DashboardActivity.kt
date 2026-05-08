package com.example.projectdeliverable1.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.fragments.DetailFragment
import com.example.projectdeliverable1.fragments.HomeFragment
import com.example.projectdeliverable1.fragments.SummaryFragment
import com.example.projectdeliverable1.models.Device

class DashboardActivity : AppCompatActivity() {

    private var userName: String = "Guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "Guest"

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeTitle)
        val btnHome = findViewById<Button>(R.id.btnHomeFragment)
        val btnSummary = findViewById<Button>(R.id.btnSummaryFragment)

        tvWelcome.text = "Welcome, $userName"

        if (savedInstanceState == null) {
            openHomeFragment()
        }

        btnHome.setOnClickListener {
            openHomeFragment()
        }

        btnSummary.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SummaryFragment.newInstance(userName))
                .commit()
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

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
    }
}
