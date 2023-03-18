package com.elad.examapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elad.examapp.utils.Constants
import com.elad.examapp.utils.SharedPrefsUtil
import com.elad.examapp.utils.Util

class BootReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        // Making sure the service keep running even when the device reboots
        // We can check here if the service should be running and then start it
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context != null) {
                val serviceShouldRun = SharedPrefsUtil.getInstance(context)
                    ?.getBooleanFromSP(Constants.SERVICE_SHOULD_RUN, false)
                if (serviceShouldRun == true) {
                    Util.startService(context)
                }
            }
        }
    }
}