package com.elad.examapp.ui.screens.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.elad.examapp.ui.adapters.BluetoothDeviceListAdapter
import com.elad.examapp.databinding.FragmentBluetoothBinding
import com.elad.examapp.ui.dialogs.BluetoothFeatureDialog
import com.elad.examapp.ui.dialogs.PermissionDialogFragment
import com.elad.examapp.model.PermissionType
import com.elad.examapp.utils.AndroidUtils
import com.elad.examapp.utils.PermissionsUtil
import java.util.*

class BluetoothFragment : Fragment() {
    private lateinit var binding: FragmentBluetoothBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDeviceListAdapter: BluetoothDeviceListAdapter
    private var deviceList: ArrayList<BluetoothDevice>? = null
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && !bluetoothDeviceListAdapter.deviceList.contains(device)) {
                        bluetoothDeviceListAdapter.addDevice(device)
                        bluetoothDeviceListAdapter.notifyItemInserted(bluetoothDeviceListAdapter.itemCount - 1)
                    }
                }
            }
        }
    }
    private val onQueryTextListener = object :
        androidx.appcompat.widget.SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = false
        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText != null) {
                filterList(newText)
            }
            return false
        }
    }
    private val onRefreshListener = OnRefreshListener {
        stopSearchBluetoothDevices()
        deviceList = null
        bluetoothDeviceListAdapter.clear()
        searchBluetoothDevices()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothDeviceListAdapter = BluetoothDeviceListAdapter()
        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        initViews()
        handlePermissions()
    }

    /**
     * This function init all views.
     */
    private fun initViews() {
        binding.bluetoothRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.bluetoothRecyclerView.adapter = bluetoothDeviceListAdapter
        binding.bluetoothEdtSearch.setOnQueryTextListener(onQueryTextListener)
        binding.swipeRefresh.setOnRefreshListener(onRefreshListener)
    }

    /**
     * This function handle the permissions flow.
     * If arrived here, usually has all permissions,
     * The just checking if the permissions is granted.
     * If yes then it starts searching for bluetooth devices
     * else it notify the users for missing permissions
     */
    private fun handlePermissions() {
        if (AndroidUtils.isBluetoothEnabled(requireContext())) {
            if (PermissionsUtil.hasBluetoothPermissions(requireContext()) &&
                PermissionsUtil.hasLocationPermissions(requireContext())) {
                searchBluetoothDevices()
            } else {
                showPermissionsRequiredDialog()
            }
        } else {
            BluetoothFeatureDialog().show(childFragmentManager, BluetoothFeatureDialog.TAG)
        }
    }

    /**
     * This function search for bluetooth devices in the device range.
     */
    private fun searchBluetoothDevices() {
        try {
            bluetoothAdapter.startDiscovery()
        } catch (e : SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * This function stop open searching for bluetooth devices in the device range.
     */
    private fun stopSearchBluetoothDevices() {
        try {
            bluetoothAdapter.cancelDiscovery()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * A function to filter the current bluetooth devices list according to a given text input.
     * If bluetooth device mac address contains the input then it will appear on the list.
     */
    private fun filterList(text: String) {
        // remember initial list
        if (deviceList == null)
            deviceList = bluetoothDeviceListAdapter.deviceList

        try {
            val filteredList = ArrayList<BluetoothDevice>()
            deviceList?.forEach { deviceItem ->
                if (deviceItem.address.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT)))
                    filteredList.add(deviceItem)
            }
            bluetoothDeviceListAdapter.filterList(filteredList)
        } catch (ex: SecurityException) {
            ex.printStackTrace()
        }
    }

    /**
     * This function shows the permissions dialog, and notify
     * the user that one or more of the permissions(bluetooth
     * or location) is missing and required in order to search
     * for bluetooth devices. Afterwards, it redirect the user
     * to the permissions settings.
     */
    private fun showPermissionsRequiredDialog() {
        PermissionDialogFragment(PermissionType.OTHER) {
            PermissionsUtil.openPermissionsSettings(requireActivity())
        }.show(childFragmentManager, PermissionDialogFragment.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(receiver)
    }
}