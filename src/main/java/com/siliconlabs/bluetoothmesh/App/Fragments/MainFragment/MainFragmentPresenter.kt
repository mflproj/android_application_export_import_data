/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment

import android.content.Intent
import android.net.Uri
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.BluetoothStateReceiver
import com.siliconlabs.bluetoothmesh.App.Logic.LocationStateReceiver
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic

class MainFragmentPresenter(private val mainFragmentView: MainFragmentView, private val bluetoothStateReceiver: BluetoothStateReceiver, private val locationStateReceiver: LocationStateReceiver, private val meshLogic: MeshLogic) : BasePresenter, BluetoothStateReceiver.BluetoothStateListener, LocationStateReceiver.LocationStateListener {
    private val TAG: String = javaClass.canonicalName!!

    override fun onResume() {
        mainFragmentView.setView()
        bluetoothStateReceiver.addListener(this)
        locationStateReceiver.addListener(this)
    }

    override fun onPause() {
        bluetoothStateReceiver.removeListener(this)
    }

    // BluetoothStateListener

    override fun onBluetoothStateChanged(enabled: Boolean) {
        mainFragmentView.setEnablingButtons()
    }

    // LocationStateListener

    override fun onLocationStateChanged() {
        mainFragmentView.setEnablingButtons()
    }

    fun shareNetworkKeys() {
        val intent = meshLogic.shareNetworkKeys()
        mainFragmentView.showShareKeysIntent(intent)
    }

    fun saveNetworkKeys() {
        val intent = meshLogic.prepareSaveKeysIntent()
        mainFragmentView.showSaveKeysIntent(intent)
    }

    fun saveKeysToPhoneStorage(intent: Intent) {
        val treeUri: Uri = intent.data!!
        meshLogic.saveKeysToLocalStorage(treeUri)
    }

}