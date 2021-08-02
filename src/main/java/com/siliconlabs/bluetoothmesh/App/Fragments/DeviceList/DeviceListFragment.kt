/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.daimajia.swipe.util.Attributes
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Dialogs.DeviceEditionDialogs
import com.siliconlabs.bluetoothmesh.App.Dialogs.DeviceEditionDialogsPresenter
import com.siliconlabs.bluetoothmesh.App.Fragments.Network.NetworkFragment
import com.siliconlabs.bluetoothmesh.App.Fragments.Network.NetworkView
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.devices_screen.*
import javax.inject.Inject


class DeviceListFragment : DaggerFragment(), DeviceListView, DeviceEditionDialogs.ActivityProvider, DeviceEditionDialogsPresenter.ParentView {

    lateinit var deviceEditionDialogsPresenter: DeviceEditionDialogsPresenter
    lateinit var deviceEditionDialogs: DeviceEditionDialogs

    override fun showLoadingDialog() {
        deviceEditionDialogs.showLoadingDialog()
    }

    override fun dismissLoadingDialog() {
        deviceEditionDialogs.dismissLoadingDialog()
    }

    override fun updateLoadingDialogMessage(loadingMessage: DeviceListView.LOADING_DIALOG_MESSAGE, errorCode: String, showCloseButton: Boolean) {
        deviceEditionDialogs.updateLoadingDialogMessage(loadingMessage, showCloseButton)
    }

    override fun updateLoadingDialogMessage(loadingMessage: DeviceListView.LOADING_DIALOG_MESSAGE, errorType: ErrorType, showCloseButton: Boolean) {
        context?.let {
            updateLoadingDialogMessage(loadingMessage, ErrorMessageConverter.convert(it, errorType), showCloseButton)
        }
    }

    @Inject
    lateinit var deviceListPresenter: DeviceListPresenter

    private var deviceListAdapter: DeviceListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.devices_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceEditionDialogs = DeviceEditionDialogs(this, deviceListPresenter.meshLogic)
        deviceEditionDialogsPresenter = DeviceEditionDialogsPresenter(deviceEditionDialogs, this, deviceListPresenter.meshLogic, deviceListPresenter.meshNodeManager)
        deviceEditionDialogs.deviceEditionDialogsListener = deviceEditionDialogsPresenter

        setupDeviceList()
        showEmptyView()
    }

    override fun onResume() {
        super.onResume()
        deviceListPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        deviceListPresenter.onPause()
        deviceListAdapter?.closeAllItems()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            deviceListAdapter?.closeAllItems()
        }
    }

    // ParentView

    override fun refreshList() {
        activity?.runOnUiThread {
            deviceListPresenter.refreshList()
            (parentFragment as NetworkFragment).refreshFragment(NetworkView.FragmentName.GROUP_LIST)
        }
    }

    override fun returnToNetworkList() {
        val mainActivity = activity as MainActivity
        mainActivity.returnToNetworkListFragment()
    }

    override fun showToast(message: DeviceEditionDialogsPresenter.TOAST_MESSAGE) {
        activity?.runOnUiThread {
            val stringResource = when (message) {
                DeviceEditionDialogsPresenter.TOAST_MESSAGE.ERROR_DELETE_DEVICE -> R.string.device_adapter_remove_device_error
                DeviceEditionDialogsPresenter.TOAST_MESSAGE.ERROR_MISSING_GROUP -> R.string.device_adapter_missing_group
            }

            stringResource.let { MeshToast.show(requireContext(), it) }
        }
    }

    //

    private fun setupDeviceList() {
        deviceListAdapter = DeviceListAdapter(context!!, deviceListPresenter, deviceListPresenter.networkConnectionLogic)
        deviceListAdapter?.mode = Attributes.Mode.Single
        devices_list.adapter = deviceListAdapter
        devices_list.itemAnimator = null

        val dividerItemDecoration = DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.recycler_view_divider)!!)
        devices_list.addItemDecoration(dividerItemDecoration)
    }

    private fun showEmptyView() {
        if (deviceListAdapter?.itemCount ?: -1 > 0) {
            devices_list.visibility = View.VISIBLE
            ll_empty_view.visibility = View.GONE
        } else {
            devices_list.visibility = View.GONE
            ll_empty_view.visibility = View.VISIBLE
        }
    }

    override fun setDevicesList(newDevicesList: Set<MeshNode>) {
        activity?.runOnUiThread {
            deviceListAdapter?.setItems(newDevicesList.toMutableList())
            showEmptyView()
        }
    }

    override fun notifyDataSetChanged() {
        activity?.runOnUiThread {
            deviceListAdapter?.notifyDataSetChanged()
        }
    }

    override fun showDeleteDeviceDialog(meshNode: MeshNode) {
        deviceEditionDialogs.showDeleteDeviceDialog(meshNode)
        deviceListAdapter?.closeAllItems()
    }

    override fun showDeviceConfigDialog(meshNode: MeshNode) {
        deviceEditionDialogs.showDeviceConfigDialog(meshNode)
        deviceListAdapter?.closeAllItems()
    }

    override fun dismissDeviceConfigDialog() {
    }

    override fun showToast(message: String) {
        activity?.runOnUiThread { MeshToast.show(requireContext(), message) }
    }

    override fun showErrorToast(errorType: ErrorType) {
        activity?.let {
            this.showToast(ErrorMessageConverter.convert(it, errorType))
        }
    }

    override fun showToast(message: DeviceListView.TOAST_MESSAGE) {
        activity?.runOnUiThread {
            val stringResource = when (message) {
                DeviceListView.TOAST_MESSAGE.ERROR_DELETE_DEVICE -> R.string.device_adapter_remove_device_error
                DeviceListView.TOAST_MESSAGE.SUCCESS -> R.string.device_adapter_remove_device_success
            }

            MeshToast.show(requireContext(), stringResource)
        }
    }

}
