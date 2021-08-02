/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Models

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.element.Element
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlab.bluetoothmesh.adk.functionality_control.ControlGroup
import com.siliconlab.bluetoothmesh.adk.functionality_control.GenericGroupHandler
import com.siliconlab.bluetoothmesh.adk.functionality_control.base.ControlRequestParameters
import com.siliconlab.bluetoothmesh.adk.functionality_control.specific.*
import com.siliconlabs.bluetoothmesh.App.Utils.ControlConverters

class MeshGroupControl(val group: Group) {

    private val controlGroup = ControlGroup(group)

    fun getOnOff(callback: GetOnOffCallback) {
        controlGroup.getStatus(GenericOnOff(), object : GenericGroupHandler<GenericOnOff> {
            override fun success(element: Element?, group: Group?, value: GenericOnOff?) {
                callback.success(value!!.state == GenericOnOff.STATE.ON)
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun setOnOff(on: Boolean, transactionId: Int, callback: SetCallback) {
        val generic = GenericOnOff()
        generic.state = if (on) GenericOnOff.STATE.ON else GenericOnOff.STATE.OFF
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlGroup.setStatus(generic, parameters, object : GenericGroupHandler<GenericOnOff> {
            override fun success(element: Element?, group: Group?, value: GenericOnOff?) {
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getLevel(callback: GetLevelCallback) {
        controlGroup.getStatus(GenericLevel(), object : GenericGroupHandler<GenericLevel> {
            override fun success(element: Element?, group: Group?, value: GenericLevel?) {
                callback.success(value!!.level)
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun setLevel(levelPercentage: Int, transactionId: Int, callback: SetCallback) {
        val generic = GenericLevel()
        generic.level = ControlConverters.getLevel(levelPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlGroup.setStatus(generic, parameters, object : GenericGroupHandler<GenericLevel> {
            override fun success(element: Element?, group: Group?, value: GenericLevel?) {
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getLightness(callback: GetLightnessCallback) {
        controlGroup.getStatus(
                LightingLightnessActual(), object : GenericGroupHandler<LightingLightnessActual> {
            override fun success(element: Element?, group: Group?, value: LightingLightnessActual?) {
                callback.success(value!!.lightness)
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun setLightness(lightnessPercentage: Int, transactionId: Int, callback: SetCallback) {
        val generic = LightingLightnessActual()
        generic.lightness = ControlConverters.getLightness(lightnessPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlGroup.setStatus(generic, parameters, object : GenericGroupHandler<LightingLightnessActual> {
            override fun success(element: Element?, group: Group?, value: LightingLightnessActual?) {
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getColorTemperature(callback: GetColorTemperatureCallback) {
        controlGroup.getStatus(
                LightingCTLGet(), object : GenericGroupHandler<LightingCTLGet> {
            override fun success(element: Element?, group: Group?, value: LightingCTLGet?) {
                callback.success(value!!.lightness, value.temperature)
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun setColorTemperature(lightnessPercentage: Int, temperaturePercentage: Int, deltaUvPercentage: Int, transactionId: Int, callback: SetCallback) {
        val generic = LightingCTLSet()
        generic.lightness = ControlConverters.getLightness(lightnessPercentage)
        generic.temperature = ControlConverters.getTemperature(temperaturePercentage)
        generic.deltaUv = ControlConverters.getDeltaUv(deltaUvPercentage)
        val parameters = ControlRequestParameters(DEFAULT_TRANSITION_TIME, DEFAULT_DELAY_TIME, DEFAULT_REQUEST_REPLY, transactionId)
        controlGroup.setStatus(generic, parameters, object : GenericGroupHandler<LightingCTLSet> {
            override fun success(element: Element?, group: Group?, value: LightingCTLSet?) {
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    fun getColorDeltaUv(callback: GetColorDeltaUvCallback) {
        controlGroup.getStatus(
                LightingCTLTemperature(), object : GenericGroupHandler<LightingCTLTemperature> {
            override fun success(element: Element?, group: Group?, value: LightingCTLTemperature?) {
                callback.success(value!!.temperature, value.deltaUv)
            }

            override fun error(group: Group?, error: ErrorType?) {
                callback.error(error!!)
            }
        })
    }

    interface SetCallback : BaseCallback

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

    interface BaseCallback {
        fun error(error: ErrorType)
    }

    companion object {
        const val DEFAULT_TRANSITION_TIME = 1
        const val DEFAULT_DELAY_TIME = 1
        const val DEFAULT_REQUEST_REPLY = false
    }
}