/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.view.View
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_lightness.view.*

class LevelViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            scenes_layout.visibility = View.VISIBLE
            iv_refresh.visibility = View.VISIBLE

            iv_device_image.setOnClickListener(ClickDeviceImageListener(meshNode))
            iv_refresh.setOnClickListener(ClickRefreshListener(meshNode, iv_refresh))

            sb_level_control.progress = meshNode.levelPercentage
            sb_level_control.setOnSeekBarChangeListener(ControlChangeListener(meshNode))
            tv_level_value.text = context.getString(R.string.device_adapter_lightness_value).format(sb_level_control.progress)

            if (!isNetworkConnected) {
                iv_device_image.setImageResource(R.drawable.lamp_disabled)
            } else if (meshNode.levelPercentage > 0) {
                iv_device_image.setImageResource(R.drawable.lamp_on)
            } else {
                iv_device_image.setImageResource(R.drawable.lamp_off)
            }
            setEnabledControls(isNetworkConnected)
        }
    }

}