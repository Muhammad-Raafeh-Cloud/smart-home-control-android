package com.example.projectdeliverable1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.models.WeatherMetric

class WeatherAdapter : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {
    private val metrics = mutableListOf<WeatherMetric>()

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvWeatherTitle)
        val tvValue: TextView = itemView.findViewById(R.id.tvWeatherValue)
        val tvDescription: TextView = itemView.findViewById(R.id.tvWeatherDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather_metric, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val metric = metrics[position]
        holder.tvTitle.text = metric.title
        holder.tvValue.text = listOf(metric.value, metric.unit).filter { it.isNotBlank() }.joinToString(" ")
        holder.tvDescription.text = metric.description
    }

    override fun getItemCount(): Int = metrics.size

    fun submitList(newMetrics: List<WeatherMetric>) {
        metrics.clear()
        metrics.addAll(newMetrics)
        notifyDataSetChanged()
    }
}
