/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config

import android.app.AlertDialog
import android.content.DialogInterface
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.configuration_control.*
import com.siliconlab.bluetoothmesh.adk.data_model.dcd.DeviceCompositionData
import com.siliconlab.bluetoothmesh.adk.data_model.element.Element
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlab.bluetoothmesh.adk.data_model.model.Model
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlab.bluetoothmesh.adk.data_model.node.NodeChangeNameException
import com.siliconlab.bluetoothmesh.adk.functionality_binder.FunctionalityBinder
import com.siliconlab.bluetoothmesh.adk.functionality_binder.FunctionalityBinderCallback
import com.siliconlab.bluetoothmesh.adk.node_control.NodeControl
import com.siliconlab.bluetoothmesh.adk.node_control.NodeControlCallback
import com.siliconlab.bluetoothmesh.adk.notification_control.PublicationSettingsCallback
import com.siliconlab.bluetoothmesh.adk.notification_control.SubscriptionControl
import com.siliconlab.bluetoothmesh.adk.notification_control.SubscriptionSettingsCallback
import com.siliconlab.bluetoothmesh.adk.notification_control.settings.PublicationSettings
import com.siliconlab.bluetoothmesh.adk.notification_control.settings.PublishPeriod
import com.siliconlab.bluetoothmesh.adk.notification_control.settings.SubscriptionSettings
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Fragments.Device.Config.DeviceConfigView.LoadingDialogMessage
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.Models.DeviceConfig
import com.siliconlabs.bluetoothmesh.App.Models.DeviceFunctionality
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager
import com.siliconlabs.bluetoothmesh.App.Utils.Converters
import java.util.concurrent.Executors

class DeviceConfigPresenter(private val deviceConfigView: DeviceConfigView, meshLogic: MeshLogic, val networkConnectionLogic: NetworkConnectionLogic, private val meshNodeManager: MeshNodeManager) : BasePresenter, NetworkConnectionListener {
    private val meshNode = meshLogic.deviceToConfigure!!
    private val configurationControl: ConfigurationControl = ConfigurationControl(meshNode.node)
    private val nodeControl: NodeControl = NodeControl(meshNode.node)

    private val taskExecutor = Executors.newSingleThreadScheduledExecutor()
    private val taskList = mutableListOf<Runnable>()
    private var allTasksCount = 0
    private var currentTask = Runnable { }

    private var getDeviceConfigRequested = false

    //data
    private var isProxy: Boolean? = null
    private var isRelay: Boolean? = null
    private var isFriend: Boolean? = null
    private var isRetransmission: Boolean? = null
    private var supportFriend: Boolean? = null
    private var supportLowPower: Boolean? = null
    private var pollTimeoutFriend: Node? = null
    private var pollTimeout: Int? = null
    private var lpnGlobalTimeout: Int? = null

    private val retransmissionCount = 3
    private val retransmissionInterval = 20

    override fun onResume() {
        networkConnectionLogic.addListener(this)
    }

    override fun onPause() {
        networkConnectionLogic.removeListener(this)
    }

    private fun getDeviceConfig() {
        meshNode.node.deviceCompositionData?.apply {
            supportFriend = supportsFriend()
            supportLowPower = supportsLowPower()
        }

        updateLpnGlobalTimeout()
    }

    private fun refreshView() {
        val currentConfig = DeviceConfig(
                meshNode.node.name,
                isProxy,
                isRelay,
                isFriend,
                isRetransmission,
                supportLowPower,
                meshNode.functionality,
                pollTimeoutFriend,
                pollTimeout,
                lpnGlobalTimeout)

        val groupsInSubnet = meshNode.node.subnets.first().groups.sortedBy { it.name }
        val nodes = meshNode.node.subnets.first().nodes.filter {
            it.deviceCompositionData?.supportsFriend() ?: true
        }
        deviceConfigView.setDeviceConfig(meshNode, currentConfig, groupsInSubnet, nodes)
    }

    private fun startTasks() {
        allTasksCount = taskList.size
        deviceConfigView.showLoadingDialog()
        takeNextTask()
    }

    fun takeNextTask() {
        if (taskList.isNotEmpty()) {
            currentTask = taskList.first()
            taskList.remove(currentTask)
            taskExecutor.execute(currentTask)
        } else {
            refreshView()
            deviceConfigView.dismissLoadingDialog()
        }
    }

    fun retryTask() {
        deviceConfigView.dismissLoadingDialog()
        deviceConfigView.showLoadingDialog()
        taskList.add(0, currentTask)
        takeNextTask()
    }

    fun abandonTasks() {
        taskList.clear()
    }

    fun showErrorMessage(error: ErrorType?) {
        refreshView()
        deviceConfigView.setLoadingDialogMessage(error, showCloseButton = true)
    }

    fun showErrorMessageWithRetryButton(error: ErrorType?) {
        refreshView()
        deviceConfigView.setLoadingDialogMessage(error, showCloseButton = true)
        deviceConfigView.showRetryButton()
    }

    // NetworkConnectionListener

    override fun connecting() {
        // nothing to do
    }

    override fun connected() {
        if (!getDeviceConfigRequested) {
            getDeviceConfig()
            getDeviceConfigRequested = true
        }
    }

    override fun disconnected() {
        // nothing to do
    }

    override fun initialConfigurationLoaded() {
        getDeviceConfig()
    }

    override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
        // nothing to do
    }

    override fun connectionErrorMessage(error: ErrorType) {
        // nothing to do
    }

    private fun isConnectedToProxyDevice(): Boolean {
        val currentlyConnectedNode = networkConnectionLogic.getCurrentlyConnectedNode()
        currentlyConnectedNode?.apply {
            return currentlyConnectedNode == meshNode.node
        }
        return false
    }

    // edit device

    fun processChangeGroup(newGroup: Group?) {
        if (meshNode.node.groups.isNotEmpty()) {
            val oldGroup = meshNode.node.groups.first()
            taskList.addAll(processUnsubscribeModelFromGroup(oldGroup, meshNode.functionality))
            taskList.add(unbindNodeFromGroup(oldGroup))
        }
        taskList.add(changeFunctionality(DeviceFunctionality.FUNCTIONALITY.Unknown))
        if (newGroup != null) {
            taskList.add(bindNodeToGroup(newGroup))
        }

        startTasks()
    }

    fun processChangeFunctionality(newFunctionality: DeviceFunctionality.FUNCTIONALITY) {
        if (meshNode.node.groups.isEmpty()) {
            meshNodeManager.removeNodeFunc(meshNode)
            deviceConfigView.showToast(DeviceConfigView.ToastMessage.ERROR_MISSING_GROUP)
            refreshView()
            return
        }
        val group = meshNode.node.groups.first()
        group?.let {
            taskList.addAll(processUnbindModelFromGroup(it, meshNode.functionality))
            taskList.addAll(processBindModelToGroup(it, newFunctionality))
        }
        taskList.add(changeFunctionality(newFunctionality))

        startTasks()
    }

    private fun processBindModelToGroup(group: Group, functionality: DeviceFunctionality.FUNCTIONALITY): List<Runnable> {
        val tasks = mutableListOf<Runnable>()
        DeviceFunctionality.getSigModels(meshNode.node, functionality).forEach { model ->
            tasks.add(bindModelToGroup(model, group))
            if (model.isSupportPublish) {
                tasks.add(setPublicationSettings(model, group, functionality))
            }
            if (model.isSupportSubscribe) {
                tasks.add(addSubscriptionSettings(model, group))
            }
        }
        return tasks
    }

    private fun processUnbindModelFromGroup(group: Group, functionality: DeviceFunctionality.FUNCTIONALITY): List<Runnable> {
        val tasks = mutableListOf<Runnable>()
        DeviceFunctionality.getSigModels(meshNode.node, functionality).forEach { model ->
            if (model.isSupportSubscribe) {
                tasks.add(removeSubscriptionSettings(model, group))
            }
            if (model.isSupportPublish) {
                tasks.add(clearPublicationSettings(model))
            }
            tasks.add(unbindModelFromGroup(model, group))
        }
        return tasks
    }

    private fun processUnsubscribeModelFromGroup(group: Group, functionality: DeviceFunctionality.FUNCTIONALITY): List<Runnable> {
        val tasks = mutableListOf<Runnable>()
        DeviceFunctionality.getSigModels(meshNode.node, functionality).forEach { model ->
            if (model.isSupportSubscribe) {
                tasks.add(removeSubscriptionSettings(model, group))
            }
        }
        return tasks
    }

    fun processChangeProxy(enabled: Boolean) {
        if (!enabled && isConnectedToProxyDevice()) {
            deviceConfigView.showDisableProxyAttentionDialog(DialogInterface.OnClickListener { _, which ->
                when (which) {
                    AlertDialog.BUTTON_POSITIVE -> {
                        changeProxy(enabled)
                    }
                    AlertDialog.BUTTON_NEGATIVE -> {
                        refreshView()
                    }
                }
            })
        } else {
            changeProxy(enabled)
        }
    }

    private fun bindNodeToGroup(group: Group): Runnable {
        return Runnable {
            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_ADDING_TO_GROUP, group.name)
            nodeControl.bind(group, PresenterNodeControlCallback())
        }
    }

    private fun unbindNodeFromGroup(group: Group): Runnable {
        return Runnable {
            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_REMOVING_FROM_GROUP, group.name)
            nodeControl.unbind(group, PresenterNodeControlCallback())
        }
    }

    private fun bindModelToGroup(model: Model, group: Group): Runnable {
        return Runnable {
            val functionalityBinder = FunctionalityBinder(group)

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_MODEL_ADDING, Converters.shortString(model.id))
            functionalityBinder.bindModel(model, PresenterFunctionalityBinderCallback())
        }
    }

    private fun unbindModelFromGroup(model: Model, group: Group): Runnable {
        return Runnable {
            val functionalityBinder = FunctionalityBinder(group)

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_MODEL_REMOVING, Converters.shortString(model.id))
            functionalityBinder.unbindModel(model, PresenterFunctionalityBinderCallback())
        }
    }

    private fun addSubscriptionSettings(model: Model, group: Group): Runnable {
        return Runnable {
            val subscriptionControl = SubscriptionControl(model)
            val subscriptionSettings = SubscriptionSettings(group)

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_SUBSCRIPTION_ADDING, Converters.shortString(model.id))
            subscriptionControl.addSubscriptionSettings(subscriptionSettings, PresenterSubscriptionSettingsCallback())
        }
    }

    private fun removeSubscriptionSettings(model: Model, group: Group): Runnable {
        return Runnable {
            val subscriptionSettings = SubscriptionSettings(group)
            val subscriptionControl = SubscriptionControl(model)

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_SUBSCRIPTION_REMOVING, Converters.shortString(model.id))
            subscriptionControl.removeSubscriptionSettings(subscriptionSettings, PresenterSubscriptionSettingsCallback())
        }
    }

    private fun setPublicationSettings(model: Model, group: Group, functionality: DeviceFunctionality.FUNCTIONALITY): Runnable {
        return Runnable {
            val subscriptionControl = SubscriptionControl(model)
            val publicationSettings = PublicationSettings(group)
            if (functionality == DeviceFunctionality.FUNCTIONALITY.SensorServer) {
                val publishPeriod = PublishPeriod(20, PublishPeriod.StepResolution.STEP_1_S)
                publicationSettings.setPeriod(publishPeriod)
            }
            publicationSettings.ttl = 5

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_PUBLICATION_SETTING, Converters.shortString(model.id))
            subscriptionControl.setPublicationSettings(publicationSettings, PresenterPublicationSettingsCallback())
        }
    }

    private fun clearPublicationSettings(model: Model): Runnable {
        return Runnable {
            val subscriptionControl = SubscriptionControl(model)

            setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_PUBLICATION_CLEARING, Converters.shortString(model.id))
            subscriptionControl.clearPublicationSettings(PresenterPublicationSettingsCallback())
        }
    }

    fun updateCompositionData() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_DCD_GETTING)
        configurationControl.getDeviceCompositionData(0, object : GetDeviceCompositionDataCallback {
            override fun success(node: Node?, deviceCompositionData: DeviceCompositionData?, elements: Array<out Element>?) {
                refreshView()
                deviceConfigView.dismissLoadingDialog()
            }

            override fun error(node: Node?, error: ErrorType?) {
                showErrorMessage(error)
            }
        })
    }

    fun changeName(name: String) {
        try {
            meshNode.node.name = name
        } catch (e: NodeChangeNameException) {
            deviceConfigView.showLoadingDialog()
            deviceConfigView.setLoadingDialogMessage(ErrorType(ErrorType.TYPE.CANNOT_SAVE_TO_DATABASE), showCloseButton = true)
        }
    }

    private fun changeFunctionality(functionality: DeviceFunctionality.FUNCTIONALITY): Runnable {
        return Runnable {
            try {
                setLoadingDialogMessageWithSteps(LoadingDialogMessage.CONFIG_FUNCTIONALITY_CHANGING)
                meshNodeManager.updateNodeFunc(meshNode, functionality)
                takeNextTask()
            } catch (e: NodeChangeNameException) {
                deviceConfigView.setLoadingDialogMessage(ErrorType(ErrorType.TYPE.CANNOT_SAVE_TO_DATABASE))
                error(e)
            }
        }
    }

    fun updatePollTimeout(node: Node) {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_POLL_TIMEOUT_GETTING)
        ConfigurationControl(node).getLpnPollTimeout(meshNode.node, object : LpnPollTimeoutCallback {
            override fun success(friendNode: Node, lowPowerNode: Node, pollTimeout: Int) {
                if (pollTimeout == 0) {
                    deviceConfigView.showToast(DeviceConfigView.ToastMessage.POLL_TIMEOUT_NOT_FRIEND)
                } else {
                    this@DeviceConfigPresenter.pollTimeoutFriend = node
                    this@DeviceConfigPresenter.pollTimeout = pollTimeout
                    deviceConfigView.promptGlobalTimeout(pollTimeout + 1)
                    deviceConfigView.showToast(DeviceConfigView.ToastMessage.POLL_TIMEOUT_UPDATED)
                }
                takeNextTask()
            }

            override fun error(friendNode: Node, lowPowerNode: Node, errorType: ErrorType) {
                deviceConfigView.setLoadingDialogMessage(errorType, showCloseButton = true)
                abandonTasks()
            }
        })
    }

    private fun changeProxy(enabled: Boolean) {
        deviceConfigView.showLoadingDialog()
        if (enabled) {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_PROXY_ENABLING)
        } else {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_PROXY_DISABLING)
        }
        configurationControl.setProxy(enabled, object : PresenterSetNodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isProxy = enabled
                takeNextTask()
            }
        })
    }

    fun updateProxy() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_PROXY_GETTING)
        configurationControl.checkProxyStatus(object : NodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isProxy = enabled
                super.success(node, enabled)
            }
        })
    }

    fun changeRelay(enabled: Boolean) {
        deviceConfigView.showLoadingDialog()
        if (enabled) {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RELAY_ENABLING)
        } else {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RELAY_DISABLING)
        }
        configurationControl.setRelay(enabled, retransmissionCount, retransmissionInterval, object : PresenterSetNodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isRelay = enabled
                takeNextTask()
            }
        })
    }

    fun updateRelay() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RELAY_GETTING)
        configurationControl.checkRelayStatus(object : NodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isRelay = enabled
                super.success(node, enabled)
            }
        })
    }

    fun changeFriend(enabled: Boolean) {
        deviceConfigView.showLoadingDialog()
        if (enabled) {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_FRIEND_ENABLING)
        } else {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_FRIEND_DISABLING)
        }
        configurationControl.setFriend(enabled, object : PresenterSetNodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isFriend = enabled
                takeNextTask()
            }
        })
    }

    fun updateFriend() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_FRIEND_GETTING)
        configurationControl.checkFriendStatus(object : NodeBehaviourCallback() {
            override fun success(node: Node?, enabled: Boolean) {
                isFriend = enabled
                super.success(node, enabled)
            }
        })
    }

    fun changeRetransmission(enabled: Boolean) {
        deviceConfigView.showLoadingDialog()
        if (enabled) {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RETRANSMISSION_ENABLING)
        } else {
            deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RETRANSMISSION_DISABLING)
        }
        val count = if (enabled) retransmissionCount else 0
        val interval = if (enabled) retransmissionInterval else 0
        configurationControl.setRetransmissionConfiguration(count, interval, object : NodeRetransmissionConfigurationCallback {
            override fun success(node: Node?, retransmissionCount: Int, retransmissionInterval: Int) {
                isRetransmission = retransmissionCount != 0
                takeNextTask()
            }

            override fun error(node: Node?, error: ErrorType?) {
                showErrorMessage(error)
            }
        })
    }

    fun updateRetransmission() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_RETRANSMISSION_GETTING)
        configurationControl.checkRetransmissionConfigurationStatus(object : NodeRetransmissionConfigurationCallback {
            override fun success(node: Node?, retransmissionCount: Int, retransmissionIntervalSteps: Int) {
                isRetransmission = retransmissionCount != 0
                takeNextTask()
            }

            override fun error(node: Node?, error: ErrorType?) {
                deviceConfigView.setLoadingDialogMessage(error, showCloseButton = true)
                abandonTasks()
            }
        })
    }

    fun changeLpnGlobalTimeout(lpnGlobalTimeoutSecsText: String) {
        val lpnGlobalTimeout: Int
        try {
            val lpnGlobalTimeoutMsText = lpnGlobalTimeoutSecsText.plus("000") // milliseconds
            lpnGlobalTimeout = lpnGlobalTimeoutMsText.toInt()
        } catch (e: NumberFormatException) {
            deviceConfigView.showToast(DeviceConfigView.ToastMessage.LPN_TIMEOUT_WRONG_RANGE)
            return
        }

        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_LPN_TIMEOUT_SETTING)
        ConfigurationControlSettings().lpnLocalTimeout = lpnGlobalTimeout
        this.lpnGlobalTimeout = convertMsToS(lpnGlobalTimeout)
        takeNextTask()
    }

    private fun updateLpnGlobalTimeout() {
        deviceConfigView.showLoadingDialog()
        deviceConfigView.setLoadingDialogMessage(LoadingDialogMessage.CONFIG_LPN_TIMEOUT_GETTING)
        lpnGlobalTimeout = convertMsToS(ConfigurationControlSettings().lpnLocalTimeout)
        takeNextTask()
    }

    private fun setLoadingDialogMessageWithSteps(loadingMessage: LoadingDialogMessage, message: String = "") {
        deviceConfigView.setLoadingDialogMessage(loadingMessage, message, taskList.size, allTasksCount)
    }

    private fun convertMsToS(milliseconds: Int) = milliseconds / 1000

    // callback classes

    inner class PresenterNodeControlCallback : NodeControlCallback {
        override fun succeed() {
            takeNextTask()
        }

        override fun error(errorType: ErrorType?) {
            showErrorMessageWithRetryButton(errorType)
        }
    }

    inner class PresenterFunctionalityBinderCallback : FunctionalityBinderCallback {
        override fun succeed(succeededModels: MutableList<Model>?, group: Group?) {
            takeNextTask()
        }

        override fun error(failedModels: MutableList<Model>?, group: Group?, errorType: ErrorType?) {
            showErrorMessageWithRetryButton(errorType)
        }
    }

    inner class PresenterPublicationSettingsCallback : PublicationSettingsCallback {
        override fun success(meshModel: Model?, publicationSettings: PublicationSettings?) {
            takeNextTask()
        }

        override fun error(meshModel: Model?, errorType: ErrorType?) {
            showErrorMessageWithRetryButton(errorType)
        }
    }

    inner class PresenterSubscriptionSettingsCallback : SubscriptionSettingsCallback {
        override fun success(meshModel: Model?, subscriptionSettings: SubscriptionSettings?) {
            takeNextTask()
        }

        override fun error(meshModel: Model?, errorType: ErrorType?) {
            showErrorMessageWithRetryButton(errorType)
        }
    }

    abstract inner class NodeBehaviourCallback : CheckNodeBehaviourCallback {
        override fun success(node: Node?, enabled: Boolean) {
            takeNextTask()
        }

        override fun error(node: Node?, error: ErrorType?) {
            deviceConfigView.setLoadingDialogMessage(error, showCloseButton = true)
            abandonTasks()
        }
    }

    abstract inner class PresenterSetNodeBehaviourCallback : SetNodeBehaviourCallback {
        override fun error(node: Node?, error: ErrorType?) {
            showErrorMessage(error)
        }
    }
}