/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.ModelView

import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlab.bluetoothmesh.adk.functionality_control.scheduler.response.SchedulerActionStatus
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality

data class MeshNode(val node: Node) {
    var onOffState = false
    var levelPercentage = 0
    var lightnessPercentage = 0
    var temperaturePercentage = 0
    var deltaUvPercentage = 0
    var functionality = DeviceFunctionality.FUNCTIONALITY.Unknown

    // Light LC
    var lcMode = false
    var lcOccupancyMode = false
    var lcOnOff = false
    var lcPropertyValue = "---"

    // Time
    var timeRole = "-"
    var taiSeconds: Long = 0
    var subsecond = 0
    var uncertainty = 0
    var timeAuthority = false
    var taiUtcDelta = 0
    var timeZoneOffset = 0

    // Scene
    var sceneNumber = 1
    var sceneOneStatus = SceneStatus.NOT_KNOWN
    var sceneTwoStatus = SceneStatus.NOT_KNOWN

    // Scheduler
    var schedules = BooleanArray(16)
    var scheduleRegister = mutableMapOf<Int, SchedulerActionStatus>()

    enum class SceneStatus {
        NOT_KNOWN,
        NOT_STORED,
        STORED,
        ACTIVE
    }
}