package com.example.projectdeliverable1.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.models.Device

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val device = readDeviceFromBundle()
        val userName = arguments?.getString(ARG_USER_NAME) ?: "Guest"

        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvAssigned = view.findViewById<TextView>(R.id.tvDetailAssigned)
        val tvLocation = view.findViewById<TextView>(R.id.tvDetailLocation)
        val tvType = view.findViewById<TextView>(R.id.tvDetailType)
        val tvStatus = view.findViewById<TextView>(R.id.tvDetailStatus)
        val tvUpdated = view.findViewById<TextView>(R.id.tvDetailUpdated)
        val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        if (device != null) {
            tvTitle.text = device.name
            tvAssigned.text = "Opened by: $userName"
            tvLocation.text = "Location: ${device.location}"
            tvType.text = "Type: ${device.type}"
            tvStatus.text = "Status: ${device.status}"
            tvUpdated.text = "Last Updated: ${device.lastUpdated}"
            tvDescription.text = device.description
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun readDeviceFromBundle(): Device? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_DEVICE, Device::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_DEVICE)
        }
    }

    companion object {
        private const val ARG_DEVICE = "arg_device"
        private const val ARG_USER_NAME = "arg_user_name"

        fun newInstance(device: Device, userName: String): DetailFragment {
            val fragment = DetailFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_DEVICE, device)
                putString(ARG_USER_NAME, userName)
            }
            return fragment
        }
    }
}
