/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment

import com.siliconlabs.bluetoothmesh.App.Logic.BluetoothStateReceiver
import com.siliconlabs.bluetoothmesh.App.Logic.LocationStateReceiver
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import dagger.Module
import dagger.Provides

@Module
class MainFragmentModule {
    @Provides
    fun provideMainFragmentView(mainFragment: MainFragment): MainFragmentView {
        return mainFragment
    }

    @Provides
    fun provideMainFragmentPresenter(mainFragmentView: MainFragmentView, bluetoothStateReceiver: BluetoothStateReceiver, locationStateReceiver: LocationStateReceiver, meshLogic: MeshLogic): MainFragmentPresenter {
        return MainFragmentPresenter(mainFragmentView, bluetoothStateReceiver, locationStateReceiver, meshLogic)
    }
}