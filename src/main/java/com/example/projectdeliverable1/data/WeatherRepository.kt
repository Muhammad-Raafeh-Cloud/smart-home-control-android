package com.example.projectdeliverable1.data

import com.example.projectdeliverable1.models.WeatherMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Assignment #03 REST API layer.
 * Fetches JSON from the public Open-Meteo API on Dispatchers.IO and converts it
 * into display rows for the SummaryFragment RecyclerView.
 */
object WeatherRepository {
    private const val WEATHER_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=33.6844&longitude=73.0479&current=temperature_2m,relative_humidity_2m,wind_speed_10m,is_day&timezone=auto"

    suspend fun getLatestWeather(): List<WeatherMetric> = withContext(Dispatchers.IO) {
        val connection = (URL(WEATHER_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP $responseCode")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(response).getJSONObject("current")
            val time = current.optString("time", currentDisplayTime())

            listOf(
                WeatherMetric(
                    title = "Outdoor Temperature",
                    value = current.getDouble("temperature_2m").toString(),
                    unit = "C",
                    description = "Open-Meteo current reading for Islamabad at $time."
                ),
                WeatherMetric(
                    title = "Humidity",
                    value = current.getInt("relative_humidity_2m").toString(),
                    unit = "%",
                    description = "Useful for AC, ventilation and smart sensor decisions."
                ),
                WeatherMetric(
                    title = "Wind Speed",
                    value = current.getDouble("wind_speed_10m").toString(),
                    unit = "km/h",
                    description = "Useful for outdoor sprinklers, cameras and safety planning."
                ),
                WeatherMetric(
                    title = "Daylight Status",
                    value = if (current.getInt("is_day") == 1) "Day" else "Night",
                    unit = "",
                    description = "Can support automatic lighting rules in a smart-home app."
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun currentDisplayTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
}
