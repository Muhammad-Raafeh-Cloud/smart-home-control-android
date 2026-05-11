package com.example.projectdeliverable1.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.activities.DashboardActivity
import com.example.projectdeliverable1.adapters.DeviceAdapter
import com.example.projectdeliverable1.data.AuthRepository
import com.example.projectdeliverable1.data.DeviceRepository
import com.example.projectdeliverable1.data.FirestoreHelper
import com.example.projectdeliverable1.models.Device
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var etName: EditText
    private lateinit var etRoom: EditText
    private lateinit var etType: EditText
    private lateinit var etStatus: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvSelected: TextView
    private var selectedDevice: Device? = null
    private var sortedByName = false
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString(ARG_USER_NAME) ?: "Guest"
        val tvSubTitle = view.findViewById<TextView>(R.id.tvHomeSubtitle)
        val etSearch = view.findViewById<EditText>(R.id.etSearchDevice)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerDevices)
        val btnAdd = view.findViewById<Button>(R.id.btnAddDevice)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateDevice)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteDevice)
        val btnClear = view.findViewById<Button>(R.id.btnClearDevice)
        val btnSort = view.findViewById<Button>(R.id.btnSortDevices)
        val btnOpenDetail = view.findViewById<Button>(R.id.btnOpenDetail)

        etName = view.findViewById(R.id.etDeviceName)
        etRoom = view.findViewById(R.id.etDeviceRoom)
        etType = view.findViewById(R.id.etDeviceType)
        etStatus = view.findViewById(R.id.etDeviceStatus)
        etDescription = view.findViewById(R.id.etDeviceDescription)
        tvSelected = view.findViewById(R.id.tvSelectedDevice)

        val firebaseUser = AuthRepository.getCurrentUser()
        tvSubTitle.text = "$userName, manage persistent smart home devices below. Firestore realtime sync is active after login."

        deviceAdapter = DeviceAdapter { selected ->
            selectedDevice = selected
            fillForm(selected)
            tvSelected.text = "Selected: ${selected.name}"
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = deviceAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            DeviceRepository.initializeIfNeeded(requireContext())
            loadDevices()
        }

        firebaseUser?.let { user ->
            firestoreListener = FirestoreHelper.syncUserData(
                user.uid,
                onUpdate = { cloudDevices ->
                    if (cloudDevices.isNotEmpty() && etSearch.text.isNullOrBlank()) {
                        deviceAdapter.submitList(cloudDevices)
                        tvSubTitle.text = "Realtime Firestore sync active: ${cloudDevices.size} cloud device(s)."
                    }
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Firestore sync error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewLifecycleOwner.lifecycleScope.launch { loadDevices(s.toString()) }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnAdd.setOnClickListener {
            val device = readFormDevice(id = 0) ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                DeviceRepository.addDevice(requireContext(), device)
                AuthRepository.getCurrentUser()?.let { user -> FirestoreHelper.addDevice(user.uid, device) }
                Toast.makeText(requireContext(), "Device saved locally and synced to Firestore", Toast.LENGTH_SHORT).show()
                clearForm()
                loadDevices(etSearch.text.toString())
            }
        }

        btnUpdate.setOnClickListener {
            val current = selectedDevice
            if (current == null) {
                Toast.makeText(requireContext(), "Select a device first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = readFormDevice(id = current.id) ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                DeviceRepository.updateDevice(requireContext(), updated)
                AuthRepository.getCurrentUser()?.let { user ->
                    if (current.firestoreId.isNotBlank()) {
                        FirestoreHelper.updateDevice(user.uid, current.firestoreId, updated)
                    } else {
                        FirestoreHelper.addDevice(user.uid, updated)
                    }
                }
                Toast.makeText(requireContext(), "Device updated locally and in Firestore", Toast.LENGTH_SHORT).show()
                clearForm()
                loadDevices(etSearch.text.toString())
            }
        }

        btnDelete.setOnClickListener {
            val current = selectedDevice
            if (current == null) {
                Toast.makeText(requireContext(), "Select a device first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                DeviceRepository.deleteDevice(requireContext(), current.id)
                AuthRepository.getCurrentUser()?.let { user ->
                    if (current.firestoreId.isNotBlank()) FirestoreHelper.deleteDevice(user.uid, current.firestoreId)
                }
                Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show()
                clearForm()
                loadDevices(etSearch.text.toString())
            }
        }

        btnClear.setOnClickListener { clearForm() }
        btnSort.setOnClickListener {
            sortedByName = !sortedByName
            btnSort.text = if (sortedByName) "Sort: Newest" else "Sort: Name"
            viewLifecycleOwner.lifecycleScope.launch { loadDevices(etSearch.text.toString()) }
        }
        btnOpenDetail.setOnClickListener {
            selectedDevice?.let { (activity as? DashboardActivity)?.openDetailFragment(it) }
                ?: Toast.makeText(requireContext(), "Select a device to open detail", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadDevices(query: String = "") {
        val devices = DeviceRepository.getDevices(requireContext(), query, sortedByName)
        deviceAdapter.submitList(devices)
    }

    private fun fillForm(device: Device) {
        etName.setText(device.name)
        etRoom.setText(device.location)
        etType.setText(device.type)
        etStatus.setText(device.status)
        etDescription.setText(device.description)
    }

    private fun clearForm() {
        selectedDevice = null
        tvSelected.text = "No device selected"
        etName.text.clear()
        etRoom.text.clear()
        etType.text.clear()
        etStatus.text.clear()
        etDescription.text.clear()
    }

    private fun readFormDevice(id: Int): Device? {
        val name = etName.text.toString().trim()
        val room = etRoom.text.toString().trim()
        val type = etType.text.toString().trim()
        val status = etStatus.text.toString().trim()
        val description = etDescription.text.toString().trim()
        if (name.isBlank() || room.isBlank() || type.isBlank() || status.isBlank() || description.isBlank()) {
            Toast.makeText(requireContext(), "Fill all device fields", Toast.LENGTH_SHORT).show()
            return null
        }
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        return Device(id, name, room, status, type, time, description)
    }

    override fun onDestroyView() {
        firestoreListener?.remove()
        firestoreListener = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_USER_NAME = "arg_user_name"
        fun newInstance(userName: String): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = Bundle().apply { putString(ARG_USER_NAME, userName) }
            return fragment
        }
    }
}
