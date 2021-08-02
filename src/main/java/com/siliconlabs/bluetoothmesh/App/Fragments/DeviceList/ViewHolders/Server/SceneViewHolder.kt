/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.Server

import android.view.View
import android.widget.TextView
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.ViewHolders.DeviceViewHolderBase
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.devices_adapter_base_header.view.*
import kotlinx.android.synthetic.main.devices_adapter_base_scene.view.*
import kotlinx.android.synthetic.main.devices_adapter_scene.view.*

class SceneViewHolder(view: View, deviceListLogic: DeviceListAdapterLogic) : DeviceViewHolderBase(view, deviceListLogic) {
    override fun bindView(meshNode: MeshNode, isNetworkConnected: Boolean) {
        super.bindView(meshNode, isNetworkConnected)
        view.apply {
            swipe.setup(meshNode)

            scenes_layout.visibility = View.VISIBLE

            iv_device_image.setImageResource(R.drawable.ic_scene)

            iv_scene_refresh.setOnClickListener {
                deviceListLogic.sceneLogic.refreshSceneStatus(meshNode, RefreshNodeListener(iv_scene_refresh))
            }
            // scene one
            btn_scene_one_recall.setOnClickListener {
                deviceListLogic.sceneLogic.recallScene(meshNode, 1)
            }
            iv_scene_one_remove.setOnClickListener {
                deviceListLogic.sceneLogic.deleteScene(meshNode, 1)
            }
            setSceneStatus(tv_scene_one_status, meshNode.sceneOneStatus)
            // scene two
            btn_scene_two_recall.setOnClickListener {
                deviceListLogic.sceneLogic.recallScene(meshNode, 2)
            }
            iv_scene_two_remove.setOnClickListener {
                deviceListLogic.sceneLogic.deleteScene(meshNode, 2)
            }
            setSceneStatus(tv_scene_two_status, meshNode.sceneTwoStatus)

            setEnabledControls(isNetworkConnected)
        }
    }

    private fun setSceneStatus(tvSceneStatus: TextView, sceneStatus: MeshNode.SceneStatus) {
        tvSceneStatus.apply {
            val greyColor = resources.getColor(R.color.adapter_item_label_color, null)
            val whiteColor = resources.getColor(R.color.adapter_item_title_color, null)
            val greenColor = resources.getColor(R.color.adapter_item_active_color, null)

            when (sceneStatus) {
                MeshNode.SceneStatus.NOT_KNOWN -> {
                    text = resources.getString(R.string.device_adapter_scenes_not_known_state)
                    setTextColor(greyColor)
                }
                MeshNode.SceneStatus.NOT_STORED -> {
                    text = resources.getString(R.string.device_adapter_scenes_not_stored_state)
                    setTextColor(greyColor)
                }
                MeshNode.SceneStatus.STORED -> {
                    text = resources.getString(R.string.device_adapter_scenes_stored_state)
                    setTextColor(whiteColor)
                }
                MeshNode.SceneStatus.ACTIVE -> {
                    text = resources.getString(R.string.device_adapter_scenes_active_state)
                    setTextColor(greenColor)
                }
            }
        }
    }
}