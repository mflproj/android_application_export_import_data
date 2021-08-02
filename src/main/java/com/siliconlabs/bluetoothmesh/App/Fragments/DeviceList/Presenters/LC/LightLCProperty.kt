/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.LC

import com.siliconlabs.bluetoothmesh.App.Utils.Converters


enum class LightLCProperty(val id: Int, val characteristic: Characteristic) {
    AmbientLuxLevelOn(0x002B, Characteristic.Illuminance),
    AmbientLuxLevelProlong(0x002C, Characteristic.Illuminance),
    AmbientLuxLevelStandby(0x002D, Characteristic.Illuminance),
    LightnessOn(0x002E, Characteristic.PerceivedLightness),
    LightnessProlong(0x002F, Characteristic.PerceivedLightness),
    LightnessStandby(0x0030, Characteristic.PerceivedLightness),
    RegulatorAccuracy(0x0031, Characteristic.Percentage8),
    RegulatorKid(0x0032, Characteristic.Coefficient),
    RegulatorKiu(0x0033, Characteristic.Coefficient),
    RegulatorKpd(0x0034, Characteristic.Coefficient),
    RegulatorKpu(0x0035, Characteristic.Coefficient),
    TimeFade(0x0036, Characteristic.TimeMillisecond24),
    TimeFadeOn(0x0037, Characteristic.TimeMillisecond24),
    TimeFadeStandbyAuto(0x0038, Characteristic.TimeMillisecond24),
    TimeFadeStandbyManual(0x0039, Characteristic.TimeMillisecond24),
    TimeOccupancyDelay(0x003A, Characteristic.TimeMillisecond24),
    TimeProlong(0x003B, Characteristic.TimeMillisecond24),
    TimeRunOn(0x003C, Characteristic.TimeMillisecond24);

    enum class Characteristic(val min: Int? = null, val max: Int? = null, val decimalExponent: Int? = null) {
        Illuminance(0, 16777214, -2),
        PerceivedLightness(0, 65535, 0),
        Percentage8(0, 200, 0),
        Coefficient,
        TimeMillisecond24(0, 16777214, -3);
    }

    @Throws(LightLCPropertyValueRangeException::class)
    private fun checkRange(value: Int) {
        if (characteristic.min != null && characteristic.max != null) {
            if (value < characteristic.min || value > characteristic.max) {
                throw LightLCPropertyValueRangeException("Invalid range")
            }
        }
    }

    @Throws(LightLCPropertyValueRangeException::class)
    fun convertToByteArray(data: String): ByteArray {
        return when (this.characteristic) {
            Characteristic.Illuminance -> {
                val value = (data.toFloat() * 100).toInt()
                checkRange(value)
                Converters.convertIntToUint24(value)
            }
            Characteristic.PerceivedLightness -> {
                val value = data.toInt()
                checkRange(value)
                Converters.convertIntToUint16(value)
            }
            Characteristic.Percentage8 -> {
                val value = (data.toFloat() * 2).toInt()
                checkRange(value)
                Converters.convertIntToUint8(value)
            }
            Characteristic.Coefficient -> {
                val value = data.toFloat()
                Converters.convertFloatToByteArray(value)
            }
            Characteristic.TimeMillisecond24 -> {
                val value = (data.toFloat() * 1000).toInt()
                checkRange(value)
                Converters.convertIntToUint24(value)
            }
        }
    }

    fun convertToValue(data: ByteArray): String {
        return when (this.characteristic) {
            Characteristic.Illuminance -> {
                val value = Converters.convertUint24ToInt(data, 0) / 100f
                value.toString()
            }
            Characteristic.PerceivedLightness -> {
                Converters.convertUint16ToInt(data, 0).toString()
            }
            Characteristic.Percentage8 -> {
                val value = Converters.convertUint8ToInt(data, 0)
                if (value == 255) "(Not known)" else (value / 2f).toString()
            }
            Characteristic.Coefficient -> {
                Converters.convertByteArrayToFloat(data).toString()
            }
            Characteristic.TimeMillisecond24 -> {
                val value = Converters.convertUint24ToInt(data, 0) / 1000f
                value.toString()
            }
        }
    }

    class LightLCPropertyValueRangeException : Exception {
        constructor(message: String?) : super(message)
    }

}