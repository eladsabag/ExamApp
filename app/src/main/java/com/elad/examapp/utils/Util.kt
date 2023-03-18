package com.elad.examapp.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import com.elad.examapp.model.LocationObject
import com.elad.examapp.service.LocationService
import com.elad.examapp.utils.Constants.ACTION_START_SERVICE
import com.elad.examapp.utils.Constants.ACTION_STOP_SERVICE
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.text.SimpleDateFormat
import java.util.*

object Util {
    /**
     * Converts a vector drawable to a bitmap descriptor.
     *
     * @param context The context of the app.
     * @param vectorResId The resource ID of the vector drawable to be converted.
     * @return A bitmap descriptor representing the vector drawable.
     */
    fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * This function starts the service named "LocationService"
     * @param context {Context} - the context of the current activity
     */
    fun startService(context: Context) {
        SharedPrefsUtil.getInstance(context)?.putBooleanToSP(Constants.SERVICE_SHOULD_RUN, true)

        val intent = Intent(context, LocationService::class.java)
        intent.action = ACTION_START_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            context.startService(intent)
        }
    }

    /**
     * This function stops the service named "LocationService"
     * @param context {Context} - the context of the current activity
     */
    fun stopService(context: Context) {
        SharedPrefsUtil.getInstance(context)?.putBooleanToSP(Constants.SERVICE_SHOULD_RUN, false)

        val intent = Intent(context, LocationService::class.java)
        intent.action = ACTION_STOP_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            context.startService(intent)
        }
    }

    /**
     * This function checks if a service named "LocationService" is currently running
     * @param context {Context} - the context of the current activity
     * @return {Boolean} -  indicating whether the service is running (true) or not (false)
     */
    fun isMyServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Returns a formatted string containing the latitude and longitude values of a location.
     *
     * @param locationObject The location object containing the latitude and longitude values.
     * @return A string in the format "latitude longitude", with latitude and longitude rounded to 5 decimal places.
     */
    fun formatLatLng(locationObject: LocationObject): String = String.format("%.5f %.5f", locationObject.lat, locationObject.lon)

    /**
     * Returns the current date and time in a formatted string.
     *
     * @return A string representing the current date and time in the format "dd/MM/yyyy HH:mm:ss".
     */
    fun getFormattedDate(): String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
}