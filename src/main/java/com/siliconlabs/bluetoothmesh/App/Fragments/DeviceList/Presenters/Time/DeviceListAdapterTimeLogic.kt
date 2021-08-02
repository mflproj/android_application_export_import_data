package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Time

import android.content.Context
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.TimeRole
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.status.TimeRoleStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.status.TimeStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic.Companion.getMeshElementControl
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.R

class DeviceListAdapterTimeLogic(val listener: DeviceListAdapterLogic.DeviceListAdapterLogicListener) {

    fun getTimeRole(meshNode: MeshNode) {
        val meshElementControl = getMeshElementControl(meshNode)

        meshElementControl?.getTimeRole(object : MeshElementControl.TimeRoleCallback {
            override fun success(status: TimeRoleStatus?) {
                meshNode.timeRole = status!!.timeRole.name
                listener.notifyItemChanged(meshNode)
                listener.showToast(R.string.device_adapter_time_success_getting_time_role)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error.toString())
            }

        })
    }

    fun setTimeRole(meshNode: MeshNode, timeRole: TimeRole) {
        val meshElementControl = getMeshElementControl(meshNode)

        meshElementControl?.setTimeRole(timeRole, object : MeshElementControl.TimeRoleCallback {
            override fun success(status: TimeRoleStatus?) {
                listener.showToast(R.string.device_adapter_time_success_setting_time_role)
                meshNode.timeRole = status!!.timeRole.name
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error.toString())
            }
        })
    }

    fun getTime(meshNode: MeshNode) {
        val meshElementControl = getMeshElementControl(meshNode)

        meshElementControl?.getTime(object : MeshElementControl.TimeCallback {
            override fun success(status: TimeStatus?) {
                listener.showToast(R.string.device_adapter_time_success_getting_time)
                meshNode.taiSeconds = status!!.taiSeconds
                meshNode.subsecond = status.subseconds
                meshNode.uncertainty = status.uncertainty
                meshNode.timeAuthority = status.timeAuthority
                meshNode.taiUtcDelta = status.taiUtcDelta
                meshNode.timeZoneOffset = status.timeZoneOffset
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error.toString())
            }

        })
    }

    fun setTime(timeValuesMap: Map<TimeParams.ParameterType, String>, meshNode: MeshNode, context: Context) {
        val meshElementControl = getMeshElementControl(meshNode)

        try {
            val taiSeconds = timeValuesMap[TimeParams.ParameterType.TAI_SECONDS]?.toLong()
            val subsecond = timeValuesMap[TimeParams.ParameterType.SUBSECOND]?.toInt()
            val uncertainty = timeValuesMap[TimeParams.ParameterType.UNCERTAINTY]?.toInt()
            val timeAuthority = timeValuesMap[TimeParams.ParameterType.TIME_AUTHORITY]?.toInt()
            val taiUtcDelta = timeValuesMap[TimeParams.ParameterType.TAI_UTC_DELTA]?.toInt()
            val timeZoneOffset = timeValuesMap[TimeParams.ParameterType.TIME_ZONE_OFFSET]?.toInt()

            if (taiSeconds != null && subsecond != null && uncertainty != null && timeAuthority != null && taiUtcDelta != null && timeZoneOffset != null) {
                val timeParams = TimeParams(taiSeconds, subsecond, uncertainty, timeAuthority, taiUtcDelta, timeZoneOffset)
                val validationInfo = timeParams.validate(context)
                if (validationInfo.isNotEmpty()) {
                    listener.showToast(validationInfo); return
                }

                meshElementControl?.setTime(timeParams, object : MeshElementControl.TimeCallback {
                    override fun success(status: TimeStatus?) {
                        listener.showToast(R.string.device_adapter_time_success_setting_time)
                        meshNode.taiSeconds = status!!.taiSeconds
                        meshNode.subsecond = status.subseconds
                        meshNode.uncertainty = status.uncertainty
                        meshNode.timeAuthority = status.timeAuthority
                        meshNode.taiUtcDelta = status.taiUtcDelta
                        meshNode.timeZoneOffset = status.timeZoneOffset
                        listener.notifyItemChanged(meshNode)
                    }

                    override fun error(error: ErrorType) {
                        listener.showToast(error.toString())
                    }

                })

            }
        } catch (e: Exception) {
            listener.showToast(R.string.device_adapter_time_invalid_input)
        }
    }
}