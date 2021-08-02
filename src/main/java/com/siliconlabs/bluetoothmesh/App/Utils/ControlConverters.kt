/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Utils

import kotlin.math.abs

class ControlConverters {
    companion object {
        private var INT_16_MIN = -32768
        private var INT_16_MAX = 32767

        private var UINT_16_MIN = 0
        private var UINT_16_MAX = 65535

        private var TEMPERATURE_MIN = 800
        private var TEMPERATURE_MAX = 20000

        private var DELTA_UV_MIN = -1.00f
        private var DELTA_UV_MAX = 1.00f

        fun getLevel(percentage: Int): Int {
            val percentageDouble = percentage.toDouble() / 100
            var levelValue = percentageDouble * UINT_16_MAX
            levelValue += INT_16_MIN
            return Math.ceil(levelValue).toInt()
        }

        fun getLevelPercentage(level: Int): Int {
            val levelMoved = level + abs(INT_16_MIN)
            val percentageDouble = levelMoved.toDouble() / UINT_16_MAX
            return Math.floor(percentageDouble * 100).toInt()
        }

        fun getLightness(percentage: Int): Int {
            val percentageDouble = percentage.toDouble() / 100
            return Math.ceil(percentageDouble * UINT_16_MAX).toInt()
        }

        fun getLightnessPercentage(lightness: Int): Int {
            val percentageDouble = lightness.toDouble() / UINT_16_MAX
            return Math.floor(percentageDouble * 100).toInt()
        }

        fun getTemperature(percentage: Int): Int {
            val percentageDouble = percentage.toDouble() / 100
            var temperatureValue = percentageDouble * (TEMPERATURE_MAX - TEMPERATURE_MIN)
            temperatureValue += TEMPERATURE_MIN
            return Math.ceil(temperatureValue).toInt()
        }

        fun getTemperaturePercentage(temperature: Int): Int {
            val temperatureMoved = temperature - TEMPERATURE_MIN
            val percentageDouble = temperatureMoved.toDouble() / (TEMPERATURE_MAX - TEMPERATURE_MIN)
            return Math.floor(percentageDouble * 100).toInt()
        }

        fun getDeltaUv(percentage: Int): Int {
            val percentageDouble = percentage.toDouble() / 100
            var deltaUv = percentageDouble * UINT_16_MAX
            deltaUv += INT_16_MIN
            return Math.ceil(deltaUv).toInt()
        }

        fun getDeltaUvPercentage(deltaUv: Int): Int {
            val deltaUvMoved = deltaUv + abs(INT_16_MIN)
            val percentageDouble = deltaUvMoved.toDouble() / UINT_16_MAX
            return Math.floor(percentageDouble * 100).toInt()
        }

        fun getDeltaUvToShow(percentage: Int): Float {
            val value = (percentage * 2 * DELTA_UV_MAX / 100) + DELTA_UV_MIN
            return when {
                value < DELTA_UV_MIN -> DELTA_UV_MIN
                value > DELTA_UV_MAX -> DELTA_UV_MAX
                else -> value
            }
        }
    }
}