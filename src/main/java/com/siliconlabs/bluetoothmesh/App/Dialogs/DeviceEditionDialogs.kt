/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.DeviceFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListView
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.CustomAlertDialogBuilder
import com.siliconlabs.bluetoothmesh.R
import kotlinx.android.synthetic.main.dialog_loading.*

class DeviceEditionDialogs(val activityProvider: ActivityProvider, private val meshLogic: MeshLogic) {
    private var loadingDialog: AlertDialog? = null
    lateinit var deviceEditionDialogsListener: DeviceEditionDialogsListener

    interface ActivityProvider {
        fun getActivity(): Activity?
    }

    fun showDeleteDeviceDialog(deviceInfo: MeshNode) {
        activityProvider.getActivity()?.runOnUiThread {
            val builder = AlertDialog.Builder(activityProvider.getActivity(), R.style.AppTheme_Light_Dialog_Alert)
            builder.apply {

                if (deviceInfo.node.isConnectedAsProxy) {
                    setMessage(R.string.devices_dialog_delete_from_proxy_message)
                }

                setTitle(context.getString(R.string.devices_dialog_delete_title))

                setPositiveButton(context.getString(R.string.dialog_positive_delete)) { dialog, _ ->
                    deviceEditionDialogsListener.deleteDevice(deviceInfo)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.dialog_negative_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.apply {
                show()
            }
        }
    }

    fun showDeleteDeviceLocallyDialog(errorType: ErrorType, node: Node) {
        activityProvider.getActivity()?.runOnUiThread {
            val builder = AlertDialog.Builder(activityProvider.getActivity(), R.style.AppTheme_Light_Dialog_Alert)
            builder.apply {
                setTitle(R.string.device_dialog_delete_locally_title)

                setMessage(context.getString(R.string.device_dialog_delete_locally_message, ErrorMessageConverter.convert(activityProvider.getActivity()!!, errorType), node.name))

                setPositiveButton(context.getString(R.string.dialog_positive_delete)) { dialog, _ ->
                    deviceEditionDialogsListener.deleteDeviceLocally(node)
                    dialog.dismiss()
                }

                setNegativeButton(R.string.dialog_negative_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            builder.create().show()
        }
    }

    fun showDeviceConfigDialog(deviceInfo: MeshNode) {
        activityProvider.getActivity()?.runOnUiThread {
            meshLogic.deviceToConfigure = deviceInfo

            val mainActivity: MainActivity = activityProvider.getActivity() as MainActivity
            val deviceDialogFragment = DeviceFragment()
            mainActivity.showFragment(deviceDialogFragment, true, animated = true)
        }
    }

    fun showLoadingDialog() {
        activityProvider.getActivity()?.runOnUiThread {
            loadingDialog?.apply {
                if (isShowing) {
                    return@runOnUiThread
                }
            }

            val view: View = LayoutInflater.from(activityProvider.getActivity()).inflate(R.layout.dialog_loading, null)
            val builder = CustomAlertDialogBuilder(activityProvider.getActivity()!!, R.style.AppTheme_Light_Dialog_Alert_Wrap)
            builder.apply {
                setView(view)
                setCancelable(false)
                setPositiveButton(activityProvider.getActivity()!!.getString(R.string.dialog_positive_ok)) { _, _ ->
                }
            }

            loadingDialog = builder.create()
            loadingDialog?.apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()

                getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
            }
        }
    }

    fun dismissLoadingDialog() {
        activityProvider.getActivity()?.runOnUiThread {
            loadingDialog?.apply {
                dismiss()
                deviceEditionDialogsListener.dismiss()
            }
            loadingDialog = null
        }
    }

    fun updateLoadingDialogMessage(loadingMessage: DeviceListView.LOADING_DIALOG_MESSAGE, showCloseButton: Boolean = false) {
        val activity = activityProvider.getActivity()!!

        when (loadingMessage) {
            DeviceListView.LOADING_DIALOG_MESSAGE.CONFIG_DEVICE_DELETING -> updateLoadingDialogMessage(activity.getString(R.string.device_config_device_deleting), showCloseButton)
        }
    }

    fun updateLoadingDialogMessage(errorType: ErrorType) {
        updateLoadingDialogMessage(ErrorMessageConverter.convert(activityProvider.getActivity()!!, errorType), true)
    }

    private fun updateLoadingDialogMessage(message: String = "", showCloseButton: Boolean = false) {
        activityProvider.getActivity()?.runOnUiThread {
            loadingDialog?.apply {
                if (!isShowing) {
                    return@runOnUiThread
                }

                loading_text.text = message

                if (showCloseButton) {
                    loading_icon.visibility = View.GONE
                    getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.VISIBLE
                }
            }
        }
    }

    interface DeviceEditionDialogsListener {
        fun dismiss()
        fun deleteDevice(deviceInfo: MeshNode)
        fun deleteDeviceLocally(node: Node)
    }
}