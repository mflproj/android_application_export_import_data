package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.content.Context
import android.view.View
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.TimeRole
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Time.TimeParams
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.R

import kotlinx.android.synthetic.main.devices_adapter_time_detail.view.*

class TimeView(context: Context) : View(context) {

    fun initView(view: View, meshNode: MeshNode, deviceListLogic: DeviceListAdapterLogic) {
        view.tv_time_role_get.text = meshNode.timeRole

        setTimeParamsTexts(view, meshNode, view.sw_human_readable.isChecked)

        view.sw_human_readable.setOnCheckedChangeListener(null)

        view.btn_time_role_get.setOnClickListener { deviceListLogic.timeLogic.getTimeRole(meshNode) }
        view.btn_send.setOnClickListener {
            val position = view.sp_choose_time_role.selectedItemPosition
            deviceListLogic.timeLogic.setTimeRole(meshNode, TimeRole.fromValue(position))
        }

        view.btn_time_get.setOnClickListener { deviceListLogic.timeLogic.getTime(meshNode) }

        view.sw_human_readable.setOnCheckedChangeListener { _, isChecked ->
            setTimeParamsTexts(view, meshNode, isChecked)
        }

        // Local Time button
        view.btn_local_time.setOnClickListener {
            view.et_tai_seconds.setText(TimeParams.getRawLocalTimeTaiSeconds().toString())
            view.et_subsecond.setText(TimeParams.getRawLocalTimeSubsecond().toString())
            view.et_uncertainty.setText("0")
            view.et_time_authority.setText("0")
            view.et_tai_utc_delta.setText(TimeParams.TAI_UTC_DELTA_VALUE.toString())
            view.et_time_zone_offset.setText(TimeParams.getLocalTimeZoneRawOffset())
        }

        view.btn_copy_status_fields.setOnClickListener {
            view.et_tai_seconds.setText(meshNode.taiSeconds.toString())
            view.et_subsecond.setText(meshNode.subsecond.toString())
            view.et_uncertainty.setText(meshNode.uncertainty.toString())
            if (meshNode.timeAuthority) view.et_time_authority.setText("1")
            else view.et_time_authority.setText("0")
            view.et_tai_utc_delta.setText(meshNode.taiUtcDelta.toString())
            view.et_time_zone_offset.setText(meshNode.timeZoneOffset.toString())
        }

        view.btn_time_set.setOnClickListener {
            val timeValuesMap = HashMap<TimeParams.ParameterType, String>()

            if (view.et_tai_seconds.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.TAI_SECONDS] = view.et_tai_seconds.text.toString()
            if (view.et_subsecond.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.SUBSECOND] = view.et_subsecond.text.toString()
            if (view.et_uncertainty.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.UNCERTAINTY] = view.et_uncertainty.text.toString()
            if (view.et_time_authority.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.TIME_AUTHORITY] = view.et_time_authority.text.toString()
            if (view.et_tai_utc_delta.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.TAI_UTC_DELTA] = view.et_tai_utc_delta.text.toString()
            if (view.et_time_zone_offset.text.isNotEmpty()) timeValuesMap[TimeParams.ParameterType.TIME_ZONE_OFFSET] = view.et_time_zone_offset.text.toString()

            if (timeValuesMap.size == 6) deviceListLogic.timeLogic.setTime(timeValuesMap, meshNode, context)
            else MeshToast.show(context, R.string.device_adapter_time_fields_cannot_be_empty)
        }
    }

    private fun setTimeParamsTexts(view: View, meshNode: MeshNode, isChecked: Boolean) {
        if (isChecked) {
            view.tv_tai_seconds.text = TimeParams.getHumanReadableTaiSeconds(meshNode.taiSeconds)
            view.tv_subsecond.text = TimeParams.getHumanReadableSubsecond(meshNode.subsecond)
            view.tv_uncertainty.text = TimeParams.getHumanReadableUncertainty(meshNode.uncertainty)
            view.tv_time_authority.text = TimeParams.getHumanReadableTimeAuthority(meshNode.timeAuthority)
            view.tv_tai_utc_delta.text = TimeParams.getHumanReadableTaiUtcDelta(meshNode.taiUtcDelta)
            view.tv_time_zone_offset.text = TimeParams.getHumanReadableTimeZoneOffset(meshNode.timeZoneOffset)
        } else {
            view.tv_tai_seconds.text = meshNode.taiSeconds.toString()
            view.tv_subsecond.text = meshNode.subsecond.toString()
            view.tv_uncertainty.text = meshNode.uncertainty.toString()
            if (meshNode.timeAuthority) view.tv_time_authority.text = "1"
            else view.tv_time_authority.text = "0"
            view.tv_tai_utc_delta.text = meshNode.taiUtcDelta.toString()
            view.tv_time_zone_offset.text = meshNode.timeZoneOffset.toString()
        }
    }
}