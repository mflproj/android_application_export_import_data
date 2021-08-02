/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters

import androidx.annotation.StringRes
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.SceneStatusCode
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.LC.DeviceListAdapterLCLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scene.DeviceListAdapterSceneLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scheduler.DeviceListAdapterSchedulerLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Time.DeviceListAdapterTimeLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.App.Models.TransactionId

class DeviceListAdapterLogic(val listener: DeviceListAdapterLogicListener, val deviceListAdapterListener: DeviceListAdapter.DeviceListAdapterListener) {

    val lcLogic = DeviceListAdapterLCLogic(listener)
    val sceneLogic = DeviceListAdapterSceneLogic(listener)
    val timeLogic = DeviceListAdapterTimeLogic(listener)
    val schedulerLogic = DeviceListAdapterSchedulerLogic(listener)

    interface DeviceListAdapterLogicListener {

        fun showToast(@StringRes messageId: Int)
        fun showToast(@StringRes messageId: Int, arg1: String)
        fun showToast(@StringRes messageId: Int, arg1: String, arg2: String)
        fun showToast(message: String)
        fun showToast(errorType: ErrorType)
        fun showToast(sceneStatusCode: SceneStatusCode)

        fun notifyItemChanged(item: MeshNode)
    }

    private val setCallback = object : MeshElementControl.SetCallback {
        override fun error(error: ErrorType) {
            listener.showToast(error)
        }
    }

    fun onClickDeviceImage(meshNode: MeshNode) {
        val meshElementControl = getMeshElementControl(meshNode)

        when (meshNode.functionality) {
            DeviceFunctionality.FUNCTIONALITY.OnOff -> {
                val newOnOffState = !meshNode.onOffState

                meshElementControl?.setOnOff(newOnOffState, transactionId.next(), setCallback)
                meshNode.onOffState = newOnOffState
            }
            DeviceFunctionality.FUNCTIONALITY.Level -> {
                var newLevelPercentage = 100
                if (meshNode.levelPercentage > 0) {
                    newLevelPercentage = 0
                }

                meshElementControl?.setLevel(newLevelPercentage, transactionId.next(), setCallback)
                meshNode.levelPercentage = newLevelPercentage
            }
            DeviceFunctionality.FUNCTIONALITY.Lightness -> {
                var newLightnessPercentage = 100
                if (meshNode.lightnessPercentage > 0) {
                    newLightnessPercentage = 0
                }

                meshElementControl?.setLightness(newLightnessPercentage, transactionId.next(), setCallback)
                meshNode.lightnessPercentage = newLightnessPercentage
            }
            DeviceFunctionality.FUNCTIONALITY.CTL -> {
                var newLightnessPercentage = 100
                if (meshNode.lightnessPercentage > 0) {
                    newLightnessPercentage = 0
                }

                meshElementControl?.setColorTemperature(newLightnessPercentage, meshNode.temperaturePercentage, meshNode.deltaUvPercentage, transactionId.next(), setCallback)
                meshNode.lightnessPercentage = newLightnessPercentage
            }
            else -> {
            }
        }

        listener.notifyItemChanged(meshNode)
    }

    fun onRefreshClick(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)

        refreshNodeListener.startRefresh()

        when (meshNode.functionality) {
            DeviceFunctionality.FUNCTIONALITY.OnOff -> {
                meshElementControl?.getOnOff(object : MeshElementControl.GetOnOffCallback {
                    override fun success(on: Boolean) {
                        meshNode.onOffState = on
                        refreshNodeListener.stopRefresh()
                        listener.notifyItemChanged(meshNode)
                    }

                    override fun error(error: ErrorType) {
                        setCallback.error(error)
                        refreshNodeListener.stopRefresh()
                    }
                })
            }
            DeviceFunctionality.FUNCTIONALITY.Level -> {
                meshElementControl?.getLevel(object : MeshElementControl.GetLevelCallback {
                    override fun success(level: Int) {
                        meshNode.levelPercentage = level
                        refreshNodeListener.stopRefresh()
                        listener.notifyItemChanged(meshNode)
                    }

                    override fun error(error: ErrorType) {
                        setCallback.error(error)
                        refreshNodeListener.stopRefresh()
                    }
                })
            }
            DeviceFunctionality.FUNCTIONALITY.Lightness -> {
                meshElementControl?.getLightness(object : MeshElementControl.GetLightnessCallback {
                    override fun success(lightness: Int) {
                        meshNode.lightnessPercentage = lightness
                        refreshNodeListener.stopRefresh()
                        listener.notifyItemChanged(meshNode)
                    }

                    override fun error(error: ErrorType) {
                        setCallback.error(error)
                        refreshNodeListener.stopRefresh()
                    }
                })
            }
            DeviceFunctionality.FUNCTIONALITY.CTL -> {
                meshElementControl?.getColorTemperature(object : MeshElementControl.GetColorTemperatureCallback {
                    override fun success(lightness: Int, temperature: Int) {
                        meshNode.lightnessPercentage = lightness
                        meshNode.temperaturePercentage = temperature

                        meshElementControl.getColorDeltaUv(object : MeshElementControl.GetColorDeltaUvCallback {
                            override fun success(temperature: Int, deltaUv: Int) {
                                meshNode.deltaUvPercentage = deltaUv
                                refreshNodeListener.stopRefresh()
                                listener.notifyItemChanged(meshNode)
                            }

                            override fun error(error: ErrorType) {
                                setCallback.error(error)
                                refreshNodeListener.stopRefresh()
                            }
                        })
                    }

                    override fun error(error: ErrorType) {
                        setCallback.error(error)
                        refreshNodeListener.stopRefresh()
                    }
                })
            }
            else -> {
            }
        }
    }

    fun onSeekBarChange(meshNode: MeshNode, levelPercentage: Int, temperaturePercentage: Int? = null, deltaUvPercentage: Int? = null) {
        val meshElementControl = getMeshElementControl(meshNode)

        when (meshNode.functionality) {
            DeviceFunctionality.FUNCTIONALITY.Level -> {
                meshElementControl?.setLevel(levelPercentage, transactionId.next(), setCallback)

                meshNode.levelPercentage = levelPercentage
            }
            DeviceFunctionality.FUNCTIONALITY.Lightness -> {
                meshElementControl?.setLightness(levelPercentage, transactionId.next(), setCallback)

                meshNode.lightnessPercentage = levelPercentage
            }
            DeviceFunctionality.FUNCTIONALITY.CTL -> {
                if (temperaturePercentage != null && deltaUvPercentage != null) {
                    meshElementControl?.setColorTemperature(levelPercentage, temperaturePercentage, deltaUvPercentage, transactionId.next(), setCallback)

                    meshNode.lightnessPercentage = levelPercentage
                    meshNode.temperaturePercentage = temperaturePercentage
                    meshNode.deltaUvPercentage = deltaUvPercentage
                }
            }
            else -> {
            }
        }

        listener.notifyItemChanged(meshNode)
    }

    companion object {
        fun getMeshElementControl(meshNode: MeshNode): MeshElementControl? {
            if (meshNode.node.groups.isEmpty()) {
                return null
            }
            return MeshElementControl(meshNode.node, meshNode.node.groups.iterator().next())
        }

        private var transactionId = TransactionId()
    }
}