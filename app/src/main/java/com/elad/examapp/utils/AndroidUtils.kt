package com.elad.examapp.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager

object AndroidUtils {
    /**
     * This function check if the device has a GPS or not.
     * @param context - The context is required in order to execute the GPS check.
     * @return true if the device has a GPS, else false.
     */
    fun hasGPS(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }

    /**
     * This function checks if the device has Bluetooth or not.
     * @param context - The context is required in order to execute the Bluetooth check.
     * @return true if the device has Bluetooth, else false.
     */
    fun hasBluetooth(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    /**
     * This function checks if the GPS is enabled or disabled on the device.
     * @param context - The context is required in order to execute the check.
     * @return true if the GPS is enabled, else false.
     */
    fun isGPSEnabled(context: Context?): Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    /**
     * This function check if the device's Bluetooth is on or off.
     * @param context - The context is required in order to execute the check.
     * @return true if the device's Bluetooth is on, else false.
     */
    fun isBluetoothEnabled(context: Context?): Boolean {
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }
}