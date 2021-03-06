/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device.Info

import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode

interface DeviceInfoView {

    fun setDeviceInfo(deviceInfo: MeshNode)
}