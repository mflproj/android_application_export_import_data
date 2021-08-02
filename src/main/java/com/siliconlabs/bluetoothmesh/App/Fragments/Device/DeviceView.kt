/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device

import com.siliconlab.bluetoothmesh.adk.ErrorType

interface DeviceView {

    fun setActionBarTitle(title: String)

    fun showLoadingDialog()

    fun setLoadingDialogMessage(message: String, showCloseButton: Boolean = false, closeFragmentOnClick: Boolean = false)

    fun setLoadingDialogMessage(errorType: ErrorType?, showCloseButton: Boolean = false)

    fun setLoadingDialogMessage(loadingMessage: LOADING_DIALOG_MESSAGE, showCloseButton: Boolean = false, closeFragmentOnClick: Boolean = false)

    fun dismissLoadingDialog()

    enum class LOADING_DIALOG_MESSAGE {
        CONFIG_CONNECTING,
        CONFIG_DISCONNECTED,
    }
}