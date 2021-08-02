/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.DeviceConfig
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality
import com.siliconlabs.bluetoothmesh.App.Utils.ErrorMessageConverter
import com.siliconlabs.bluetoothmesh.App.Views.CustomAlertDialogBuilder
import com.siliconlabs.bluetoothmesh.App.Views.MeshToast
import com.siliconlabs.bluetoothmesh.App.Views.makeVisibleIf
import com.siliconlabs.bluetoothmesh.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.device_config_screen.*
import kotlinx.android.synthetic.main.dialog_loading.*
import javax.inject.Inject

class DeviceConfigFragment : DaggerFragment(), DeviceConfigView {
    @Inject
    lateinit var deviceConfigPresenter: DeviceConfigPresenter

    private var loadingDialog: AlertDialog? = null
    private var choosenFriendNode: Node? = null

    private val nodeNameTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            deviceConfigPresenter.changeName(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.device_config_screen, container, false)
    }

    override fun onResume() {
        super.onResume()
        deviceConfigPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        deviceConfigPresenter.onPause()
    }

    override fun setDeviceConfig(deviceInfo: MeshNode, deviceConfig: DeviceConfig, groupsInSubnet: List<Group>, nodes: List<Node>) {
        activity?.runOnUiThread {
            deviceInfo.apply {
                changeSectionsVisibility()

                setupDcdWarningSection()
                setupNameSection()
                setupFeaturesSections(deviceConfig)
                setupLpnSections(nodes, deviceConfig)
                setupGroupSection(groupsInSubnet)
                setupFunctionalitiesSection()
            }
        }
    }

    private fun MeshNode.changeSectionsVisibility() {
        ll_dcd_warning.makeVisibleIf(node.deviceCompositionData == null)
        node.deviceCompositionData?.apply {
            ll_proxy.makeVisibleIf(supportsProxy())
            ll_relay.makeVisibleIf(supportsRelay())
            ll_friend.makeVisibleIf(supportsFriend())
            ll_retransmission.visibility = View.VISIBLE
            ll_low_power.makeVisibleIf(supportsLowPower())
        }
    }

    private fun setupDcdWarningSection() {
        btn_dcd_info.setOnClickListener { showDcdInfoDialog() }
        btn_dcd_refresh.setOnClickListener { deviceConfigPresenter.updateCompositionData() }
    }

    private fun showDcdInfoDialog() {
        AlertDialog.Builder(activity, R.style.AppTheme_Light_Dialog_Alert_Wrap)
                .setTitle(getString(R.string.device_config_dcd_info_title))
                .setMessage(getString(R.string.device_config_dcd_info_content))
                .setPositiveButton(R.string.dialog_positive_ok) { dialog, _ ->
                    dialog.dismiss()
                }.create()
                .show()
    }

    private fun MeshNode.setupNameSection() {
        et_device_name.removeTextChangedListener(nodeNameTextWatcher)
        et_device_name.setText(node.name)
        et_device_name.addTextChangedListener(nodeNameTextWatcher)
    }

    private fun setupFeaturesSections(deviceConfig: DeviceConfig) {
        setFeaturesOnClickListeners()
        disableFeaturesOnCheckedChangeListeners()
        fillFeaturesData(deviceConfig)
        setFeaturesOnCheckedChangeListeners()
    }

    private fun setFeaturesOnClickListeners() {
        btn_get_proxy.setOnClickListener { deviceConfigPresenter.updateProxy() }
        btn_get_relay.setOnClickListener { deviceConfigPresenter.updateRelay() }
        btn_get_friend.setOnClickListener { deviceConfigPresenter.updateFriend() }
        btn_get_retransmission.setOnClickListener { deviceConfigPresenter.updateRetransmission() }
    }

    private fun disableFeaturesOnCheckedChangeListeners() {
        sw_proxy.setOnCheckedChangeListener(null)
        sw_relay.setOnCheckedChangeListener(null)
        sw_friend.setOnCheckedChangeListener(null)
        sw_retransmission.setOnCheckedChangeListener(null)
    }

    private fun fillFeaturesData(deviceConfig: DeviceConfig) {
        deviceConfig.proxy?.let {
            sw_proxy.isEnabled = true
            sw_proxy.isChecked = it
        }
        deviceConfig.relay?.let {
            sw_relay.isEnabled = true
            sw_relay.isChecked = it
        }
        deviceConfig.friend?.let {
            sw_friend.isEnabled = true
            sw_friend.isChecked = it
        }
        deviceConfig.retransmission?.let {
            sw_retransmission.isEnabled = true
            sw_retransmission.isChecked = it
        }
    }

    private fun setFeaturesOnCheckedChangeListeners() {
        sw_proxy.setOnCheckedChangeListener { _, isChecked ->
            deviceConfigPresenter.processChangeProxy(isChecked)
        }
        sw_relay.setOnCheckedChangeListener { _, isChecked ->
            deviceConfigPresenter.changeRelay(isChecked)
        }
        sw_friend.setOnCheckedChangeListener { _, isChecked ->
            deviceConfigPresenter.changeFriend(isChecked)
        }
        sw_retransmission.setOnCheckedChangeListener { _, isChecked ->
            deviceConfigPresenter.changeRetransmission(isChecked)
        }
    }

    private fun setupLpnSections(nodes: List<Node>, deviceConfig: DeviceConfig) {
        sp_nodes.adapter = NodesAdapter(requireContext(), nodes)
        btn_get_poll_timeout.setOnClickListener {
            sp_nodes.selectedItem?.let { deviceConfigPresenter.updatePollTimeout(it as Node) }
                    ?: MeshToast.show(requireContext(), getString(R.string.device_dialog_friend_null))
        }
        deviceConfig.pollTimeout?.let {
            fillPollTimeout(deviceConfig)
            sp_nodes.setSelection(nodes.indexOf(choosenFriendNode).coerceAtLeast(0))
        }
        sp_nodes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                choosenFriendNode = nodes[position]
                fillPollTimeout(deviceConfig)
            }
        }

        btn_set_global_timeout.setOnClickListener {
            deviceConfigPresenter.changeLpnGlobalTimeout(et_global_timeout_secs.text.toString())
        }
        deviceConfig.lpnGlobalTimeout?.let {
            tv_lpn_global_timeout_secs.text = String.format(getString(R.string.device_dialog_lpn_value_label), it)
        }
    }

    private fun fillPollTimeout(deviceConfig: DeviceConfig) {
        tv_poll_timeout.text =
                if (choosenFriendNode == deviceConfig.pollTimeoutFriend) {
                    String.format(getString(R.string.device_dialog_poll_timeout_value), deviceConfig.pollTimeout)
                } else {
                    getString(R.string.device_dialog_poll_timeout_value_unknown)
                }
    }

    private fun MeshNode.setupGroupSection(groupsInSubnet: List<Group>) {
        val groupList = mutableListOf("").apply {
            addAll(groupsInSubnet.map { it.name })
        }
        val groupAdapter = ArrayAdapter<String>(context!!, R.layout.spinner_item_dark, groupList)

        sp_group.onItemSelectedListener = null
        sp_group.adapter = groupAdapter
        if (node.groups.isNotEmpty()) {
            val nodeGroup = node.groups.first()
            groupsInSubnet.find { it == nodeGroup }
                    ?.let { sp_group.setSelection(groupList.indexOf(it.name), false) }
        } else {
            sp_group.setSelection(Adapter.NO_SELECTION, false)
        }
        sp_group.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    deviceConfigPresenter.processChangeGroup(null)
                } else {
                    deviceConfigPresenter.processChangeGroup(groupsInSubnet[position - 1])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun MeshNode.setupFunctionalitiesSection() {
        val namedFunctionalities = DeviceFunctionality.getFunctionalitiesNamed(node).toMutableList()
                .sortedBy { it.functionalityName }
        val funtionalitiesNames = namedFunctionalities.map { it.functionalityName }
        val functionalitiesAdapter = ArrayAdapter<String>(context!!, R.layout.spinner_item_dark, funtionalitiesNames)

        sp_functionality.onItemSelectedListener = null
        sp_functionality.adapter = functionalitiesAdapter
        if (functionality != DeviceFunctionality.FUNCTIONALITY.Unknown) {
            namedFunctionalities.indexOfFirst { it.functionality == functionality }
                    .takeUnless { it == -1 }
                    ?.let { sp_functionality.setSelection(it, false) }
        } else {
            sp_functionality.setSelection(Adapter.NO_SELECTION, false)
        }
        sp_functionality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                deviceConfigPresenter.processChangeFunctionality(namedFunctionalities[position].functionality)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun showToast(message: DeviceConfigView.ToastMessage) {
        activity?.runOnUiThread {
            val stringResource = when (message) {
                DeviceConfigView.ToastMessage.ERROR_MISSING_GROUP -> getString(R.string.device_config_select_group_first)
                DeviceConfigView.ToastMessage.POLL_TIMEOUT_UPDATED -> getString(R.string.device_config_poll_timeout_updated)
                DeviceConfigView.ToastMessage.POLL_TIMEOUT_NOT_FRIEND -> getString(R.string.device_config_poll_timeout_not_friend, (sp_nodes.selectedItem as Node).name)
                DeviceConfigView.ToastMessage.LPN_TIMEOUT_WRONG_RANGE -> getString(R.string.device_config_lpn_timeout_wrong_range)
            }
            MeshToast.show(requireContext(), stringResource)
        }
    }

    override fun showLoadingDialog() {
        activity?.runOnUiThread {
            if (loadingDialog?.isShowing == true) {
                return@runOnUiThread
            }

            val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)
            val builder = CustomAlertDialogBuilder(activity!!, R.style.AppTheme_Light_Dialog_Alert_Wrap).apply {
                setView(view)
                setCancelable(false)
                setPositiveButton(activity!!.getString(R.string.dialog_positive_ok)) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            loadingDialog = builder.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
            }
        }
    }

    override fun dismissLoadingDialog() {
        activity?.runOnUiThread {
            loadingDialog?.dismiss()
            loadingDialog = null
        }
    }

    override fun setLoadingDialogMessage(message: String, showCloseButton: Boolean) {
        activity?.runOnUiThread {
            loadingDialog?.takeIf { it.isShowing }?.apply {
                loading_text.text = message
                if (showCloseButton) {
                    loading_icon.visibility = View.GONE
                    setupCloseButton()
                }
            }
        }
    }

    private fun AlertDialog.setupCloseButton() {
        getButton(AlertDialog.BUTTON_POSITIVE).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                deviceConfigPresenter.abandonTasks()
                dismiss()
            }
        }
    }

    override fun showRetryButton() {
        activity?.runOnUiThread {
            loadingDialog?.takeIf { it.isShowing }?.apply {
                getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.dialog_retry)
                    setOnClickListener {
                        deviceConfigPresenter.retryTask()
                    }
                }
            }
        }
    }

    override fun setLoadingDialogMessage(errorType: ErrorType?, showCloseButton: Boolean) {
        activity?.let {
            setLoadingDialogMessage(ErrorMessageConverter.convert(it, errorType!!), showCloseButton)
        }
    }

    override fun setLoadingDialogMessage(loadingMessage: DeviceConfigView.LoadingDialogMessage, message: String) {
        setLoadingDialogMessage(getMessageResource(loadingMessage, message))
    }

    override fun setLoadingDialogMessage(loadingMessage: DeviceConfigView.LoadingDialogMessage, message: String, leftTasksCount: Int, allTasksCount: Int) {
        val doneTasksCount = allTasksCount - leftTasksCount
        val stepsCount = "$doneTasksCount/$allTasksCount\n"
        setLoadingDialogMessage(stepsCount.plus(getMessageResource(loadingMessage, message)))
    }

    private fun getMessageResource(loadingMessage: DeviceConfigView.LoadingDialogMessage, message: String): String {
        return activity?.let {
            when (loadingMessage) {
                DeviceConfigView.LoadingDialogMessage.CONFIG_ADDING_TO_GROUP -> it.getString(R.string.device_config_adding_to_group).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_REMOVING_FROM_GROUP -> it.getString(R.string.device_config_removing_from_group).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_PROXY_ENABLING -> it.getString(R.string.device_config_proxy_enabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_PROXY_DISABLING -> it.getString(R.string.device_config_proxy_disabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_PROXY_GETTING -> it.getString(R.string.device_config_proxy_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_MODEL_ADDING -> it.getString(R.string.device_config_model_adding).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_MODEL_REMOVING -> it.getString(R.string.device_config_model_removing).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_SUBSCRIPTION_ADDING -> it.getString(R.string.device_config_subscription_adding).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_SUBSCRIPTION_REMOVING -> it.getString(R.string.device_config_subscription_removing).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_PUBLICATION_SETTING -> it.getString(R.string.device_config_publication_setting).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_PUBLICATION_CLEARING -> it.getString(R.string.device_config_publication_clearing).format(message)
                DeviceConfigView.LoadingDialogMessage.CONFIG_FUNCTIONALITY_CHANGING -> it.getString(R.string.device_config_functionality_changing)
                DeviceConfigView.LoadingDialogMessage.CONFIG_FRIEND_ENABLING -> it.getString(R.string.device_config_friend_enabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_FRIEND_DISABLING -> it.getString(R.string.device_config_friend_disabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_FRIEND_GETTING -> it.getString(R.string.device_config_friend_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RETRANSMISSION_ENABLING -> it.getString(R.string.device_config_retransmission_enabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RETRANSMISSION_DISABLING -> it.getString(R.string.device_config_retransmission_disabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RETRANSMISSION_GETTING -> it.getString(R.string.device_config_retransmission_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RELAY_ENABLING -> it.getString(R.string.device_config_relay_enabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RELAY_DISABLING -> it.getString(R.string.device_config_relay_disabling)
                DeviceConfigView.LoadingDialogMessage.CONFIG_RELAY_GETTING -> it.getString(R.string.device_config_relay_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_POLL_TIMEOUT_GETTING -> it.getString(R.string.device_config_poll_timeout_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_LPN_TIMEOUT_SETTING -> it.getString(R.string.device_config_lpn_timeout_setting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_LPN_TIMEOUT_GETTING -> it.getString(R.string.device_config_lpn_timeout_getting)
                DeviceConfigView.LoadingDialogMessage.CONFIG_DCD_GETTING -> getString(R.string.device_config_dcd_getting)
            }
        } ?: ""
    }

    override fun showDisableProxyAttentionDialog(onClickListener: DialogInterface.OnClickListener) {
        val builder = CustomAlertDialogBuilder(activity!!, R.style.AppTheme_Light_Dialog_Alert)
        builder.apply {
            setTitle(getString(R.string.device_config_proxy_disable_attention_title))
            setMessage(getString(R.string.device_config_proxy_disable_attention_message))
            setPositiveButton(getString(R.string.dialog_positive_ok)) { dialog, _ ->
                onClickListener.onClick(dialog, AlertDialog.BUTTON_POSITIVE)
            }
            setCancelable(false)
            setNegativeButton(getString(R.string.dialog_negative_cancel)) { dialog, _ ->
                onClickListener.onClick(dialog, AlertDialog.BUTTON_NEGATIVE)
            }
        }

        val dialog = builder.create()
        dialog.apply {
            show()
        }
    }

    override fun promptGlobalTimeout(timeout: Int) {
        et_global_timeout_secs.setText(timeout.toString())
    }
}
