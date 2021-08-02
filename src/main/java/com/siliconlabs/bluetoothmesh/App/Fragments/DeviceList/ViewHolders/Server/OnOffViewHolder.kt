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
import kotlinx.android.synthetic.main.devices_adapter_default.view.*

class OnOffViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            scenes_layout.visibility = View.VISIBLE
            iv_refresh.visibility = View.VISIBLE

            iv_device_image.setOnClickListener(ClickDeviceImageListener(meshNode))
            iv_refresh.setOnClickListener(ClickRefreshListener(meshNode, iv_refresh))

            if (!isNetworkConnected) {
                iv_device_image.setImageResource(R.drawable.toggle_off)
            } else if (meshNode.onOffState) {
                iv_device_image.setImageResource(R.drawable.toggle_on)
            } else {
                iv_device_image.setImageResource(R.drawable.toggle_off)
            }

            setEnabledControls(isNetworkConnected)
        }
    }

}