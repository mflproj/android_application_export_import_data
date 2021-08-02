/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device

import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import dagger.Module
import dagger.Provides

@Module
class DeviceModule {
    @Provides
    fun provideDeviceDialogView(deviceFragment: DeviceFragment): DeviceView {
        return deviceFragment
    }

    @Provides
    fun provideDeviceDialogPresenter(deviceView: DeviceView, networkConnectionLogic: NetworkConnectionLogic, meshLogic: MeshLogic): DevicePresenter {
        return DevicePresenter(deviceView, networkConnectionLogic, meshLogic)
    }
}