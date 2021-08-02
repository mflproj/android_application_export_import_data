/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders

import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Views.RefreshNodeButton
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_swipe_menu.view.*

abstract class DeviceViewHolderBase(val view: View, val deviceListLogic: DeviceListAdapterLogic) : RecyclerView.ViewHolder(view) {

    open fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        val node = meshNode.node
        view.apply {
            tv_device_name.text = node.name ?: ""
            tv_device_id.text = node.primaryElementAddress?.toString()
            tv_device_proxy.visibility = if (node.isConnectedAsProxy) View.VISIBLE else View.GONE

            iv_refresh.visibility = View.GONE
            scenes_layout.visibility = View.GONE

            iv_config.setOnClickListener {
                deviceListLogic.deviceListAdapterListener.onConfigClickListener(meshNode)
            }
            iv_remove.setOnClickListener {
                deviceListLogic.deviceListAdapterListener.onDeleteClickListener(meshNode)
            }

            setupScene(meshNode)
        }
    }

    private fun setupScene(meshNode: MeshNode) {
        view.apply {
            btn_store_scene_one.setOnClickListener {
                deviceListLogic.sceneLogic.storeScene(meshNode, 1)
            }
            btn_store_scene_two.setOnClickListener {
                deviceListLogic.sceneLogic.storeScene(meshNode, 2)
            }
        }
    }

    //

    fun SwipeLayout.setup(meshNode: MeshNode) {
        surfaceView.setOnClickListener {
            deviceListLogic.deviceListAdapterListener.onConfigClickListener(meshNode)
        }
        surfaceView.setOnLongClickListener {
            this@setup.open()
            return@setOnLongClickListener true
        }
        showMode = SwipeLayout.ShowMode.LayDown
        addDrag(SwipeLayout.DragEdge.Right, swipe_menu)
    }

    fun setEnabledControls(enabled: Boolean) {
        setEnabledControls(view, enabled)
        setEnabledControls(view.swipe_menu, true)
        if (!enabled) view.tv_device_proxy.visibility = View.GONE
    }

    private fun setEnabledControls(view: View, enabled: Boolean) {
        if (view is ViewGroup && view !is AppCompatSpinner) {
            for (i in 0 until view.childCount) {
                setEnabledControls(view.getChildAt(i), enabled)
            }
        } else {
            view.isEnabled = enabled
            view.alpha = if (enabled) 1f else 0.5f
        }
    }

    inner class ClickRefreshListener(private val deviceInfo: MeshNode,
                                     private val refreshNodeButton: RefreshNodeButton) : View.OnClickListener {
        override fun onClick(v: View?) {
            deviceListLogic.onRefreshClick(deviceInfo, RefreshNodeListener(refreshNodeButton))
        }
    }

    inner class ControlChangeListener(private val deviceInfo: MeshNode) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.apply {
                deviceListLogic.onSeekBarChange(deviceInfo, progress)
            }
        }
    }

    inner class ClickDeviceImageListener(private val deviceInfo: MeshNode) : View.OnClickListener {
        override fun onClick(v: View?) {
            deviceListLogic.onClickDeviceImage(deviceInfo)
        }
    }

    protected class RefreshNodeListener(private val refreshNodeButton: RefreshNodeButton) : DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener {

        override fun startRefresh() {
            refreshNodeButton.startRefresh()
        }

        override fun stopRefresh() {
            refreshNodeButton.stopRefresh()
        }
    }
}