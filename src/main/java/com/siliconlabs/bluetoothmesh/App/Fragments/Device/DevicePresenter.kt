/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic

class DevicePresenter(private val deviceView: DeviceView, private val networkConnectionLogic: NetworkConnectionLogic, private val meshLogic: MeshLogic) : BasePresenter, NetworkConnectionListener {

    private val meshNode = meshLogic.deviceToConfigure!!

    var firstConfig: Boolean = false

    override fun onResume() {
        if (meshNode.node.name == null) {
            deviceView.setActionBarTitle("")
        } else {
            deviceView.setActionBarTitle(meshNode.node.name)
        }

        if (firstConfig) {
            networkConnectionLogic.connect(meshLogic.provisionedBluetoothConnectableDevice!!, refreshBluetoothDevice = true)
        }
        networkConnectionLogic.addListener(this)
    }

    override fun onPause() {
        networkConnectionLogic.removeListener(this)
    }

    fun onDestroy() {
        if (firstConfig) {
            networkConnectionLogic.disconnect()
        }
    }

    // NetworkConnectionListener
    override fun connecting() {
        deviceView.showLoadingDialog()
        deviceView.setLoadingDialogMessage(DeviceView.LOADING_DIALOG_MESSAGE.CONFIG_CONNECTING)
    }

    override fun connected() {
        if (firstConfig) {
            networkConnectionLogic.setupInitialNodeConfiguration(meshNode.node)
        } else {
            deviceView.dismissLoadingDialog()
        }
    }

    override fun disconnected() {
        deviceView.showLoadingDialog()
        deviceView.setLoadingDialogMessage(DeviceView.LOADING_DIALOG_MESSAGE.CONFIG_DISCONNECTED, true, true)
    }

    override fun initialConfigurationLoaded() {
        deviceView.dismissLoadingDialog()
    }

    override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
        deviceView.setLoadingDialogMessage(messageType.toString(), true)
    }

    override fun connectionErrorMessage(error: ErrorType) {
        deviceView.setLoadingDialogMessage(error, true)
    }
}