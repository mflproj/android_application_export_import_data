/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config.DeviceConfigFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Info.DeviceInfoFragment
import com.siliconlabs.bluetoothmesh.R

class DevicePageAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DeviceConfigFragment()
            else -> DeviceInfoFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.device_dialog_config_page_title)
            else -> context.getString(R.string.device_dialog_info_page_title)
        }
    }
}