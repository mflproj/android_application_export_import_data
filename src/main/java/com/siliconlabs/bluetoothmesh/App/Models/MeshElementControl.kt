/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Models

import android.util.Log
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.element.Element
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlab.bluetoothmesh.adk.data_model.model.ModelIdentifier
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlab.bluetoothmesh.adk.data_model.scene.Scene
import com.siliconlab.bluetoothmesh.adk.functionality_control.ControlElement
import com.siliconlab.bluetoothmesh.adk.functionality_control.GetElementStatusCallback
import com.siliconlab.bluetoothmesh.adk.functionality_control.SetElementStatusCallback
import com.siliconlab.bluetoothmesh.adk.functionality_control.base.ControlRequestParameters
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.*
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.get.LightControlLightOnOffGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.get.LightControlModeGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.get.LightControlOccupancyModeGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.get.LightControlPropertyGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.set.LightControlLightOnOffSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.set.LightControlModeSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.set.LightControlOccupancyModeSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.set.LightControlPropertySet
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlLightOnOffStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlModeStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlOccupancyModeStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.light_control.status.LightControlPropertyStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.SceneElementCallback
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.SceneMessageFlags
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.request.*
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.response.SceneRegisterStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.response.SceneStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.request.get.SchedulerActionGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.request.get.SchedulerGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.request.set.SchedulerActionSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerActionStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerElementCallback
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerResponse
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.specific.*
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.TimeElementCallback
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.TimeRole
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.get.TimeGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.get.TimeRoleGet
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.set.TimeRoleSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.set.TimeSet
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.status.TimeRoleStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.time.status.TimeStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Time.TimeParams
import com.siliconlabs.bluetoothmesh.App.Utils.ControlConverters

class MeshElementControl(val node: Node, val group: Group) {
    private val TAG: String = javaClass.canonicalName!!
    private fun createControllerForModel(model: ModelIdentifier): ControlElement? {
        return node.elements
                ?.find { it.sigModels.any { it.id == model.id } }
                ?.let { ControlElement(it, group) }
    }

    // generic
    fun getOnOff(callback: GetOnOffCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.GenericOnOffServer)

        controlElement?.getStatus(GenericOnOff(), object : GetElementStatusCallback<GenericOnOff> {
            override fun success(element: Element?, group: Group?, value: GenericOnOff?) {
                Log.d(TAG, "getStatus GenericOnOff success: ${value!!.state}")

                callback.success(value.state == GenericOnOff.STATE.ON)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getStatus GenericOnOff error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setOnOff(on: Boolean, transactionId: Int, callback: SetCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.GenericOnOffServer)
        val generic = GenericOnOff()
        generic.state = if (on) GenericOnOff.STATE.ON else GenericOnOff.STATE.OFF
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlElement?.setStatus(generic, parameters, object : SetElementStatusCallback<GenericOnOff> {
            override fun success(element: Element?, group: Group?, value: GenericOnOff?) {
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getLevel(callback: GetLevelCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.GenericLevelServer)

        controlElement?.getStatus(GenericLevel(), object : GetElementStatusCallback<GenericLevel> {
            override fun success(element: Element?, group: Group?, value: GenericLevel?) {
                Log.d(TAG, "getStatus GenericLevel success: ${value!!.level}")
                val levelPercentage = ControlConverters.getLevelPercentage(value.level)
                callback.success(levelPercentage)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getStatus GenericLevel error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLevel(levelPercentage: Int, transactionId: Int, callback: SetCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.GenericLevelServer)
        val generic = GenericLevel()
        generic.level = ControlConverters.getLevel(levelPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlElement?.setStatus(generic, parameters, object : SetElementStatusCallback<GenericLevel> {
            override fun success(element: Element?, group: Group?, value: GenericLevel?) {
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getLightness(callback: GetLightnessCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLightnessServer)

        controlElement?.getStatus(LightingLightnessActual(), object : GetElementStatusCallback<LightingLightnessActual> {
            override fun success(element: Element?, group: Group?, value: LightingLightnessActual?) {
                Log.d(TAG, "getStatus LightingLightnessActual success: ${value!!.lightness}")
                val lightnessPercentage = ControlConverters.getLightnessPercentage(value.lightness)
                callback.success(lightnessPercentage)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getStatus LightingLightnessActual error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLightness(lightnessPercentage: Int, transactionId: Int, callback: SetCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLightnessServer)
        val generic = LightingLightnessActual()
        generic.lightness = ControlConverters.getLightness(lightnessPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlElement?.setStatus(generic, parameters, object : SetElementStatusCallback<LightingLightnessActual> {
            override fun success(element: Element?, group: Group?, value: LightingLightnessActual?) {
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getColorTemperature(callback: GetColorTemperatureCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightCTLServer)

        controlElement?.getStatus(LightingCTLGet(), object : GetElementStatusCallback<LightingCTLGet> {
            override fun success(element: Element?, group: Group?, value: LightingCTLGet?) {
                Log.d(TAG, "getStatus LightingCTLGet success: ${value!!.lightness} ${value.temperature}")
                val lightnessPercentage = ControlConverters.getLightnessPercentage(value.lightness)
                val temperaturePercentage = ControlConverters.getTemperaturePercentage(value.temperature)

                callback.success(lightnessPercentage, temperaturePercentage)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getStatus LightingCTLGet error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setColorTemperature(lightnessPercentage: Int, temperaturePercentage: Int, deltaUvPercentage: Int, transactionId: Int, callbackPercentage: SetCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightCTLServer)
        val generic = LightingCTLSet()
        generic.lightness = ControlConverters.getLightness(lightnessPercentage)
        generic.temperature = ControlConverters.getTemperature(temperaturePercentage)
        generic.deltaUv = ControlConverters.getDeltaUv(deltaUvPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlElement?.setStatus(generic, parameters, object : SetElementStatusCallback<LightingCTLSet> {
            override fun success(element: Element?, group: Group?, value: LightingCTLSet?) {
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                callbackPercentage.error(error!!)
            }
        })
    }

    fun getColorDeltaUv(callback: GetColorDeltaUvCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightCTLTemperatureServer)

        controlElement?.getStatus(LightingCTLTemperature(), object : GetElementStatusCallback<LightingCTLTemperature> {
            override fun success(element: Element?, group: Group?, value: LightingCTLTemperature?) {
                Log.d(TAG, "getStatus LightingCTLTemperature success: ${value!!.temperature} ${value.deltaUv}")
                val temperaturePercentage = ControlConverters.getTemperaturePercentage(value.temperature)
                val deltaUvPercentage = ControlConverters.getDeltaUvPercentage(value.deltaUv)

                callback.success(temperaturePercentage, deltaUvPercentage)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getStatus LightingCTLTemperature error: $error")

                callback.error(error!!)
            }
        })
    }

    // lc

    fun getLCOnOff(callback: LightControlLightOnOffStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlLightOnOffGet()
        controlElement?.getLightControlValue(request, object : LightControlElementCallback<LightControlLightOnOffStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlLightOnOffStatus?) {
                Log.d(TAG, "getLCOnOff success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getLCOnOff error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLCOnOff(lightOnOff: LightControlLightOnOff, transactionId: Int, transitionTime: Int?, delay: Int?, acknowledged: Boolean, callback: LightControlLightOnOffStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlLightOnOffSet(lightOnOff, transactionId).apply {
            this.transitionTime = transitionTime
            this.delay = delay
            flags = LightControlMessageFlags.withAcknowledged(acknowledged)
        }

        controlElement?.setLightControlValue(request, object : LightControlElementCallback<LightControlLightOnOffStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlLightOnOffStatus?) {
                Log.d(TAG, "setLCOnOff success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "setLCOnOff error: $error")

                callback.error(error!!)
            }
        })
    }

    fun getLCProperty(propertyId: Int, callback: LightControlPropertyStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlPropertyGet(propertyId)

        controlElement?.getLightControlValue(request, object : LightControlElementCallback<LightControlPropertyStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlPropertyStatus?) {
                Log.d(TAG, "getLCProperty success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getLCProperty error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLCProperty(propertyId: Int, propertyValue: ByteArray, acknowledged: Boolean, callback: LightControlPropertyStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlPropertySet(propertyId, propertyValue,
                LightControlMessageFlags.withAcknowledged(acknowledged))

        controlElement?.setLightControlValue(request, object : LightControlElementCallback<LightControlPropertyStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlPropertyStatus?) {
                Log.d(TAG, "setLCProperty success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "setLCProperty error: $error")

                callback.error(error!!)
            }
        })
    }

    fun getLCMode(callback: LightControlModeStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlModeGet()

        controlElement?.getLightControlValue(request, object : LightControlElementCallback<LightControlModeStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlModeStatus?) {
                Log.d(TAG, "getLCMode success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getLCMode error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLCMode(newMode: LightControlMode, acknowledged: Boolean, callback: LightControlModeStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlModeSet(newMode,
                LightControlMessageFlags.withAcknowledged(acknowledged))

        controlElement?.setLightControlValue(request, object : LightControlElementCallback<LightControlModeStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlModeStatus?) {
                Log.d(TAG, "setLCMode LightControlModeSet success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "setLCMode LightControlModeSet error: $error")

                callback.error(error!!)
            }
        })
    }

    fun getLCOccupancyMode(callback: LightControlOccupancyModeStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlOccupancyModeGet()

        controlElement?.getLightControlValue(request, object : LightControlElementCallback<LightControlOccupancyModeStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlOccupancyModeStatus?) {
                Log.d(TAG, "getLCOccupancyMode success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getLCOccupancyMode error: $error")

                callback.error(error!!)
            }
        })
    }

    fun setLCOccupancyMode(newMode: LightControlOccupancyMode, acknowledged: Boolean, callback: LightControlOccupancyModeStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.LightLCServer)
        val request = LightControlOccupancyModeSet(newMode,
                LightControlMessageFlags.withAcknowledged(acknowledged))

        controlElement?.setLightControlValue(request, object : LightControlElementCallback<LightControlOccupancyModeStatus> {
            override fun success(element: Element?, group: Group?, status: LightControlOccupancyModeStatus?) {
                Log.d(TAG, "setLCOccupancyMode success: $status")

                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "setLCOccupancyMode error: $error")

                callback.error(error!!)
            }
        })
    }

    // time

    fun getTimeRole(callback: TimeRoleCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.TimeServer)
        val request = TimeRoleGet()

        controlElement?.getTimeValue(request, object : TimeElementCallback<TimeRoleStatus> {
            override fun success(element: Element?, group: Group?, status: TimeRoleStatus?) {
                Log.d(TAG, "getTimeRole success: $status")
                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getTimeRole error: $error")
                callback.error(error!!)
            }
        })
    }

    fun setTimeRole(timeRole: TimeRole, callback: TimeRoleCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.TimeServer)
        val request = TimeRoleSet(timeRole)

        controlElement?.setTimeValue(request, object : TimeElementCallback<TimeRoleStatus> {
            override fun success(element: Element?, group: Group?, status: TimeRoleStatus?) {
                Log.d(TAG, "setTimeRole success: $status")
                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "setTimeRole error: $error")
                callback.error(error!!)
            }
        })
    }

    fun getTime(callback: TimeCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.TimeServer)
        val request = TimeGet()

        controlElement?.getTimeValue(request, object : TimeElementCallback<TimeStatus> {
            override fun success(element: Element?, group: Group?, status: TimeStatus?) {
                Log.d(TAG, "getTime success: $status")
                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                Log.d(TAG, "getTime error: $error")

                callback.error(error!!)
            }

        })
    }

    fun setTime(timeParams: TimeParams, callback: TimeCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.TimeServer)
        val request = TimeSet(timeParams.taiSeconds, timeParams.subsecond, timeParams.uncertainty, timeParams.timeAuthority == 1,
                timeParams.taiUtcDelta, timeParams.timeZoneOffset)

        controlElement?.setTimeValue(request, object : TimeElementCallback<TimeStatus> {
            override fun success(element: Element?, group: Group?, status: TimeStatus?) {
                callback.success(status)
            }

            override fun error(element: Element?, group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    // scene

    fun getScene(callback: SceneStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.SceneServer)
        val funName = "getScene"
        Log.d(TAG, funName)

        controlElement?.getSceneValue(SceneGet(), SceneStatusCallbackHandler(funName, callback))
    }

    fun getSceneRegister(callback: SceneRegisterStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.SceneServer)
        val funName = "getSceneRegister"
        Log.d(TAG, funName)

        controlElement?.getSceneValue(SceneRegisterGet(), SceneRegisterStatusCallbackHandler(funName, callback))
    }

    fun recallScene(acknowledged: Boolean, sceneNumber: Int, tid: Int, transitionTime: Int?, delay: Int?, callback: SceneStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.SceneServer)
        val funName = "recallScene"
        Log.d(TAG, funName)

        val scene = createScene(sceneNumber)
        val flags = SceneMessageFlags().apply { this.acknowledged = acknowledged }
        val request = SceneRecall(scene, tid, flags).apply {
            this.transitionTime = transitionTime
            this.delay = delay
        }

        controlElement?.changeSceneRegister(request, SceneStatusCallbackHandler(funName, callback))
    }

    fun storeScene(acknowledged: Boolean, sceneNumber: Int, callback: SceneRegisterStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.SceneServer)
        val funName = "storeScene"
        Log.d(TAG, funName)

        val scene = createScene(sceneNumber)
        val flags = SceneMessageFlags().apply { this.acknowledged = acknowledged }
        val request = SceneStore(scene, flags)

        controlElement?.changeSceneRegister(request, SceneRegisterStatusCallbackHandler(funName, callback))
    }

    fun deleteScene(acknowledged: Boolean, sceneNumber: Int, callback: SceneRegisterStatusCallback) {
        val controlElement = createControllerForModel(ModelIdentifier.SceneServer)
        val funName = "deleteScene"
        Log.d(TAG, funName)

        val scene = createScene(sceneNumber)
        val flags = SceneMessageFlags().apply { this.acknowledged = acknowledged }
        val request = SceneDelete(scene, flags)

        controlElement?.changeSceneRegister(request, SceneRegisterStatusCallbackHandler(funName, callback))
    }

    private fun createScene(sceneNumber: Int): Scene {
        val network = group.subnet.network

        return network.createScene("", sceneNumber)
                ?: network.scenes.find { it.number == sceneNumber }!!
    }

    fun getScheduleRegister(callback: SchedulerCallback<SchedulerStatus>) {
        val controlElement = createControllerForModel(ModelIdentifier.SchedulerServer)
        controlElement?.getScheduleRegister(SchedulerGet(), SchedulerControlCallback(callback))
    }

    fun getSchedulerAction(index: Int, callback: SchedulerCallback<SchedulerActionStatus>) {
        val controlElement = createControllerForModel(ModelIdentifier.SchedulerServer)
        controlElement?.getScheduleRegister(SchedulerActionGet(index), SchedulerControlCallback(callback))
    }

    fun setSchedulerAction(request: SchedulerActionSet, callback: SchedulerCallback<SchedulerActionStatus>) {
        val controlElement = createControllerForModel(ModelIdentifier.SchedulerServer)
        controlElement?.setScheduleRegister(request, SchedulerControlCallback(callback))
    }

    private class SchedulerControlCallback<T : SchedulerResponse>(private val callback: SchedulerCallback<T>) : SchedulerElementCallback<T?> {
        override fun error(element: Element?, group: Group?, errorType: ErrorType) {
            callback.error(errorType)
        }

        override fun success(element: Element?, group: Group?, response: T?) {
            callback.success(response!!)
        }
    }

    inner class SceneStatusCallbackHandler(private val funName: String, private val callback: SceneStatusCallback) : SceneElementCallback<SceneStatus> {
        override fun success(element: Element?, group: Group?, status: SceneStatus?) {
            Log.d(TAG, "$funName SUCCESS")
            callback.success(status)
        }

        override fun error(element: Element?, group: Group?, error: ErrorType?) {
            Log.d(TAG, "$funName ERROR: $error")
            callback.error(error!!)
        }
    }

    inner class SceneRegisterStatusCallbackHandler(private val funName: String, val callback: SceneRegisterStatusCallback) : SceneElementCallback<SceneRegisterStatus> {
        override fun success(element: Element?, group: Group?, status: SceneRegisterStatus?) {
            Log.d(TAG, "$funName SUCCESS")
            callback.success(status)
        }

        override fun error(element: Element?, group: Group?, error: ErrorType?) {
            Log.d(TAG, "$funName ERROR: $error")

            callback.error(error!!)
        }
    }

    interface SetCallback : BaseCallback

    interface SceneRegisterStatusCallback : BaseCallback {
        fun success(sceneRegisterStatus: SceneRegisterStatus?)
    }

    interface SceneStatusCallback : BaseCallback {
        fun success(sceneStatus: SceneStatus?)
    }

    interface LightControlLightOnOffStatusCallback : BaseCallback {
        fun success(status: LightControlLightOnOffStatus?)
    }

    interface LightControlPropertyStatusCallback : BaseCallback {
        fun success(status: LightControlPropertyStatus?)
    }

    interface LightControlModeStatusCallback : BaseCallback {
        fun success(status: LightControlModeStatus?)
    }

    interface LightControlOccupancyModeStatusCallback : BaseCallback {
        fun success(status: LightControlOccupancyModeStatus?)
    }

    interface GetOnOffCallback : BaseCallback {
        fun success(on: Boolean)
    }

    interface GetLevelCallback : BaseCallback {
        fun success(level: Int)
    }

    interface GetLightnessCallback : BaseCallback {
        fun success(lightness: Int)
    }

    interface GetColorTemperatureCallback : BaseCallback {
        fun success(lightness: Int, temperature: Int)
    }

    interface GetColorDeltaUvCallback : BaseCallback {
        fun success(temperature: Int, deltaUv: Int)
    }

    interface SchedulerCallback<T : SchedulerResponse> : BaseCallback {
        fun success(status: T)
    }

    interface TimeRoleCallback : BaseCallback {
        fun success(status: TimeRoleStatus?)
    }

    interface TimeCallback : BaseCallback {
        fun success(status: TimeStatus?)
    }

    interface BaseCallback {
        fun error(error: ErrorType)
    }

    companion object {
        const val DEFAULT_TRANSITION_TIME = 1
        const val DEFAULT_DELAY_TIME = 1
        const val DEFAULT_REQUEST_REPLY = false
    }
}