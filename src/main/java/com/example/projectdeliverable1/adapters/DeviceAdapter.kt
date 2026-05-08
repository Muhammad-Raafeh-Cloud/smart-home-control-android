package com.example.projectdeliverable1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.models.Device

class DeviceAdapter(
    private val onItemClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<Device>()

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvDeviceLocation)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
        val tvType: TextView = itemView.findViewById(R.id.tvDeviceType)
        val tvUpdated: TextView = itemView.findViewById(R.id.tvDeviceUpdated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.tvName.text = device.name
        holder.tvLocation.text = "Location: ${device.location}"
        holder.tvStatus.text = device.status
        holder.tvType.text = device.type
        holder.tvUpdated.text = "Updated: ${device.lastUpdated}"
        holder.itemView.setOnClickListener { onItemClick(device) }
    }

    override fun getItemCount(): Int = devices.size

    fun submitList(newDevices: List<Device>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }
}
