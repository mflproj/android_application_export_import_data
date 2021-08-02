/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Dialogs

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.configuration_control.ConfigurationControl
import com.siliconlab.bluetoothmesh.adk.configuration_control.FactoryResetCallback
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListView
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager

class DeviceEditionDialogsPresenter(val dialog: DeviceEditionDialogs, val parentView: ParentView, val meshLogic: MeshLogic, val deviceFunctionalityDb: MeshNodeManager) : DeviceEditionDialogs.DeviceEditionDialogsListener {
    private val TAG: String = javaClass.canonicalName!!

    interface ParentView {
        fun returnToNetworkList()
        fun refreshList()
        fun showToast(message: TOAST_MESSAGE)
    }

    enum class TOAST_MESSAGE {
        ERROR_DELETE_DEVICE,
        ERROR_MISSING_GROUP
    }

    override fun dismiss() {
    }

    override fun deleteDevice(deviceInfo: MeshNode) {
        val configurationControl = ConfigurationControl(deviceInfo.node)

        dialog.showLoadingDialog()
        dialog.updateLoadingDialogMessage(DeviceListView.LOADING_DIALOG_MESSAGE.CONFIG_DEVICE_DELETING)
        factoryResetDevice(configurationControl)
    }

    private fun factoryResetDevice(configurationControl: ConfigurationControl) {
        configurationControl.factoryReset(object : FactoryResetCallback {
            override fun success(node: Node?) {
                node?.let {
                    val meshNode = deviceFunctionalityDb.getMeshNode(it)
                    deviceFunctionalityDb.removeNodeFunc(meshNode)
                }
                dialog.dismissLoadingDialog()
                parentView.refreshList()
            }

            override fun error(node: Node?, error: ErrorType?) {
                dialog.dismissLoadingDialog()
                dialog.showDeleteDeviceLocallyDialog(error!!, node!!)
                parentView.refreshList()
            }
        })
    }

    override fun deleteDeviceLocally(node: Node) {
        node.removeOnlyFromLocalStructure()
        parentView.refreshList()
    }

}