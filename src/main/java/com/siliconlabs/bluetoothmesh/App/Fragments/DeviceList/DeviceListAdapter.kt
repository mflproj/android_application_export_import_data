/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.SceneStatusCode
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Client.CTLClientViewHolder
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Client.LevelClientViewHolder
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Client.LightnessClientViewHolder
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Client.OnOffClientViewHolder
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server.*
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.UnknownViewHolder
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.App.Views.SwipeRecyclerAdapter
import com.siliconlabs.bluetoothmesh.R
import java.util.*

class DeviceListAdapter(val context: Context,
                        deviceListAdapterListener: DeviceListAdapterListener,
                        private val networkConnectionLogic: NetworkConnectionLogic)
    : DeviceListAdapterLogic.DeviceListAdapterLogicListener, SwipeRecyclerAdapter<MeshNode, DeviceViewHolderBase>(DeviceInfoComparator()) {

    private val deviceListLogic = DeviceListAdapterLogic(this, deviceListAdapterListener)

    override fun getItemViewType(position: Int): Int {
        return getItem(position).functionality.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolderBase {
        val layoutInflater = LayoutInflater.from(context)
        return when (DeviceFunctionality.FUNCTIONALITY.values()[viewType]) {
            // server
            DeviceFunctionality.FUNCTIONALITY.OnOff -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                OnOffViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.Level -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_lightness, parent, false)
                LevelViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.Lightness -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_lightness, parent, false)
                LightnessViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.CTL -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_ctl, parent, false)
                CTLViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.LightLCServer -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_light_lc, parent, false)
                LightLCViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.SceneServer -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_scene, parent, false)
                SceneViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.Scheduler -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_time_scheduler, parent, false)
                TimeSchedulerViewHolder(view, deviceListLogic, context)
            }
            // client
            DeviceFunctionality.FUNCTIONALITY.OnOffClient -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                OnOffClientViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.LevelClient -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                LevelClientViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.LightnessClient -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                LightnessClientViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.CTLClient -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                CTLClientViewHolder(view, deviceListLogic)
            }
            DeviceFunctionality.FUNCTIONALITY.TimeServer -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_time, parent, false)
                TimeViewHolder(view, deviceListLogic)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.devices_adapter_default, parent, false)
                UnknownViewHolder(view, deviceListLogic)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: DeviceViewHolderBase, position: Int) {
        val device = getItem(position)
        viewHolder.bindView(device, networkConnectionLogic.isConnected())
    }

    interface DeviceListAdapterListener {

        fun onDeleteClickListener(deviceInfo: MeshNode)
        fun onConfigClickListener(deviceInfo: MeshNode)

        interface RefreshNodeListener {

            fun startRefresh()
            fun stopRefresh()
        }
    }

// Comparator

    class DeviceInfoComparator : Comparator<MeshNode> {

        override fun compare(o1: MeshNode, o2: MeshNode): Int {
            return o1.node.devKey.keyIndex.compareTo(o2.node.devKey.keyIndex)
        }
    }

//DeviceListAdapterLogicListener

    override fun showToast(@StringRes messageId: Int) {
        MeshToast.show(context, messageId)
    }

    override fun showToast(@StringRes messageId: Int, arg1: String) {
        val messageString = context.getString(messageId, arg1)
        MeshToast.show(context, messageString)
    }

    override fun showToast(@StringRes messageId: Int, arg1: String, arg2: String) {
        val messageString = context.getString(messageId, arg1, arg2)
        MeshToast.show(context, messageString)
    }

    override fun showToast(message: String) {
        MeshToast.show(context, message)
    }

    override fun showToast(errorType: ErrorType) {
        MeshToast.show(context, ErrorMessageConverter.convert(context, errorType))
    }

    override fun showToast(sceneStatusCode: SceneStatusCode) {
        val stringResource = when (sceneStatusCode) {
            SceneStatusCode.SUCCESS -> R.string.device_adapter_scenes_success_status
            SceneStatusCode.SCENE_NOT_FOUND -> R.string.device_adapter_scenes_not_found_status
            SceneStatusCode.SCENE_REGISTER_FULL -> R.string.device_adapter_scenes_register_full_status
            else -> R.string.device_adapter_scenes_wrong_status
        }
        MeshToast.show(context, stringResource)
    }
}