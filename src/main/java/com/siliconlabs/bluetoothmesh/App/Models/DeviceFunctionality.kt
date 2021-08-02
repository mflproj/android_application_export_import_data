/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Models

import com.siliconlab.bluetoothmesh.adk.data_model.model.ModelIdentifier
import com.siliconlab.bluetoothmesh.adk.data_model.model.SigModel
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node

class DeviceFunctionality {

    enum class FUNCTIONALITY(vararg val model: ModelIdentifier) {
        Unknown,
        //onoff
        OnOff(ModelIdentifier.GenericOnOffServer),
        OnOffClient(ModelIdentifier.GenericOnOffClient),
        //level
        Level(ModelIdentifier.GenericLevelServer),
        LevelClient(ModelIdentifier.GenericLevelClient),
        //lightness
        Lightness(ModelIdentifier.LightLightnessServer),
        LightnessClient(ModelIdentifier.LightLightnessClient),
        //ctl
        CTL(ModelIdentifier.LightCTLServer),
        CTLClient(ModelIdentifier.LightCTLClient),
        //sensor
        SensorServer(ModelIdentifier.SensorServer),
        SensorSetupServer(ModelIdentifier.SensorSetupServer),
        SensorClient(ModelIdentifier.SensorClient),
        //time
        TimeServer(ModelIdentifier.TimeServer),
        TimeClient(ModelIdentifier.TimeClient),
        //lc
        LightLCServer(ModelIdentifier.LightLCServer),
        LightLCClient(ModelIdentifier.LightLCClient),
        //scene
        SceneServer(ModelIdentifier.SceneServer),
        SceneClient(ModelIdentifier.SceneClient),
        //scheduler
        Scheduler(ModelIdentifier.SchedulerServer);

        private fun getAdditionalModels(): Set<ModelIdentifier> {
            val additionalModels: Set<ModelIdentifier> = when (this) {
                //onoff
                OnOff -> setOf(ModelIdentifier.LightLightnessServer)
                OnOffClient -> setOf(ModelIdentifier.LightLightnessClient)
                //level
                LevelClient -> setOf(ModelIdentifier.GenericOnOffClient)
                //lightness
                Lightness -> setOf(ModelIdentifier.GenericOnOffServer)
                LightnessClient -> setOf(ModelIdentifier.GenericOnOffClient)
                //ctl
                CTL -> setOf(ModelIdentifier.LightCTLTemperatureServer, ModelIdentifier.GenericOnOffServer, ModelIdentifier.LightLightnessServer)
                CTLClient -> setOf(ModelIdentifier.GenericOnOffClient, ModelIdentifier.LightLightnessClient)
                //lc
                LightLCServer -> setOf(ModelIdentifier.LightLCSetupServer)
                //scheduler
                Scheduler -> setOf(ModelIdentifier.SchedulerSetupServer, ModelIdentifier.TimeServer, ModelIdentifier.TimeSetupServer)
                //time
                TimeServer -> setOf(ModelIdentifier.TimeSetupServer)
                else -> emptySet()
            }

            return when (this) {
                Unknown -> {
                    additionalModels
                }
                else -> {
                    additionalModels.plus(setOf(ModelIdentifier.SceneServer,
                            ModelIdentifier.SceneSetupServer))
                }
            }
        }

        fun getAllModels(): Set<ModelIdentifier> {
            val models = mutableSetOf<ModelIdentifier>()
            models.addAll(model)
            models.addAll(getAdditionalModels())

            return models
        }

        companion object {
            fun fromId(id: Int): FUNCTIONALITY? {
                return ModelIdentifier.values().find { it.id == id }?.let { modelIdentifier ->
                    values().find { it.model.contains(modelIdentifier) }
                }
            }
        }
    }

    data class FunctionalityNamed(val functionality: FUNCTIONALITY, val functionalityName: String)

    companion object {

        fun getFunctionalitiesNamed(node: Node): Set<FunctionalityNamed> {
            return mutableSetOf(
                    FunctionalityNamed(FUNCTIONALITY.Unknown, "")
            ).apply {
                addAll(node.elements?.flatMap { it.sigModels }
                        ?.mapNotNull { sigModel ->
                            FUNCTIONALITY.fromId(sigModel.id)?.let {
                                FunctionalityNamed(it, sigModel.name)
                            }
                        } ?: emptySet())
            }
        }

        fun getSigModels(node: Node, functionality: FUNCTIONALITY): Set<SigModel> {
            val supportedModelIds = functionality.getAllModels()

            return node.elements?.flatMap { it.sigModels }
                    ?.filter { sigModel -> supportedModelIds.any { it.id == sigModel.id } }
                    ?.toSet() ?: emptySet()
        }
    }
}