/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config

import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager
import dagger.Module
import dagger.Provides

@Module
class DeviceConfigModule {
    @Provides
    fun provideDeviceConfigView(deviceConfigFragment: DeviceConfigFragment): DeviceConfigView {
        return deviceConfigFragment
    }

    @Provides
    fun provideDeviceConfigPresenter(deviceConfigView: DeviceConfigView, meshLogic: MeshLogic, networkConnectionLogic: NetworkConnectionLogic, meshNodeManager: MeshNodeManager): DeviceConfigPresenter {
        return DeviceConfigPresenter(deviceConfigView, meshLogic, networkConnectionLogic, meshNodeManager)
    }

}