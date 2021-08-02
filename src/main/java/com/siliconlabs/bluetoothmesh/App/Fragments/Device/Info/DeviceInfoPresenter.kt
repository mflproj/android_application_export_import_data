/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device.Info

import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic

class DeviceInfoPresenter(private var deviceInfoView: DeviceInfoView, private val meshLogic: MeshLogic) : BasePresenter {

    override fun onResume() {
        deviceInfoView.setDeviceInfo(meshLogic.deviceToConfigure!!)
    }

    override fun onPause() {
    }

}