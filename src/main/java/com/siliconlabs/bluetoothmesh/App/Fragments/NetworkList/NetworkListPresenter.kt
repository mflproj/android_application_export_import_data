/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList


import android.util.Log
import com.google.gson.Gson
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.SubnetChangeNameException
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.SubnetCreationException
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonExporter
import com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonImporter
import com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects.JsonMesh
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.Models.MeshNetworkManager
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager

class NetworkListPresenter(private val networkListView: NetworkListView, private val meshLogic: MeshLogic, private val meshNetworkManager: MeshNetworkManager, val networkConnectionLogic: NetworkConnectionLogic, val meshNodeManager: MeshNodeManager) : BasePresenter {
    private val TAG: String = javaClass.canonicalName!!

    private var network = meshLogic.currentNetwork!!

    override fun onResume() {
        refreshList()
    }

    override fun onPause() {
    }

    private fun refreshList() {
        networkListView.setNetworkList(network.subnets)
    }

    // View callbacks

    fun addNetwork(name: String): Boolean {
        if (name.trim().isEmpty()) {
            return false
        }

        try {
            meshNetworkManager.createSubnet(name)
        } catch (e: SubnetCreationException) {
            Log.e(TAG, e.toString())
            networkListView.showToast(NetworkListView.TOAST_MESSAGE.ERROR_CREATING_NETWORK)
        }

        refreshList()
        return true
    }

    fun updateNetwork(networkInfo: Subnet, newName: String): Boolean {
        if (newName.trim().isEmpty()) {
            return false
        }

        try {
            networkInfo.name = newName
            networkListView.showToast(NetworkListView.TOAST_MESSAGE.SUCCESS_UPDATE)
        } catch (e: SubnetChangeNameException) {
            return false
        }

        refreshList()
        return true
    }

    fun deleteNetwork(networkInfo: Subnet) {
        networkListView.showLoadingDialog()
        if (networkInfo.nodes.isEmpty()) {
            removeNetwork(networkInfo)
        } else {
            removeNetworkWithNodes(networkInfo)
        }
    }

    fun deleteNetworkLocally(subnet: Subnet) {
        subnet.removeOnlyFromLocalStructure()
        refreshList()
    }

    private fun removeNetwork(subnet: Subnet) {
        meshNetworkManager.removeSubnet(subnet, object : MeshNetworkManager.DeleteNetworksCallback {
            override fun success() {
                Log.d(TAG, "removeSubnet success")
                networkListView.dismissLoadingDialog()
                refreshList()
            }

            override fun error(subnet: Subnet?, error: ErrorType?) {
                Log.d(TAG, "removeSubnet error")
                networkListView.dismissLoadingDialog()
                networkListView.showDeleteNetworkLocallyDialog(subnet!!, error!!)
                refreshList()
            }
        })
    }

    private fun removeNetworkWithNodes(subnet: Subnet) {
        networkConnectionLogic.addListener(object : NetworkConnectionListener {
            fun clear() {
                refreshList()
                networkConnectionLogic.disconnect()
                networkConnectionLogic.removeListener(this)
            }

            override fun connecting() {
                Log.d(TAG, "connecting")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK, subnet.name)
            }

            override fun connected() {
                Log.d(TAG, "connected")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.REMOVING_NETWORK, subnet.name)
                meshNetworkManager.removeSubnet(subnet, object : MeshNetworkManager.DeleteNetworksCallback {
                    override fun success() {
                        Log.d(TAG, "removeSubnet success")
                        networkConnectionLogic.disconnect()
                        removeNodesFunc(subnet)
                        networkListView.dismissLoadingDialog()
                        refreshList()
                    }

                    override fun error(subnet: Subnet?, error: ErrorType?) {
                        Log.d(TAG, "removeSubnet error")
                        networkListView.dismissLoadingDialog()
                        networkListView.showDeleteNetworkLocallyDialog(subnet!!, error!!)
                        clear()
                    }
                })
                networkConnectionLogic.removeListener(this)
            }

            override fun disconnected() {
                Log.d(TAG, "disconnected")
            }

            override fun initialConfigurationLoaded() {
            }

            override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
                Log.d(TAG, "connectionMessage")
                clear()
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK_ERROR, messageType.toString(), true)
            }

            override fun connectionErrorMessage(error: ErrorType) {
                Log.d(TAG, "connectionErrorMessage")
                clear()
                networkListView.dismissLoadingDialog()
                networkListView.showDeleteNetworkLocallyDialog(subnet, error)
            }
        })
        networkConnectionLogic.connect(subnet)
    }

    //Method to export given subnet to a string and then reimport it into the database
    //and then reinitialize the network
    fun exportAndImportNetwork(subnet: Subnet)
    {
        val sequenceNumber = meshNetworkManager.bluetoothMesh.sequenceNumber
        val jsonExporter = JsonExporter()
        val data = jsonExporter.export()
        val json = Gson().fromJson<JsonMesh>(data, JsonMesh::class.java)
        JsonImporter(json).import()
        val network_imported = meshNetworkManager.bluetoothMesh.networks.find { x -> x.uuid == subnet.network.uuid }
        if(network_imported == null)
        {
            Log.i(TAG, "unable to find network to re-import")
            networkListView.showImportNetworkDialogErrorMessage(subnet, "Re-imported network data not found in database. Please try again.")
            return
        }
        val netImport = networkConnectionLogic.networkImportList.find { x -> x.uuid == network_imported.uuid }
        var initialized = false
        if(netImport == null)
        {
            Log.i(TAG, "network initialization info not found")
            networkListView.showImportNetworkDialogErrorMessage(subnet, "Network settings to initialize not found. Go to device screen to retrieve ")
            return
        }
        var errorMsg: String? = ""
        meshLogic.currentNetwork = network_imported
        network = meshLogic.currentNetwork!!
        try
        {
            meshNetworkManager.bluetoothMesh.initializeNetwork(network_imported, netImport.provisionerAddress, netImport.fourIndex)
            initialized = true
        }
        catch (e: Exception)
        {
            errorMsg = e.message
            Log.i(TAG, "initialize network error -> " + e.message)
        }
        meshNetworkManager.bluetoothMesh.sequenceNumber = sequenceNumber
        networkListView.dismissLoadingDialog()
        if(!initialized)
        {
            networkListView.showImportNetworkDialogErrorMessage(subnet, "unable to initialize network ${subnet.name} after importing data, error: " + errorMsg)
        }
        refreshList()
    }

    //Method for retrieving IV index for selected subnet from the network for re-importing into database
    fun retrieveDataFromNetworkForReimportingDatabase() {
        if(meshLogic.currentNetwork == null)
        {
            return
        }
        networkListView.showLoadingDialog()
        val subnet = meshLogic.currentSubnet!!
        var dataObtained = false

        networkConnectionLogic.addListener(object : NetworkConnectionListener {
            fun clear() {
                refreshList()
                networkConnectionLogic.disconnect()
                networkConnectionLogic.removeListener(this)
                if(dataObtained)
                {
                    networkListView.showImportDataDialog(subnet!!)
                }
            }

            override fun connecting() {
                Log.d(TAG, "connecting")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK, subnet?.name!!)
            }


            override fun connected() {
                Log.d(TAG, "connected")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.EXPORTING_AND_IMPORTING_NETWORK_DATA, subnet.name)
                networkListView.dismissLoadingDialog()
                dataObtained = true
                clear()
                return
            }

            override fun disconnected() {
                Log.d(TAG, "disconnected")
            }

            override fun initialConfigurationLoaded() {
            }

            override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
                Log.d(TAG, "connectionMessage")
                clear()
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK_ERROR, messageType.toString(), true)
            }

            override fun connectionErrorMessage(error: ErrorType) {
                Log.d(TAG, "connectionErrorMessage")
                clear()
                networkListView.dismissLoadingDialog()
                networkListView.showImportNetworkDialogErrorMessage(subnet, "Unable to connect to network")

            }
        })
        networkConnectionLogic.connect(subnet!!)
    }





    fun removeNodesFunc(subnet: Subnet) {
        meshNodeManager.getMeshNodes(subnet).forEach {
            meshNodeManager.removeNodeFunc(it)
        }
    }

    fun networkClicked(subnet: Subnet) {
        meshLogic.currentSubnet = subnet
        networkListView.showNetworkOptionsDialog()
    }

}
