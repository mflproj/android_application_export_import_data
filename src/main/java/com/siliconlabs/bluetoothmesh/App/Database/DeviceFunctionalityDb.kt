/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Database

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.siliconlab.bluetoothmesh.adk.BuildConfig
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality

class DeviceFunctionalityDb(val context: Context) {
    private val FILE_NAME = "nodeFunctionality"
    private val FILE_NAME_V2 = "nodeFunctionality_v2"

    private val ADK_VERSION_KEY = "adkVersion"

    private val sharedPreferencesV1: SharedPreferences
    private val sharedPreferencesV2: SharedPreferences

    init {
        sharedPreferencesV1 = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPreferencesV2 = context.getSharedPreferences(FILE_NAME_V2, Context.MODE_PRIVATE)

        migrateIfNeeded()
    }

    fun getFunctionality(node: Node): DeviceFunctionality.FUNCTIONALITY {
        val funcName = sharedPreferencesV2.getString(node.uuid!!.contentToString(), DeviceFunctionality.FUNCTIONALITY.Unknown.name)
        val functionality = DeviceFunctionality.FUNCTIONALITY.values().find { it.name == funcName }
        return functionality ?: DeviceFunctionality.FUNCTIONALITY.Unknown
    }

    fun saveFunctionality(node: Node, func: DeviceFunctionality.FUNCTIONALITY) {
        val editor = sharedPreferencesV2.edit()
        editor.putString(node.uuid!!.contentToString(), func.name)
        editor.apply()
    }

    fun removeFunctionality(node: Node) {
        val editor = sharedPreferencesV2.edit()
        editor.remove(node.uuid!!.contentToString())
        editor.apply()
    }

    //migration

    private fun migrateIfNeeded() {
        val defaultSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferencesV1.all.isNotEmpty()) {
            val adkVersion = defaultSharedPreferences.getString(ADK_VERSION_KEY, "unknown")
            when (adkVersion) {
                "2.2.0.0" -> {
                    migrateToV2()
                }
                "unknown" -> {
                    fixFunctionalityOrders()
                    migrateToV2()
                }
            }
        }

        defaultSharedPreferences.edit().putString(ADK_VERSION_KEY, BuildConfig.ADK_VERSION).apply()
    }

    private fun functionalityV1ToFunctionalityV2Mapper(functionalityV1: DeviceFunctionality.FUNCTIONALITY): DeviceFunctionality.FUNCTIONALITY {
        return when (functionalityV1) {
            DeviceFunctionality.FUNCTIONALITY.OnOff -> DeviceFunctionality.FUNCTIONALITY.OnOff
            DeviceFunctionality.FUNCTIONALITY.OnOffClient -> DeviceFunctionality.FUNCTIONALITY.Level
            DeviceFunctionality.FUNCTIONALITY.Level -> DeviceFunctionality.FUNCTIONALITY.Lightness
            DeviceFunctionality.FUNCTIONALITY.LevelClient -> DeviceFunctionality.FUNCTIONALITY.CTL
            DeviceFunctionality.FUNCTIONALITY.Lightness -> DeviceFunctionality.FUNCTIONALITY.SensorServer
            DeviceFunctionality.FUNCTIONALITY.LightnessClient -> DeviceFunctionality.FUNCTIONALITY.SensorSetupServer
            DeviceFunctionality.FUNCTIONALITY.CTL -> DeviceFunctionality.FUNCTIONALITY.OnOffClient
            DeviceFunctionality.FUNCTIONALITY.CTLClient -> DeviceFunctionality.FUNCTIONALITY.LevelClient
            DeviceFunctionality.FUNCTIONALITY.SensorServer -> DeviceFunctionality.FUNCTIONALITY.LightnessClient
            DeviceFunctionality.FUNCTIONALITY.SensorSetupServer -> DeviceFunctionality.FUNCTIONALITY.CTLClient
            DeviceFunctionality.FUNCTIONALITY.SensorClient -> DeviceFunctionality.FUNCTIONALITY.SensorClient
            else -> DeviceFunctionality.FUNCTIONALITY.Unknown
        }
    }

    private fun fixFunctionalityOrders() {
        if (sharedPreferencesV1.all.isNotEmpty()) {
            val editorV1 = sharedPreferencesV1.edit()
            sharedPreferencesV1.all?.forEach {
                val value: Int = it.value as Int
                val functionalityV1 = DeviceFunctionality.FUNCTIONALITY.values()[value]
                val functionalityV2 = functionalityV1ToFunctionalityV2Mapper(functionalityV1)

                editorV1.putInt(it.key, functionalityV2.ordinal)
            }
            editorV1.commit()
        }
    }

    private fun migrateToV2() {
        if (sharedPreferencesV1.all.isNotEmpty()) {
            val editorV1 = sharedPreferencesV1.edit()
            val editorV2 = sharedPreferencesV2.edit()
            sharedPreferencesV1.all?.forEach {
                val value: Int = it.value as Int
                val functionalityV2 = DeviceFunctionality.FUNCTIONALITY.values()[value]
                editorV2.putString(it.key, functionalityV2.name)
                editorV1.remove(it.key)
            }
            editorV1.commit()
            editorV2.commit()
        }
    }
}