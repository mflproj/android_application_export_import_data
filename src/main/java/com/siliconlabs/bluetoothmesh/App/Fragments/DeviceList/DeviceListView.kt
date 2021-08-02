/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode

interface DeviceListView {

    fun setDevicesList(newDevicesList: Set<MeshNode>)

    fun notifyDataSetChanged()

    fun showLoadingDialog()

    fun dismissLoadingDialog()

    fun updateLoadingDialogMessage(loadingMessage: LOADING_DIALOG_MESSAGE, errorCode: String = "", showCloseButton: Boolean = false)

    fun updateLoadingDialogMessage(loadingMessage: LOADING_DIALOG_MESSAGE, errorType: ErrorType, showCloseButton: Boolean = false)

    fun showDeleteDeviceDialog(meshNode: MeshNode)

    fun showDeviceConfigDialog(meshNode: MeshNode)

    fun dismissDeviceConfigDialog()

    fun showToast(message: String)

    fun showErrorToast(errorType: ErrorType)

    fun showToast(message: TOAST_MESSAGE)

    enum class LOADING_DIALOG_MESSAGE {
        CONFIG_DEVICE_DELETING,
    }

    enum class TOAST_MESSAGE {
        ERROR_DELETE_DEVICE,
        SUCCESS
    }

}