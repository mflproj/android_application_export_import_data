/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders

import android.view.View
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_default.view.*

class UnknownViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            iv_device_image.setImageResource(R.drawable.ic_unknown)
            setEnabledControls(isNetworkConnected)
        }
    }

}