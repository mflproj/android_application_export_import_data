/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App

import com.siliconlabs.bluetoothmesh.App.Activities.Logs.LogsActivity
import com.siliconlabs.bluetoothmesh.App.Activities.Logs.LogsActivityModule
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivityModule
import com.siliconlabs.bluetoothmesh.App.Fragments.ControlGroup.ControlGroupFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.ControlGroup.ControlGroupModule
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config.DeviceConfigFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config.DeviceConfigModule
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.DeviceFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.DeviceModule
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Info.DeviceInfoFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Info.DeviceInfoModule
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListModule
import com.siliconlabs.bluetoothmesh.App.Fragments.GroupList.GroupListFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.GroupList.GroupListModule
import com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment.MainFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment.MainFragmentModule
import com.siliconlabs.bluetoothmesh.App.Fragments.Network.NetworkFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Network.NetworkModule
import com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList.NetworkListFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList.NetworkListModule
import com.siliconlabs.bluetoothmesh.App.Fragments.Scanner.ScannerFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Scanner.ScannerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [(MainActivityModule::class)])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [(LogsActivityModule::class)])
    abstract fun bindLogsActivity(): LogsActivity

    @ContributesAndroidInjector(modules = [(MainFragmentModule::class)])
    abstract fun bindMainFragment(): MainFragment

    @ContributesAndroidInjector(modules = [(NetworkModule::class)])
    abstract fun bindNetworkFragment(): NetworkFragment

    @ContributesAndroidInjector(modules = [(DeviceListModule::class)])
    abstract fun bindDeviceListFragment(): DeviceListFragment

    @ContributesAndroidInjector(modules = [(GroupListModule::class)])
    abstract fun bindGroupListFragment(): GroupListFragment

    @ContributesAndroidInjector(modules = [(ScannerModule::class)])
    abstract fun bindScannerFragment(): ScannerFragment

    @ContributesAndroidInjector(modules = [(NetworkListModule::class)])
    abstract fun bindNetworkListFragment(): NetworkListFragment

    @ContributesAndroidInjector(modules = [(DeviceModule::class)])
    abstract fun bindDeviceDialogFragment(): DeviceFragment

    @ContributesAndroidInjector(modules = [(DeviceInfoModule::class)])
    abstract fun bindDeviceInfoFragment(): DeviceInfoFragment

    @ContributesAndroidInjector(modules = [(DeviceConfigModule::class)])
    abstract fun bindDeviceConfigFragment(): DeviceConfigFragment

    @ContributesAndroidInjector(modules = [(ControlGroupModule::class)])
    abstract fun bindControlGroupFragment(): ControlGroupFragment
}