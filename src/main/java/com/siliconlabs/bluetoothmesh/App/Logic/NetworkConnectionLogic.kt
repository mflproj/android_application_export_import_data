/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Logic

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.configuration_control.ConfigurationControl
import com.siliconlab.bluetoothmesh.adk.configuration_control.GetDeviceCompositionDataCallback
import com.siliconlab.bluetoothmesh.adk.configuration_control.SetNodeBehaviourCallback
import com.siliconlab.bluetoothmesh.adk.connectable_device.*
import com.siliconlab.bluetoothmesh.adk.connectable_device.ProxyConnection.MESH_PROXY_SERVICE
import com.siliconlab.bluetoothmesh.adk.data_model.dcd.DeviceCompositionData
import com.siliconlab.bluetoothmesh.adk.data_model.element.Element
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlabs.bluetoothmesh.App.Models.BluetoothConnectableDevice
import com.siliconlabs.bluetoothmesh.App.Models.NetworkImport
import java.util.*

class NetworkConnectionLogic(private val context: Context, private val connectableDeviceHelper: ConnectableDeviceHelper, val bluetoothLeScanLogic: BluetoothScanner) : BluetoothConnectableDevice.DeviceConnectionCallback {
    private val TAG: String = javaClass.canonicalName!!

    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    var networkImportList : MutableList<NetworkImport> = mutableListOf()

    enum class CONNECTION_STATE {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private var currentState = CONNECTION_STATE.DISCONNECTED

    private val listeners: ArrayList<NetworkConnectionListener> = ArrayList()

    private var networkInfo: Subnet? = null

    var proxyConnection: ProxyConnection? = null

    private var bluetoothConnectableDevice: BluetoothConnectableDevice? = null

    private var connectionTimeoutRunnable: Runnable = Runnable {
        connetionTimeout()
    }

    fun connect(network: Subnet) {
        synchronized(this) {
            if (networkInfo != null) {
                // new network
                if (networkInfo != network) {
                    disconnect()
                } else {
                    // already connected/connecting
                    if (currentState != CONNECTION_STATE.DISCONNECTED) {
                        return
                    }
                }
            }

            Log.d(TAG, "Connecting to subnet")
            setCurrentState(CONNECTION_STATE.CONNECTING)

            networkInfo = network
            bluetoothLeScanLogic.addScanCallback(scanCallback)
            startScan()
        }
    }

    fun connect(bluetoothConnectableDevice: BluetoothConnectableDevice, refreshBluetoothDevice: Boolean) {
        synchronized(currentState) {
            if (networkInfo != null) {
                disconnect()
            }

            Log.d(TAG, "Connecting to device")
            setCurrentState(CONNECTION_STATE.CONNECTING)

            // workaround to 133 gatt issue
            // https://github.com/googlesamples/android-BluetoothLeGatt/issues/44
            uiHandler.postDelayed({
                this@NetworkConnectionLogic.bluetoothConnectableDevice = bluetoothConnectableDevice
                bluetoothConnectableDevice.addDeviceConnectionCallback(this@NetworkConnectionLogic)

                proxyConnection = ProxyConnection(bluetoothConnectableDevice)
                //Retrieve IV index for network to initialize network after importing into database
                proxyConnection!!.observeSecureNetworkBeacon(object: SecureNetworkBeaconCallback
                {
                    override fun success(p0: Int, p1: Boolean, p2: Boolean, fourIndex: Int) {
                        val node = getNode(bluetoothConnectableDevice)
                        val networkUuid = node?.subnets?.first()?.network?.uuid
                        if(networkUuid != null)
                        {
                            val netImport = networkImportList.find { x -> x.uuid == networkUuid }
                            if(netImport == null)
                            {
                                networkImportList.add(NetworkImport(uuid = networkUuid, fourIndex = fourIndex, provisionerAddress = node?.primaryElementAddress!!))
                            }
                            else
                            {
                                netImport.fourIndex = fourIndex
                                netImport.provisionerAddress = node?.primaryElementAddress!!
                            }
                        }
                        Log.i("fourIndex", "p0: $p0, p1: $p1, p2: $p2, fourIndex: $fourIndex")
                        setCurrentState(CONNECTION_STATE.CONNECTED)
                    }
                })
                proxyConnection!!.connectToProxy(refreshBluetoothDevice, object : ConnectionCallback {
                    override fun success(device: ConnectableDevice) {
                        Log.d(TAG, "ConnectionCallback success")

                    }

                    override fun error(device: ConnectableDevice, error: ErrorType) {
                        Log.d(TAG, "ConnectionCallback error=$error")
                        setCurrentState(CONNECTION_STATE.DISCONNECTED)
                        connectionErrorMessage(error)
                    }
                })

            }, 500)
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting")
        networkInfo = null
        stopScan()
        bluetoothLeScanLogic.removeScanCallback(scanCallback)
        setCurrentState(CONNECTION_STATE.DISCONNECTED)
        bluetoothConnectableDevice?.removeDeviceConnectionCallback(this)
        bluetoothConnectableDevice = null
        proxyConnection?.disconnect(object : DisconnectionCallback {
            override fun success(device: ConnectableDevice?) {
                Log.d(TAG, "Disconnecting success")
            }

            override fun error(device: ConnectableDevice?, error: ErrorType?) {
                Log.d(TAG, "Disconnecting error: $error")
            }
        })
    }

    fun setupInitialNodeConfiguration(node: Node) {
        enableProxy(node)
    }

    private fun enableProxy(node: Node) {
        ConfigurationControl(node).setProxy(true, object : SetNodeBehaviourCallback {
            override fun error(node: Node, error: ErrorType) {
                connectionErrorMessage(error)
            }

            override fun success(node: Node, enabled: Boolean) {
                getDeviceCompositionData(node)
            }
        })
    }

    private fun getDeviceCompositionData(node: Node) {
        ConfigurationControl(node).getDeviceCompositionData(0, object : GetDeviceCompositionDataCallback {
            override fun error(node: Node, error: ErrorType) {
                connectionErrorMessage(error)
            }

            override fun success(node: Node, deviceCompositionData: DeviceCompositionData, elements: Array<out Element>?) {
                enableNodeIdentity(node)
            }
        })
    }

    private fun enableNodeIdentity(node: Node) {
        ConfigurationControl(node).setNodeIdentity(true, node.subnets.first(), object : SetNodeBehaviourCallback {
            override fun error(node: Node, error: ErrorType) {
                connectionErrorMessage(error)
            }

            override fun success(node: Node, enabled: Boolean) {
                listeners.forEach { listener -> listener.initialConfigurationLoaded() }
            }
        })
    }

    fun addListener(networkConnectionListener: NetworkConnectionListener) {
        synchronized(listeners) {
            listeners.add(networkConnectionListener)

            notifyCurrentState()
        }
    }

    fun removeListener(networkConnectionListener: NetworkConnectionListener) {
        synchronized(listeners) {
            listeners.remove(networkConnectionListener)
        }
    }

    // DeviceConnectionCallback

    override fun onConnectedToDevice() {
        Log.d(TAG, "onConnectedToDevice")
        //ignore
    }

    override fun onDisconnectedFromDevice() {
        Log.d(TAG, "onDisconnectedFromDevice")
        disconnect()
    }

    // ScanCallback

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(TAG, result.toString())

            val bluetoothConnectableDevice = BluetoothConnectableDevice(context, result, bluetoothLeScanLogic)

            val subnets = connectableDeviceHelper.findSubnets(bluetoothConnectableDevice)

            if (subnets.contains(networkInfo)) {
                stopScan()
            } else {
                return
            }

            connect(bluetoothConnectableDevice, refreshBluetoothDevice = false)
        }
    }

    private fun startScan() {
        Log.d(TAG, "Start scanning")

        networkInfo?.apply {
            if (nodes.isEmpty()) {
                connectionMessage(NetworkConnectionListener.MessageType.NO_NODE_IN_NETWORK)
                return
            }

            val meshProxyService = ParcelUuid(MESH_PROXY_SERVICE)
            if (bluetoothLeScanLogic.startLeScan(meshProxyService)) {
                uiHandler.removeCallbacks(connectionTimeoutRunnable)
                uiHandler.postDelayed(connectionTimeoutRunnable, 10000)
            }
        }
    }

    private fun stopScan() {
        Log.d(TAG, "Stop Scanning")

        bluetoothLeScanLogic.stopLeScan()
    }

    private fun connetionTimeout() {
        Log.d(TAG, "Connection timeout")

        stopScan()
        setCurrentState(CONNECTION_STATE.DISCONNECTED)
        connectionErrorMessage(ErrorType(ErrorType.TYPE.COULD_NOT_CONNECT_TO_DEVICE))
    }

    fun isConnected(): Boolean {
        synchronized(this) {
            return currentState == CONNECTION_STATE.CONNECTED
        }
    }

    private fun setCurrentState(currentState: CONNECTION_STATE) {
        Log.d(TAG, "setCurrentState: $currentState")
        synchronized(this) {
            if (this.currentState == currentState) {
                return
            }
            uiHandler.removeCallbacks(connectionTimeoutRunnable)
            this.currentState = currentState
        }
        notifyCurrentState()
    }

    private fun notifyCurrentState() {
        synchronized(listeners) {
            uiHandler.post {
                when (currentState) {
                    CONNECTION_STATE.DISCONNECTED -> {
                        listeners.forEach { listener -> listener.disconnected() }
                    }
                    CONNECTION_STATE.CONNECTING -> {
                        listeners.forEach { listener -> listener.connecting() }
                    }
                    CONNECTION_STATE.CONNECTED -> {
                        listeners.forEach { listener -> listener.connected() }
                    }
                }
            }
        }
    }

    private fun connectionMessage(message: NetworkConnectionListener.MessageType) {
        synchronized(listeners) {
            uiHandler.post {
                listeners.forEach { listener -> listener.connectionMessage(message) }
            }
        }
    }

    private fun connectionErrorMessage(errorType: ErrorType) {
        synchronized(listeners) {
            uiHandler.post {
                listeners.forEach { listener -> listener.connectionErrorMessage(errorType) }
            }
        }
    }

    fun getCurrentlyConnectedNode(): Node? {
        return connectableDeviceHelper.findNode(bluetoothConnectableDevice)
    }

    //Get node element for given BluetoothConnectableDevice
    fun getNode(bluetoothConnectableDevice: BluetoothConnectableDevice): Node? {
        return connectableDeviceHelper.findNode(bluetoothConnectableDevice)
    }

}