/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.view.View
import android.widget.SeekBar
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Utils.ControlConverters
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_ctl.view.*

class CTLViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            scenes_layout.visibility = View.VISIBLE
            iv_refresh.visibility = View.VISIBLE

            iv_device_image.setOnClickListener(ClickDeviceImageListener(meshNode))
            iv_refresh.setOnClickListener(ClickRefreshListener(meshNode, iv_refresh))

            sb_level_control.progress = meshNode.lightnessPercentage
            tv_level_value.text = context.getString(R.string.device_adapter_lightness_value).format(sb_level_control.progress)

            sb_temperature_control.progress = meshNode.temperaturePercentage
            tv_temperature_value.text = context.getString(R.string.device_adapter_temperature_value).format(ControlConverters.getTemperature(meshNode.temperaturePercentage))

            sb_uv_control.progress = meshNode.deltaUvPercentage
            tv_uv_value.text = context.getString(R.string.device_adapter_delta_uv_value).format(ControlConverters.getDeltaUvToShow(meshNode.deltaUvPercentage))

            sb_level_control.setOnSeekBarChangeListener(CTLControlChangeListener(meshNode, sb_level_control, sb_temperature_control, sb_uv_control))
            sb_temperature_control.setOnSeekBarChangeListener(CTLControlChangeListener(meshNode, sb_level_control, sb_temperature_control, sb_uv_control))
            sb_uv_control.setOnSeekBarChangeListener(CTLControlChangeListener(meshNode, sb_level_control, sb_temperature_control, sb_uv_control))

            if (!isNetworkConnected) {
                iv_device_image.setImageResource(R.drawable.lamp_disabled)
            } else if (meshNode.lightnessPercentage > 0) {
                iv_device_image.setImageResource(R.drawable.lamp_on)
            } else {
                iv_device_image.setImageResource(R.drawable.lamp_off)
            }
            setEnabledControls(isNetworkConnected)
        }
    }

    inner class CTLControlChangeListener(private val deviceInfo: MeshNode, private val levelSeekBar: SeekBar,
                                         private val temperatureSeekBar: SeekBar, private val uvSeekBar: SeekBar) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let {
                deviceListLogic.onSeekBarChange(deviceInfo, levelSeekBar.progress, temperatureSeekBar.progress, uvSeekBar.progress)
            }
        }
    }
}