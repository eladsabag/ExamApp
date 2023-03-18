package com.elad.examapp.utils

object Constants {
    // Splash Fragment
    const val NAVIGATION_DELAY = 2000L
    // Map Fragment
    const val LOCATIONS_MAX_LIMIT = 20
    // Location Service
    const val LOCATION_INTERVAL = 1 * 1000L // desired location interval 1 min (expected location updates interval)
    const val LOCATION_FASTEST_INTERVAL = LOCATION_INTERVAL / 2 // fastest interval allowed 30 sec (location updates may arrive faster than desired location interval)
    const val LOCATION_MAX_WAIT_TIME = LOCATION_INTERVAL * 2 // maximum interval delay 2 min (location updates may arrive longer than desired location interval)
    const val BROADCAST_NEW_LOCATION = "BROADCAST_NEW_LOCATION"
    const val BROADCAST_NEW_LOCATION_EXTRA_KEY = "BROADCAST_NEW_LOCATION_EXTRA_KEY"
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val NOTIFICATION_ID = 154
    const val CHANNEL_ID = "com.elad.examapp.CHANNEL_ID_FOREGROUND"
    const val MAIN_ACTION = "com.elad.examapp.locationservice.action.main"
    // Shared Preferences
    const val SERVICE_SHOULD_RUN = "SERVICE_SHOULD_RUN"
}