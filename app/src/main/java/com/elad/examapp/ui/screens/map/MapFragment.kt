package com.elad.examapp.ui.screens.map

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.elad.examapp.R
import com.elad.examapp.ui.dialogs.PermissionDialogFragment
import com.elad.examapp.model.PermissionType
import com.elad.examapp.model.LocationObject
import com.elad.examapp.utils.PermissionsUtil
import com.elad.examapp.utils.Util
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elad.examapp.ui.dialogs.BluetoothFeatureDialog
import com.elad.examapp.utils.AndroidUtils
import com.elad.examapp.utils.Constants
import com.elad.examapp.utils.Constants.LOCATIONS_MAX_LIMIT
import com.google.gson.Gson

class MapFragment : Fragment() {
    private lateinit var viewModel: MapViewModel
    private lateinit var mMap : GoogleMap
    private var locationsList = mutableListOf<LocationObject>()
    private var markers = arrayListOf<Marker>()
    private val onMapReadyCallback: OnMapReadyCallback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.setMinZoomPreference(13f)
        // start flow when map is ready, so the ui will be available for updates
        handlePermissions()
        viewModel.locationsListLiveData.observe(viewLifecycleOwner, locationsListObserver)
    }
    private val locationPermissionsLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
        if (PermissionsUtil.checkIfAllPermissionsGranted(isGranted)) {
            // location permissions granted, load list and start location service if not running
            viewModel.loadLocationsList()
            if (!Util.isMyServiceRunning(requireContext()))
                Util.startService(requireContext())
        } else {
            requestPermissionsWithRationaleCheck(PermissionType.LOCATION)
        }
    }

    private val bluetoothPermissionsLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
        if (PermissionsUtil.checkIfAllPermissionsGranted(isGranted)) {
            // bluetooth permissions granted, now check if has location permissions( continue handlePermissions flow )
            handlePermissions()
        } else {
            requestPermissionsWithRationaleCheck(PermissionType.BLUETOOTH)
        }
    }
    private val requestBluetoothFeatureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handlePermissions()
        } else {
            BluetoothFeatureDialog().show(childFragmentManager, BluetoothFeatureDialog.TAG)
        }
    }
    private val locationBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive")
            if (intent.action.equals(Constants.BROADCAST_NEW_LOCATION)) {
                val json = intent.getStringExtra(Constants.BROADCAST_NEW_LOCATION_EXTRA_KEY)
                val locationObject = Gson().fromJson(json, LocationObject::class.java)
                handleNewLocation(locationObject)
            }
        }
    }
    private val locationsListObserver = Observer<List<LocationObject>> { result -> consumeLocationsListObserve(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMapView()
        requireActivity().registerReceiver(locationBroadcastReceiver, IntentFilter(Constants.BROADCAST_NEW_LOCATION))
    }

    /**
     * This function loads the google map view.
     */
    private fun loadMapView() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(onMapReadyCallback)
    }

    /**
     * This function handle the permissions flow.
     * It first checks if the device has bluetooth feature.
     * If true then it continues to ask bluetooth and locations
     * permissions. If false and bluetooth permissions granted
     * then it shows the bluetooth system feature dialog else
     * it displays the required message for the user.
     *
     * Eventually, if all permissions is granted, then it
     * starts the location service and load the locations
     * list from the database.
     */
    private fun handlePermissions() {
        // first check if bluetooth feature enabled
        if (AndroidUtils.isBluetoothEnabled(requireContext())) {
            // BLUETOOTH enable, check if has bluetooth permissions
            if (!PermissionsUtil.hasBluetoothPermissions(requireContext())) {
                // BLUETOOTH permissions not granted, request BLUETOOTH permissions
                PermissionsUtil.requestBluetoothPermissions(bluetoothPermissionsLauncher)
            } else {
                // BLUETOOTH permissions granted, now check if has location permissions
                if (!PermissionsUtil.hasLocationPermissions(requireContext())) {
                    // LOCATION permissions not granted, request LOCATION permissions
                    PermissionsUtil.requestLocationPermissions(locationPermissionsLauncher)
                } else {
                    // LOCATION permissions granted, enable flow
                    viewModel.loadLocationsList()
                    if (!Util.isMyServiceRunning(requireContext())) {
                        Util.startService(requireContext())
                    }
                }
            }
        } else {
            // BLUETOOTH feature not enabled, if has BLUETOOTH permissions then ask to enable
            if (PermissionsUtil.hasBluetoothPermissions(requireContext())) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetoothFeatureLauncher.launch(enableIntent)
            } else {
                // BLUETOOTH permissions not granted, user must turn on BLUETOOTH manually
                BluetoothFeatureDialog().show(childFragmentManager, BluetoothFeatureDialog.TAG)
            }
        }
    }

    /**
     * This function receives permission type and checks if it should show permissions rationale.
     * If true then it shows the required permission rationale else it opens the permissions settings.
     * The permission types: LOCATION, BLUETOOTH.
     */
    private fun requestPermissionsWithRationaleCheck(permissionType: PermissionType) {
        val shouldShowBluetoothRationale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_ADMIN)
            }
        val shouldShowLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)

        when(permissionType) {
            PermissionType.LOCATION -> {
                if (shouldShowLocationRationale)
                    showPermissionRationale(PermissionType.LOCATION)
                else
                    PermissionsUtil.openPermissionsSettings(requireActivity())
            }
            PermissionType.BLUETOOTH -> {
                if (shouldShowBluetoothRationale)
                    showPermissionRationale(PermissionType.BLUETOOTH)
                else
                    PermissionsUtil.openPermissionsSettings(requireActivity())
            }
            else -> {}
        }
    }

    /**
     * This function receives permission type and display the necessary dialog
     * for the provided permission type.
     * The permission types: LOCATION, BLUETOOTH.
     */
    private fun showPermissionRationale(permissionType: PermissionType) {
        PermissionDialogFragment(PermissionType.LOCATION) {
            when(permissionType) {
                PermissionType.LOCATION -> PermissionsUtil.requestLocationPermissions(locationPermissionsLauncher)
                PermissionType.BLUETOOTH -> PermissionsUtil.requestBluetoothPermissions(bluetoothPermissionsLauncher)
                else -> {}
            }
        }.show(childFragmentManager, PermissionDialogFragment.TAG)
    }

    /**
     * This function observes locations list live data on the map view model.
     * It receives locations list and updates the ui accordingly.
     * If the list is empty, then it means it is a case of first app usage,
     * so it requests for current user location and updates the ui accordingly.
     * The request is necessary because the first location update from the location
     * service might delay and take time so it improves the ui appearance faster.
     *
     * Eventually, it removes the observer, which is no longer needed.
     */
    private fun consumeLocationsListObserve(listOfLocations : List<LocationObject>) {
        Log.d(TAG, "consumeLocationsListObserve locationsListSize=${listOfLocations.size}")
        if (listOfLocations.isEmpty()) {
            // first time, get current location
            getCurrentLocation()
        } else {
            listOfLocations.forEach { locationObject -> setMarkerAndCamera(locationObject) }
        }
        // stop observing after loading list
        viewModel.locationsListLiveData.removeObserver(locationsListObserver)
    }

    /**
     * This function handles new location update received from receiver.
     * It receives new location, checks the current list of locations
     * and markers, and if it reached the size limit it deletes the last
     * location and marker and update the database and ui accordingly.
     */
    private fun handleNewLocation(locationObject: LocationObject) {
        Log.d(TAG, "handleNewLocation locationsListSize=${locationsList.size} markersListSize=${markers.size}")
        if (locationsList.size == LOCATIONS_MAX_LIMIT && markers.size == LOCATIONS_MAX_LIMIT) {
            // delete last location, which is the first in the list
            viewModel.deleteLocation(locationsList.first())
            locationsList.removeFirst()
            // delete last marker, which is the first in the list
            markers.first().remove()
            markers.removeFirst()
        }
        viewModel.insertNewLocation(locationObject)
        setMarkerAndCamera(locationObject)
    }

    /**
     * This function gets the current user location and insert it to the database.
     * It only used on first app use, when no locations provided yet, so it sets
     * the marker and focus the camera on the current user location for better ui,
     * because there might be a delay until first location from location service
     * is received.
     */
    private fun getCurrentLocation() {
        try {
            LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val locationObject = LocationObject(
                            Util.getFormattedDate(),
                            task.result.latitude,
                            task.result.longitude,
                            task.result.altitude
                        )
                        viewModel.insertNewLocation(locationObject)
                        setMarkerAndCamera(locationObject)
                    } else {
                        Log.e(TAG, "Cannot get current location: ", task.exception)
                    }
                }
        } catch (e : SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * This function receives location object and change the ui accordingly.
     * It adds marker in the provided location, and moves the camera to the
     * same location on the map.
     *
     * Eventually it adds the location and markers to the locations and markers lists.
     */
    private fun setMarkerAndCamera(locationObject: LocationObject) {
        if (!isAdded) return // might try setting when application not visible, prevents system errors

        try {
            // get lat and lan from location object
            val latLng = LatLng(locationObject.lat, locationObject.lon)
            // set marker on map
            val marker = mMap.addMarker(
                MarkerOptions()
                    .title(Util.formatLatLng(locationObject))
                    .position(latLng)
                    .icon(
                        Util.bitmapDescriptorFromVector(
                            requireContext(),
                            R.drawable.ic_marker_resized
                        ))
            )!!
            // move camera to added marker
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            // add marker to markers list
            markers.add(marker)
            // add location to locations list
            locationsList.add(locationObject)
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /**
         * Uncomment the code to stop the service and unregister
         * the location receiver when the app is terminated.
         *
         * Usually the service shouldn't run continuously because
         * of device performance and battery life.
         *
         * Depends on app requirements.
         *
         */
//        requireActivity().unregisterReceiver(locationBroadcastReceiver)
//        if (PermissionsUtil.hasLocationPermissions(requireContext())) {
//            if (Util.isMyServiceRunning(requireContext())) {
//                Util.stopService(requireContext())
//            }
//        }
    }

    companion object {
        const val TAG = "MapFragment"
    }
}