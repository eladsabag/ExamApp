package com.elad.examapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionsUtil {
    /**
     * This function opens the permissions settings on user's phone.
     * @param - The activity that executes this operation.
     */
    fun openPermissionsSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    /**
     * This function checks if all permissions is granted for given key, value map.
     * @param isGranted - The map that contains the un/granted permissions, key - permission, value true if granted else false.
     * @return true if all permissions granted else false
     */
    fun checkIfAllPermissionsGranted(isGranted: Map<String, @JvmSuppressWildcards Boolean>): Boolean {
        for (value in isGranted.values) if (!value) return false
        return true
    }

    /**
     * This function check if the user has location permissions.
     * The permissions: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION.
     * @param context - The context is required in order to execute the permissions check.
     * @return true if has location permissions else false
     */
    fun hasLocationPermissions(context: Context?): Boolean {
        return ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This function request for location permissions.
     * The permissions: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION.
     * @param permissionLauncher - The launcher that suppose to execute the request.
     */
    fun requestLocationPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * This function request for Bluetooth permissions according to it's version.
     * Android 12+ request for newer permissions, less than that request for older permissions.
     * The permissions: BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN.
     * @param permissionLauncher - The launcher that suppose to execute the request.
     */
    fun requestBluetoothPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
            ))
        }
    }

    /**
     * This function check if the user has location permissions.
     * The permissions: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION.
     * @param context - The context is required in order to execute the permissions check.
     * @return true if has location permissions else false
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                }
    }
}
