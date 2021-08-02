/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.text.InputType
import android.view.View
import android.widget.AdapterView
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.LC.LightLCProperty
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_light_lc.view.*

class LightLCViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            scenes_layout.visibility = View.VISIBLE
            iv_device_image.visibility = View.GONE

            sw_lc_mode.setOnCheckedChangeListener(null)
            sw_lc_occupancy_mode.setOnCheckedChangeListener(null)
            sw_lc_on_off.setOnCheckedChangeListener(null)
            sw_lc_mode.isChecked = meshNode.lcMode
            sw_lc_occupancy_mode.isChecked = meshNode.lcOccupancyMode
            sw_lc_on_off.isChecked = meshNode.lcOnOff
            tv_lc_property_value.text = meshNode.lcPropertyValue

            sp_property_id.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    et_lc_property_data.setText("")
                    tv_lc_property_value.text = "---"
                    meshNode.lcPropertyValue = "---"

                    when (LightLCProperty.values()[position].characteristic) {
                        LightLCProperty.Characteristic.Illuminance -> {
                            et_lc_property_data.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            tv_lc_property_unit.text = context.getString(R.string.device_adapter_lc_illuminance_unit)
                        }
                        LightLCProperty.Characteristic.PerceivedLightness -> {
                            et_lc_property_data.inputType = InputType.TYPE_CLASS_NUMBER
                            tv_lc_property_unit.text = ""
                        }
                        LightLCProperty.Characteristic.Percentage8 -> {
                            et_lc_property_data.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            tv_lc_property_unit.text = context.getString(R.string.device_adapter_lc_percentage_unit)
                        }
                        LightLCProperty.Characteristic.Coefficient -> {
                            et_lc_property_data.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                            tv_lc_property_unit.text = ""
                        }
                        LightLCProperty.Characteristic.TimeMillisecond24 -> {
                            et_lc_property_data.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            tv_lc_property_unit.text = context.getString(R.string.device_adapter_lc_time_unit)
                        }
                    }
                }
            }

            //mode
            iv_lc_mode_refresh.setOnClickListener {
                deviceListLogic.lcLogic.refreshLCMode(meshNode, RefreshNodeListener(iv_lc_mode_refresh))
            }
            sw_lc_mode.setOnCheckedChangeListener { _, isChecked ->
                deviceListLogic.lcLogic.setLCMode(meshNode, isChecked)
            }
            //occupancy mode
            iv_lc_occupancy_mode_refresh.setOnClickListener {
                deviceListLogic.lcLogic.refreshLCOccupancyMode(meshNode, RefreshNodeListener(iv_lc_occupancy_mode_refresh))
            }
            sw_lc_occupancy_mode.setOnCheckedChangeListener { _, isChecked ->
                deviceListLogic.lcLogic.setLCOccupancyMode(meshNode, isChecked)
            }
            //on off
            iv_lc_on_off_refresh.setOnClickListener {
                deviceListLogic.lcLogic.refreshLCLightOnOff(meshNode, RefreshNodeListener(iv_lc_on_off_refresh))
            }
            sw_lc_on_off.setOnCheckedChangeListener { _, isChecked ->
                deviceListLogic.lcLogic.setLCOnOff(meshNode, isChecked)
            }
            //property
            iv_lc_property_refresh.setOnClickListener {
                deviceListLogic.lcLogic.refreshLCProperty(meshNode, sp_property_id.selectedItemPosition, RefreshNodeListener(iv_lc_property_refresh))
            }
            btn_lc_property_send.setOnClickListener {
                deviceListLogic.lcLogic.setLCProperty(meshNode, sp_property_id.selectedItemPosition, et_lc_property_data.text.toString())
            }

            setEnabledControls(isNetworkConnected)
        }
    }
}