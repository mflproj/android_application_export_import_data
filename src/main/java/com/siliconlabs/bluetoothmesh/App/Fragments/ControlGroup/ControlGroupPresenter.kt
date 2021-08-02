/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.ControlGroup

import android.util.Log
import android.view.View
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.model.ModelIdentifier
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshGroupControl
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager
import com.siliconlabs.bluetoothmesh.App.Models.TransactionId

class ControlGroupPresenter(private val controlGroupView: ControlGroupView,
                            val networkConnectionLogic: NetworkConnectionLogic,
                            val meshLogic: MeshLogic,
                            private val meshGroupControl: MeshGroupControl,
                            val meshNodeManager: MeshNodeManager)
    : BasePresenter, NetworkConnectionListener, DeviceListAdapter.DeviceListAdapterListener, MeshGroupControl.SetCallback {

    private val TAG: String = javaClass.canonicalName!!

    val groupInfo = meshLogic.currentGroup!!
    val networkInfo: Subnet = meshLogic.currentSubnet!!

    var nodes: Set<MeshNode> = emptySet()

    override fun onResume() {
        networkConnectionLogic.addListener(this)
        controlGroupView.setMasterControlEnabled(false)
        controlGroupView.setMasterControlVisibility(View.GONE)
        refreshList()
    }

    override fun onPause() {
        networkConnectionLogic.removeListener(this)
    }

    fun refreshList() {
        nodes = meshNodeManager.getMeshNodes(groupInfo)
        controlGroupView.setDevicesList(nodes)
        if (nodes.isEmpty()) {
            controlGroupView.setMasterControlVisibility(View.GONE)
        } else {
            controlGroupView.setMasterControlVisibility(View.VISIBLE)
        }
    }

    private fun adjustMasterControl(level: Int) {
        if (level > 0) {
            controlGroupView.setMasterSwitch(true)
        } else {
            controlGroupView.setMasterSwitch(false)
        }
        controlGroupView.setMasterLevel(level)
    }

    // NetworkConnectionListener

    override fun connecting() {
        controlGroupView.setMeshIconState(ControlGroupView.MESH_ICON_STATE.CONNECTING)
        refreshList()
    }

    override fun connected() {
        controlGroupView.setMeshIconState(ControlGroupView.MESH_ICON_STATE.CONNECTED)
        controlGroupView.setMasterControlEnabled(true)
        refreshList()
    }

    override fun disconnected() {
        controlGroupView.setMeshIconState(ControlGroupView.MESH_ICON_STATE.DISCONNECTED)
        controlGroupView.setMasterControlEnabled(false)
        refreshList()
    }

    override fun initialConfigurationLoaded() {
    }

    override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
        controlGroupView.showToast(messageType.toString())
    }

    override fun connectionErrorMessage(error: ErrorType) {
        controlGroupView.showToast(error)
    }

    // View callbacks

    fun meshIconClicked(iconState: ControlGroupView.MESH_ICON_STATE) {
        when (iconState) {
            ControlGroupView.MESH_ICON_STATE.DISCONNECTED -> {
                networkConnectionLogic.connect(networkInfo)
            }
            ControlGroupView.MESH_ICON_STATE.CONNECTING -> {
                networkConnectionLogic.disconnect()
            }
            ControlGroupView.MESH_ICON_STATE.CONNECTED -> {
                networkConnectionLogic.disconnect()
            }
        }
    }

    fun onMasterSwitchChanged(isChecked: Boolean) {
        Log.d(TAG, "MasterSwitchChanged isChecked:$isChecked")

        val level = if (isChecked) 100 else 0

        onMasterLevelChanged(level)
    }

    fun onMasterLevelChanged(percentage: Int) {
        Log.d(TAG, "MasterLeverChanged levelPercentage:$percentage")

        val onOffExist = nodes.any { it.functionality.getAllModels().contains(ModelIdentifier.GenericOnOffServer) }
        val levelExist = nodes.any { it.functionality.getAllModels().contains(ModelIdentifier.GenericLevelServer) }
        val lightnessExist = nodes.any { it.functionality.getAllModels().contains(ModelIdentifier.LightLightnessServer) }

        if (onOffExist) {
            meshGroupControl.setOnOff(percentage > 0, transactionId.next(), this)
        }
        if (levelExist) {
            meshGroupControl.setLevel(percentage, transactionId.next(), this)
        }
        if (lightnessExist) {
            meshGroupControl.setLightness(percentage, transactionId.next(), this)
        }

        nodes.forEach {
            it.onOffState = percentage > 0
            it.levelPercentage = percentage
            it.lightnessPercentage = percentage
        }

        adjustMasterControl(percentage)
        controlGroupView.refreshView()
    }

    // Device list adapter listener

    override fun onDeleteClickListener(deviceInfo: MeshNode) {
        controlGroupView.showDeleteDeviceDialog(deviceInfo)
    }

    override fun onConfigClickListener(deviceInfo: MeshNode) {
        controlGroupView.showDeviceConfigDialog(deviceInfo)
    }

    //group control callback

    override fun error(error: ErrorType) {
        controlGroupView.showToast(error)
    }

    companion object {
        private var transactionId = TransactionId()
    }
}