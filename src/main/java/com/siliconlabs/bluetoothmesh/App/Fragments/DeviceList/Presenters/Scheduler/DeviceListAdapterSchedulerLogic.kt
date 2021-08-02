/*
 * Copyright © 2020 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler

import android.content.Context
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.SchedulerAction
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.request.set.SchedulerActionSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.request.set.SchedulerMessageFlags
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerActionStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic.Companion.getMeshElementControl
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.R

class DeviceListAdapterSchedulerLogic(val listener: DeviceListAdapterLogic.DeviceListAdapterLogicListener) {

    fun refreshScheduleRegister(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        refreshNodeListener.startRefresh()

        getMeshElementControl(meshNode)?.getScheduleRegister(object : MeshElementControl.SchedulerCallback<SchedulerStatus> {
            override fun success(status: SchedulerStatus) {
                refreshNodeListener.stopRefresh()
                listener.showToast(R.string.device_adapter_scheduler_success_getting_action)
                meshNode.schedules = status.schedules
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun refreshSchedulerAction(index: Int, meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        refreshNodeListener.startRefresh()

        getMeshElementControl(meshNode)?.getSchedulerAction(index, object : MeshElementControl.SchedulerCallback<SchedulerActionStatus> {
            override fun success(status: SchedulerActionStatus) {
                refreshNodeListener.stopRefresh()
                listener.showToast(R.string.device_adapter_scheduler_success_getting_action)
                meshNode.scheduleRegister[status.index] = status
                meshNode.schedules[status.index] = status.action != SchedulerAction.Action.NO_ACTION
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun onSchedulerSetButtonClick(schedulerParams: SchedulerParams, meshNode: MeshNode, context: Context) {
        val validationInfo = schedulerParams.validate(context)
        if (validationInfo.isNotEmpty()) {
            listener.showToast(validationInfo); return
        }

        val request = SchedulerActionSet().apply {
            flags = SchedulerMessageFlags().apply { isAcknowledged = true }
            index = schedulerParams.index
            action = schedulerParams.action
            scene = schedulerParams.scene
            year = schedulerParams.year
            months = schedulerParams.months
            day = schedulerParams.day
            daysOfWeek = schedulerParams.daysOfWeek
            hour = schedulerParams.hour
            minute = schedulerParams.minute
            second = schedulerParams.second
            transitionTime = schedulerParams.transitionTime
        }
        getMeshElementControl(meshNode)?.setSchedulerAction(request, object : MeshElementControl.SchedulerCallback<SchedulerActionStatus> {
            override fun success(status: SchedulerActionStatus) {
                listener.showToast(R.string.device_adapter_scheduler_success_setting_action)
                meshNode.scheduleRegister[status.index] = status
                meshNode.schedules[status.index] = status.action != SchedulerAction.Action.NO_ACTION
                listener.notifyItemChanged(meshNode)
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }
}
