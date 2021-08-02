/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.ControlGroup

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListView
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode

interface ControlGroupView {

    fun refreshView()

    fun setMeshIconState(iconState: MESH_ICON_STATE)

    fun setMasterSwitch(isChecked: Boolean)

    fun setMasterLevel(progress: Int)

    fun setMasterControlEnabled(enabled: Boolean)

    fun setMasterControlVisibility(visibility: Int)

    fun setDevicesList(devicesInfo: Set<MeshNode>)

    fun notifyItemChanged(deviceInfo: MeshNode)

    fun showToast(message: String)

    fun showToast(errorType: ErrorType)

    fun showToast(message: TOAST_MESSAGE)

    fun showDeleteDeviceDialog(deviceInfo: MeshNode)

    fun showDeviceConfigDialog(deviceInfo: MeshNode)

    fun showLoadingDialog()

    fun updateLoadingDialogMessage(loadingMessage: DeviceListView.LOADING_DIALOG_MESSAGE, errorCode: String = "", showCloseButton: Boolean = false)

    enum class MESH_ICON_STATE {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    enum class TOAST_MESSAGE {
        NOT_CONNECTED_TO_MESH_NETWORK,
        TO_DO,
        SUCCESS
    }
}