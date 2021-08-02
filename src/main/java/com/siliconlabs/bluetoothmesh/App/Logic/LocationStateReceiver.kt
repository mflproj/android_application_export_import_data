/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

class LocationStateReceiver : BroadcastReceiver() {
    private val TAG: String = javaClass.canonicalName!!

    private val listeners: ArrayList<LocationStateListener> = ArrayList()

    fun addListener(locationStateListener: LocationStateListener) {
        synchronized(listeners) {
            listeners.add(locationStateListener)
        }
    }

    fun removeListener(locationStateListener: LocationStateListener) {
        synchronized(listeners) {
            listeners.remove(locationStateListener)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            notifyListeners()
        }
    }

    private fun notifyListeners() {
        synchronized(listeners) {
            listeners.forEach { listener ->
                Log.d(TAG, "onLocationStateChanged")
                listener.onLocationStateChanged()
            }
        }
    }

    interface LocationStateListener {
        fun onLocationStateChanged()
    }
}