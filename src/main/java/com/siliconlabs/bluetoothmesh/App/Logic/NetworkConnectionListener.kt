/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Logic

import com.siliconlab.bluetoothmesh.adk.ErrorType

interface NetworkConnectionListener {
    fun connecting()

    fun connected()

    fun disconnected()

    fun initialConfigurationLoaded()

    fun connectionMessage(messageType: MessageType)

    fun connectionErrorMessage(error: ErrorType)

    enum class MessageType {
        NO_NODE_IN_NETWORK,

        GATT_NOT_CONNECTED,
        GATT_PROXY_DISCONNECTED,
        GATT_ERROR_DISCOVERING_SERVICES,

        PROXY_SERVICE_NOT_FOUND,
        PROXY_CHARACTERISTIC_NOT_FOUND,
        PROXY_DESCRIPTOR_NOT_FOUND,
    }
}