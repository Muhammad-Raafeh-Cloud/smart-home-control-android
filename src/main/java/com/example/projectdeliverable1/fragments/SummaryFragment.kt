package com.example.projectdeliverable1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.adapters.WeatherAdapter
import com.example.projectdeliverable1.data.DeviceRepository
import com.example.projectdeliverable1.data.WeatherRepository
import kotlinx.coroutines.launch

class SummaryFragment : Fragment() {

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var tvApiStatus: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString(ARG_USER_NAME) ?: "Guest"
        val recyclerWeather = view.findViewById<RecyclerView>(R.id.recyclerWeather)
        val btnRefreshWeather = view.findViewById<Button>(R.id.btnRefreshWeather)
        tvApiStatus = view.findViewById(R.id.tvApiStatus)

        weatherAdapter = WeatherAdapter()
        recyclerWeather.layoutManager = LinearLayoutManager(requireContext())
        recyclerWeather.adapter = weatherAdapter

        view.findViewById<TextView>(R.id.tvSummaryUser).text = "Dashboard overview for $userName"
        view.findViewById<TextView>(R.id.tvSummaryMessage).text =
            "REST API data is loaded below in a RecyclerView. SQLite counts are loaded from the local database."

        loadDatabaseSummary(view)
        loadWeatherFromApi()

        btnRefreshWeather.setOnClickListener { loadWeatherFromApi() }
    }

    private fun loadDatabaseSummary(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            DeviceRepository.initializeIfNeeded(requireContext())
            val (total, active) = DeviceRepository.getCounts(requireContext())
            view.findViewById<TextView>(R.id.tvSummaryTotal).text = total.toString()
            view.findViewById<TextView>(R.id.tvSummaryActive).text = active.toString()
        }
    }

    private fun loadWeatherFromApi() {
        tvApiStatus.text = "Loading Open-Meteo REST API data..."
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val metrics = WeatherRepository.getLatestWeather()
                weatherAdapter.submitList(metrics)
                tvApiStatus.text = "Open-Meteo REST API loaded successfully"
            } catch (exception: Exception) {
                weatherAdapter.submitList(emptyList())
                tvApiStatus.text = "API unavailable: ${exception.message ?: "Check internet connection"}"
            }
        }
    }

    companion object {
        private const val ARG_USER_NAME = "arg_user_name"

        fun newInstance(userName: String): SummaryFragment {
            return SummaryFragment().apply {
                arguments = Bundle().apply { putString(ARG_USER_NAME, userName) }
            }
        }
    }
}
