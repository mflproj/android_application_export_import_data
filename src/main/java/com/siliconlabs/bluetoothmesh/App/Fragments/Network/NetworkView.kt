/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Network

import com.siliconlab.bluetoothmesh.adk.ErrorType

interface NetworkView {

    fun showToast(toastMessage: ToastMessage)

    fun showErrorToast(errorType: ErrorType)

    fun setMeshIconState(iconState: MeshIconState)

    fun setActionBarTitle(title: String)

    fun showFragment(fragmentName: FragmentName)

    enum class FragmentName {
        GROUP_LIST,
        DEVICE_LIST
    }

    enum class MeshIconState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    enum class ToastMessage {
        NO_NODE_IN_NETWORK,
        GATT_NOT_CONNECTED,
        GATT_PROXY_DISCONNECTED,
        GATT_ERROR_DISCOVERING_SERVICES,
        PROXY_SERVICE_NOT_FOUND,
        PROXY_CHARACTERISTIC_NOT_FOUND,
        PROXY_DESCRIPTOR_NOT_FOUND,
    }
}