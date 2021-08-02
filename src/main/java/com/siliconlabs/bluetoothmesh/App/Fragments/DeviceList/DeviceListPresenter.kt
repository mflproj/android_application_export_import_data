/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList

import android.util.Log
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager

class DeviceListPresenter(private val deviceListView: DeviceListView, val meshLogic: MeshLogic, val networkConnectionLogic: NetworkConnectionLogic, val meshNodeManager: MeshNodeManager) : BasePresenter, DeviceListAdapter.DeviceListAdapterListener, MeshElementControl.SetCallback, NetworkConnectionListener {
    private val TAG: String = javaClass.canonicalName!!

    private val networkInfo = meshLogic.currentSubnet!!

    private var deviceList: Set<MeshNode> = emptySet()

    override fun onResume() {
        Log.d(TAG, "onResume")
        refreshList()
        networkConnectionLogic.addListener(this)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
    }

    fun refreshList() {
        deviceList = meshNodeManager.getMeshNodes(networkInfo)
        deviceListView.setDevicesList(deviceList)
    }

    // device list adapter listener

    override fun onDeleteClickListener(deviceInfo: MeshNode) {
        deviceListView.showDeleteDeviceDialog(deviceInfo)
    }

    override fun onConfigClickListener(deviceInfo: MeshNode) {
        deviceListView.showDeviceConfigDialog(deviceInfo)
    }

    // network connection callback

    override fun connecting() {
        deviceListView.notifyDataSetChanged()
    }

    override fun connected() {
        deviceListView.notifyDataSetChanged()
    }

    override fun disconnected() {
        deviceListView.notifyDataSetChanged()
    }

    override fun initialConfigurationLoaded() {
    }

    override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
        // nothing to do
    }

    override fun connectionErrorMessage(error: ErrorType) {
        // nothing to do
    }

    override fun error(error: ErrorType) {
        deviceListView.showErrorToast(error)
    }

}
