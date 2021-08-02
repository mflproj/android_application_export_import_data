/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.LC

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.LightControlLightOnOff.NOT_OFF_AND_NOT_STANDBY
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.LightControlLightOnOff.OFF_OR_STANDBY
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.LightControlMode
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.LightControlOccupancyMode.STANDBY_TRANSITION_DISABLED
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.LightControlOccupancyMode.STANDBY_TRANSITION_ENABLED
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlLightOnOffStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlModeStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlOccupancyModeStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlPropertyStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic.Companion.getMeshElementControl
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.App.Models.TransactionId
import com.siliconlabs.bluetoothmesh.R

class DeviceListAdapterLCLogic(val listener: DeviceListAdapterLogic.DeviceListAdapterLogicListener) {
    private val acknowledged = true

    // mode

    fun refreshLCMode(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)
        refreshNodeListener.startRefresh()
        meshElementControl?.getLCMode(object : MeshElementControl.LightControlModeStatusCallback {
            override fun success(status: LightControlModeStatus?) {
                refreshNodeListener.stopRefresh()
                meshNode.lcMode = status!!.mode == LightControlMode.ON
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun setLCMode(meshNode: MeshNode, enable: Boolean) {
        val meshElementControl = getMeshElementControl(meshNode)

        val mode = if (enable) {
            LightControlMode.ON
        } else {
            LightControlMode.OFF
        }
        meshElementControl?.setLCMode(mode, acknowledged, object : MeshElementControl.LightControlModeStatusCallback {
            override fun success(status: LightControlModeStatus?) {
                listener.showToast(R.string.device_adapter_lc_new_lc_mode, mode.value.toString())
                meshNode.lcMode = status!!.mode == LightControlMode.ON
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    // occupancy mode

    fun refreshLCOccupancyMode(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)
        refreshNodeListener.startRefresh()
        meshElementControl?.getLCOccupancyMode(object : MeshElementControl.LightControlOccupancyModeStatusCallback {
            override fun success(status: LightControlOccupancyModeStatus?) {
                refreshNodeListener.stopRefresh()
                meshNode.lcOccupancyMode = status!!.mode == STANDBY_TRANSITION_ENABLED
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun setLCOccupancyMode(meshNode: MeshNode, enable: Boolean) {
        val meshElementControl = getMeshElementControl(meshNode)

        val mode = if (enable) STANDBY_TRANSITION_ENABLED else STANDBY_TRANSITION_DISABLED
        meshElementControl?.setLCOccupancyMode(mode, acknowledged, object : MeshElementControl.LightControlOccupancyModeStatusCallback {
            override fun success(status: LightControlOccupancyModeStatus?) {
                listener.showToast(R.string.device_adapter_lc_new_lc_occupancy_mode, mode.value.toString())
                meshNode.lcOccupancyMode = status!!.mode == STANDBY_TRANSITION_ENABLED
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    // light onOff

    fun refreshLCLightOnOff(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)
        refreshNodeListener.startRefresh()
        meshElementControl?.getLCOnOff(object : MeshElementControl.LightControlLightOnOffStatusCallback {
            override fun success(status: LightControlLightOnOffStatus?) {
                refreshNodeListener.stopRefresh()
                meshNode.lcOnOff = status!!.presentLightOnOff == NOT_OFF_AND_NOT_STANDBY
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun setLCOnOff(meshNode: MeshNode, enable: Boolean) {
        val meshElementControl = getMeshElementControl(meshNode)

        val mode = if (enable) NOT_OFF_AND_NOT_STANDBY else OFF_OR_STANDBY

        meshElementControl?.setLCOnOff(mode, transactionId.next(), null, null, acknowledged, object : MeshElementControl.LightControlLightOnOffStatusCallback {
            override fun success(status: LightControlLightOnOffStatus?) {
                listener.showToast(R.string.device_adapter_lc_new_lc_on_off_mode, mode.value.toString())
                meshNode.lcOnOff = status!!.presentLightOnOff == NOT_OFF_AND_NOT_STANDBY
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    // property

    fun refreshLCProperty(meshNode: MeshNode, selectedItemPosition: Int, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)
        refreshNodeListener.startRefresh()

        val property = LightLCProperty.values()[selectedItemPosition]
        val propertyId = property.id
        meshElementControl?.getLCProperty(propertyId, object : MeshElementControl.LightControlPropertyStatusCallback {
            override fun success(status: LightControlPropertyStatus?) {
                refreshNodeListener.stopRefresh()
                meshNode.lcPropertyValue = property.convertToValue(status!!.propertyValue)
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun setLCProperty(meshNode: MeshNode, selectedItemPosition: Int, data: String) {
        if (data.isBlank()) {
            listener.showToast(R.string.device_adapter_lc_input_blank)
            return
        }

        val property = LightLCProperty.values()[selectedItemPosition]
        val propertyId = property.id
        val propertyData: ByteArray
        try {
            propertyData = property.convertToByteArray(data)
        } catch (e: LightLCProperty.LightLCPropertyValueRangeException) {
            val maxValue = property.characteristic.max.toString()
            val maxValueDecimalExponent = property.characteristic.decimalExponent
            val maxValueLen = maxValue.length
            val max = maxValue.substring(0, maxValueLen + maxValueDecimalExponent!!) + "." + maxValue.substring(maxValueLen + maxValueDecimalExponent, maxValueLen);
            listener.showToast(R.string.device_adapter_lc_input_out_of_range, property.characteristic.min.toString(), max)
            return
        }

        val meshElementControl = getMeshElementControl(meshNode)
        meshElementControl?.setLCProperty(propertyId, propertyData, acknowledged, object : MeshElementControl.LightControlPropertyStatusCallback {
            override fun success(status: LightControlPropertyStatus?) {
                meshNode.lcPropertyValue = property.convertToValue(status!!.propertyValue)
                listener.notifyItemChanged(meshNode)
                listener.showToast(R.string.device_adapter_lc_new_lc_property_value, meshNode.lcPropertyValue)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    companion object {
        private var transactionId = TransactionId()
    }
}