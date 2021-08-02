/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Network

import android.util.Log
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic

class NetworkPresenter(private val networkView: NetworkView, private val networkConnectionLogic: NetworkConnectionLogic, private val meshLogic: MeshLogic) : BasePresenter, NetworkConnectionListener {
    private val TAG: String = javaClass.canonicalName!!

    private val networkInfo: Subnet = meshLogic.currentSubnet!!

    private var connectToSubnet = true

    override fun onResume() {
        Log.d(TAG, "onResume")
        networkView.setActionBarTitle(networkInfo.name)
        networkConnectionLogic.addListener(this)

        if (connectToSubnet) {
            networkConnectionLogic.connect(networkInfo)
            connectToSubnet = false
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        networkConnectionLogic.removeListener(this)
    }

    fun onDestroy() {
        networkConnectionLogic.disconnect()
    }

    // View

    fun meshIconClicked(iconState: NetworkView.MeshIconState) {
        when (iconState) {
            NetworkView.MeshIconState.DISCONNECTED -> {
                networkConnectionLogic.connect(networkInfo)
            }
            NetworkView.MeshIconState.CONNECTING -> {
                networkConnectionLogic.disconnect()
            }
            NetworkView.MeshIconState.CONNECTED -> {
                networkConnectionLogic.disconnect()
            }
        }
    }

    // NetworkConnectionListener

    override fun connecting() {
        networkView.setMeshIconState(NetworkView.MeshIconState.CONNECTING)
    }

    override fun connected() {
        networkView.setMeshIconState(NetworkView.MeshIconState.CONNECTED)
    }

    override fun disconnected() {
        networkView.setMeshIconState(NetworkView.MeshIconState.DISCONNECTED)
    }

    override fun initialConfigurationLoaded() {
    }

    override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
        val message: NetworkView.ToastMessage? = when (messageType) {
            NetworkConnectionListener.MessageType.NO_NODE_IN_NETWORK -> NetworkView.ToastMessage.NO_NODE_IN_NETWORK
            NetworkConnectionListener.MessageType.GATT_NOT_CONNECTED -> NetworkView.ToastMessage.GATT_NOT_CONNECTED
            NetworkConnectionListener.MessageType.GATT_PROXY_DISCONNECTED -> NetworkView.ToastMessage.GATT_PROXY_DISCONNECTED
            NetworkConnectionListener.MessageType.GATT_ERROR_DISCOVERING_SERVICES -> NetworkView.ToastMessage.GATT_ERROR_DISCOVERING_SERVICES
            NetworkConnectionListener.MessageType.PROXY_SERVICE_NOT_FOUND -> NetworkView.ToastMessage.PROXY_SERVICE_NOT_FOUND
            NetworkConnectionListener.MessageType.PROXY_CHARACTERISTIC_NOT_FOUND -> NetworkView.ToastMessage.PROXY_CHARACTERISTIC_NOT_FOUND
            NetworkConnectionListener.MessageType.PROXY_DESCRIPTOR_NOT_FOUND -> NetworkView.ToastMessage.PROXY_DESCRIPTOR_NOT_FOUND
        }

        message?.let {
            networkView.showToast(message)
        }
    }

    override fun connectionErrorMessage(error: ErrorType) {
        networkView.showErrorToast(error)
    }
}