package com.elad.examapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elad.examapp.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.gson.Gson
import com.elad.examapp.R
import com.elad.examapp.model.LocationObject
import com.elad.examapp.utils.Constants.BROADCAST_NEW_LOCATION
import com.elad.examapp.utils.Constants.BROADCAST_NEW_LOCATION_EXTRA_KEY
import com.elad.examapp.utils.Constants.CHANNEL_ID
import com.elad.examapp.utils.Constants.LOCATION_FASTEST_INTERVAL
import com.elad.examapp.utils.Constants.LOCATION_INTERVAL
import com.elad.examapp.utils.Constants.LOCATION_MAX_WAIT_TIME
import com.elad.examapp.utils.Constants.MAIN_ACTION
import com.elad.examapp.utils.Constants.NOTIFICATION_ID
import com.elad.examapp.utils.Constants.ACTION_START_SERVICE
import com.elad.examapp.utils.Constants.ACTION_STOP_SERVICE
import com.elad.examapp.utils.Util
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var isServiceRunningRightNow = false
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var lastShownNotificationId = -1

    override fun onCreate() {
        super.onCreate()
        initLocationComponents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return START_NOT_STICKY;
        }

        Log.d(TAG, "onStartCommand A")
        if (intent.action.equals(ACTION_START_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            Log.d(TAG, "onStartCommand B")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notifyToUserForForegroundService()
            }

            isServiceRunningRightNow = true

            startRecording()

        } else if (intent.action.equals(ACTION_STOP_SERVICE)) {
            stopRecording()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            stopSelf()

            isServiceRunningRightNow = false
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun initLocationComponents() {
        fusedLocationProviderClient = getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    Log.d(TAG, ":getLastLocation ${locationResult.lastLocation!!.latitude} ${locationResult.lastLocation!!.longitude}")
                    val intent = Intent(BROADCAST_NEW_LOCATION)
                    val locationObject = LocationObject(
                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                        locationResult.lastLocation!!.latitude,
                        locationResult.lastLocation!!.longitude,
                        locationResult.lastLocation!!.altitude
                    )
                    val json = Gson().toJson(locationObject)
                    intent.putExtra(BROADCAST_NEW_LOCATION_EXTRA_KEY, json)
                    sendBroadcast(intent)
//                    LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updateNotification(Util.formatLatLng(locationObject))
                    }
                } else {
                    Log.d(TAG, "Location information isn't available.")
                }
            }
        }
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
            .setWaitForAccurateLocation(false)
            .build()
    }

    private fun startRecording() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "stop Location Callback removed.")
                        stopSelf()
                    } else {
                        Log.d(TAG, "stop Failed to remove Location Callback.")
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notifyToUserForForegroundService() {
        // On notification click
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder = getNotificationBuilder(
            this,
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        ) //Low importance prevent visual appearance for this notification channel on top
        notificationBuilder!!
            .setContentIntent(pendingIntent) // Open activity
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
            .setContentTitle(getString(R.string.my_location))
            .setContentText(getString(R.string.app_running))
        val notification: Notification = notificationBuilder!!.build()
        startForeground(NOTIFICATION_ID, notification)
        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(lastShownNotificationId)
        }
        lastShownNotificationId = NOTIFICATION_ID
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationBuilder(
        context: Context,
        channelId: String,
        importance: Int
    ): NotificationCompat.Builder {
        prepareChannel(context, channelId, importance)
        return NotificationCompat.Builder(context, channelId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareChannel(context: Context, id: String, importance: Int) {
        val appName: String = context.getString(R.string.app_name)
        val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var nChannel = nm.getNotificationChannel(id)
        if (nChannel == null) {
            nChannel = NotificationChannel(id, appName, importance)
            nChannel.description = "Exam App Channel"

            // from another answer
            nChannel.enableLights(true)
            nChannel.lightColor = Color.BLUE
            nm.createNotificationChannel(nChannel)
        }
    }

    private fun updateNotification(content: String) {
        notificationBuilder?.setContentText(content)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder?.build())
    }

    companion object {
        private const val TAG = "LocationService"
    }
}