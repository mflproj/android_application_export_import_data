/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.ControlGroup

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.daimajia.swipe.util.Attributes
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlabs.bluetoothmesh.App.Activities.Main.MainActivity
import com.siliconlabs.bluetoothmesh.App.Dialogs.DeviceEditionDialogs
import com.siliconlabs.bluetoothmesh.App.Dialogs.DeviceEditionDialogsPresenter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListView
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.control_group.*
import javax.inject.Inject

class ControlGroupFragment : DaggerFragment(), ControlGroupView, DeviceEditionDialogs.ActivityProvider, DeviceEditionDialogsPresenter.ParentView {
    private val TAG: String = javaClass.canonicalName!!

    override fun refreshList() {
        activity?.runOnUiThread {
            controlGroupPresenter.refreshList()
        }
    }

    override fun returnToNetworkList() {
        val mainActivity = activity as MainActivity
        mainActivity.returnToNetworkListFragment()
    }

    @Inject
    lateinit var controlGroupPresenter: ControlGroupPresenter

    private var deviceListAdapter: DeviceListAdapter? = null

    private lateinit var rotate: Animation
    private var meshStatusBtn: ImageView? = null
    private var meshIconStatus = ControlGroupView.MESH_ICON_STATE.DISCONNECTED
    lateinit var deviceEditionDialogsPresenter: DeviceEditionDialogsPresenter
    lateinit var deviceEditionDialogs: DeviceEditionDialogs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.control_group, container, false)
    }

    var switchEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceEditionDialogs = DeviceEditionDialogs(this, controlGroupPresenter.meshLogic)
        deviceEditionDialogsPresenter = DeviceEditionDialogsPresenter(deviceEditionDialogs, this, controlGroupPresenter.meshLogic, controlGroupPresenter.meshNodeManager)
        deviceEditionDialogs.deviceEditionDialogsListener = deviceEditionDialogsPresenter
        setHasOptionsMenu(true)

        iv_switch.setOnClickListener {
            switchEnabled = !switchEnabled
            controlGroupPresenter.onMasterSwitchChanged(switchEnabled)
        }

        sb_light_control.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    controlGroupPresenter.onMasterLevelChanged(it.progress)
                }
            }
        })

        setupDeviceList()
        showEmptyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_control_group_toolbar, menu)

        val menuIcon = menu.findItem(R.id.proxy_menu)

        meshStatusBtn?.clearAnimation()
        meshStatusBtn?.visibility = View.INVISIBLE
        meshStatusBtn?.setOnClickListener(null)

        meshStatusBtn = menuIcon?.actionView as ImageView

        setMeshIconState(meshIconStatus)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBar(controlGroupPresenter.groupInfo.name)
        controlGroupPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        controlGroupPresenter.onPause()
        meshStatusBtn?.clearAnimation()
        deviceListAdapter?.closeAllItems()
    }

    //

    private fun setupDeviceList() {
        deviceListAdapter = DeviceListAdapter(context!!, controlGroupPresenter, controlGroupPresenter.networkConnectionLogic)
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            deviceListAdapter?.closeAllItems()
        }
    }

    override fun setMeshIconState(iconState: ControlGroupView.MESH_ICON_STATE) {
        meshIconStatus = iconState

        meshStatusBtn?.apply {
            when (iconState) {
                ControlGroupView.MESH_ICON_STATE.DISCONNECTED -> {
                    setImageResource(R.drawable.ic_mesh_red)
                    clearAnimation()
                }
                ControlGroupView.MESH_ICON_STATE.CONNECTING -> {
                    setImageResource(R.drawable.ic_mesh_yellow)
                    startAnimation(rotate)
                }
                ControlGroupView.MESH_ICON_STATE.CONNECTED -> {
                    setImageResource(R.drawable.ic_mesh_green)
                    clearAnimation()
                }
            }

            setOnClickListener {
                controlGroupPresenter.meshIconClicked(iconState)
            }
        }
    }

    override fun setMasterSwitch(isChecked: Boolean) {
        if (isChecked) {
            iv_switch.setImageResource(R.drawable.toggle_on)
        } else {
            iv_switch.setImageResource(R.drawable.toggle_off)
        }
    }

    override fun showToast(message: String) {
        MeshToast.show(requireContext(), message)
    }

    override fun showToast(errorType: ErrorType) {
        showToast(ErrorMessageConverter.convert(requireContext(), errorType))
    }

    override fun setMasterLevel(progress: Int) {
        sb_light_control.progress = progress
        tv_light_value.text = context!!.getString(R.string.device_adapter_lightness_value).format(progress)
    }


    override fun setMasterControlEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.5f

        iv_switch.isEnabled = enabled
        iv_switch.alpha = alpha
        sb_light_control.isEnabled = enabled
        sb_light_control.alpha = alpha
    }

    override fun setMasterControlVisibility(visibility: Int) {
        ll_master_control.visibility = visibility
    }

    override fun refreshView() {
        deviceListAdapter?.notifyDataSetChanged()
    }

    override fun setDevicesList(devicesInfo: Set<MeshNode>) {
        deviceListAdapter?.setItems(devicesInfo.toMutableList())
        deviceListAdapter?.notifyDataSetChanged()
        showEmptyView()
    }

    override fun notifyItemChanged(deviceInfo: MeshNode) {
        deviceListAdapter?.notifyDataSetChanged()
    }

    override fun showToast(message: DeviceEditionDialogsPresenter.TOAST_MESSAGE) {
        activity?.runOnUiThread {
            val stringResource = when (message) {
                DeviceEditionDialogsPresenter.TOAST_MESSAGE.ERROR_DELETE_DEVICE -> R.string.device_adapter_remove_device_error
                DeviceEditionDialogsPresenter.TOAST_MESSAGE.ERROR_MISSING_GROUP -> R.string.control_group_missing_group
            }
            MeshToast.show(requireContext(), stringResource)
        }
    }

    override fun showToast(message: ControlGroupView.TOAST_MESSAGE) {
        val stringResource = when (message) {
            ControlGroupView.TOAST_MESSAGE.NOT_CONNECTED_TO_MESH_NETWORK -> R.string.control_group_not_connected_to_mesh_network
            ControlGroupView.TOAST_MESSAGE.SUCCESS -> R.string.control_group_success
            ControlGroupView.TOAST_MESSAGE.TO_DO -> R.string.control_group_to_do
        }
        MeshToast.show(requireContext(), stringResource)
    }


    override fun showDeleteDeviceDialog(deviceInfo: MeshNode) {
        deviceEditionDialogs.showDeleteDeviceDialog(deviceInfo)
        deviceListAdapter?.closeAllItems()
    }

    override fun showDeviceConfigDialog(deviceInfo: MeshNode) {
        deviceEditionDialogs.showDeviceConfigDialog(deviceInfo)
        deviceListAdapter?.closeAllItems()
    }

    override fun showLoadingDialog() {
        deviceEditionDialogs.showLoadingDialog()
    }

    override fun updateLoadingDialogMessage(loadingMessage: DeviceListView.LOADING_DIALOG_MESSAGE, errorCode: String, showCloseButton: Boolean) {
        deviceEditionDialogs.updateLoadingDialogMessage(loadingMessage, showCloseButton)
    }

}