/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet

interface NetworkListView {
    fun showDeleteNetworkDialog(networkInfo: Subnet)

    fun showDeleteNetworkLocallyDialog(subnet: Subnet, errorType: ErrorType)

    fun showEditNetworkDialog(networkInfo: Subnet)

    fun showAddNetworkDialog()

    fun showToast(message: TOAST_MESSAGE)

    fun setNetworkList(networkList: Set<Subnet>)

    fun showLoadingDialog()

    fun updateLoadingDialogMessage(loadingMessage: LOADING_DIALOG_MESSAGE, message: String = "", showCloseButton: Boolean = false)

    fun updateLoadingDialogMessage(loadingMessage: LOADING_DIALOG_MESSAGE, errorType: ErrorType, showCloseButton: Boolean = false)

    fun dismissLoadingDialog()

    fun showNetworkFragment()

    fun showImportNetworkDialogErrorMessage(subnet: Subnet, msg: String)

    fun showRetrieveNetworkDataDialog()

    fun showNetworkOptionsDialog()

    fun showImportDataDialog(subnet: Subnet)

    enum class LOADING_DIALOG_MESSAGE {
        CONNECTING_TO_NETWORK,
        CONNECTING_TO_NETWORK_ERROR,
        REMOVING_NETWORK,
        REMOVING_NETWORK_ERROR,
        EXPORTING_AND_IMPORTING_NETWORK_DATA
    }

    enum class TOAST_MESSAGE {
        SUCCESS_DELETE,
        SUCCESS_UPDATE,
        SUCCESS_ADD,
        ERROR_CREATING_NETWORK
    }

}