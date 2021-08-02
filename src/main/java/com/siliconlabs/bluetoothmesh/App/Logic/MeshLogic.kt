/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.siliconlab.bluetoothmesh.adk.BluetoothMesh
import com.siliconlab.bluetoothmesh.adk.configuration.BluetoothMeshConfiguration
import com.siliconlab.bluetoothmesh.adk.configuration.LocalVendorModel
import com.siliconlab.bluetoothmesh.adk.data_model.group.Group
import com.siliconlab.bluetoothmesh.adk.data_model.network.Network
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlab.bluetoothmesh.adk.export_data.ExportKeys
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.BluetoothConnectableDevice
import java.io.File

class MeshLogic(val context: Context) {

    val bluetoothMesh: BluetoothMesh

    companion object {
        val supportedLocalVendorModels = mutableListOf<LocalVendorModel>()
    }

    init {
        BluetoothMesh.initialize(context, BluetoothMeshConfiguration(supportedLocalVendorModels))
        bluetoothMesh = BluetoothMesh.getInstance()
    }

    // data
    var currentNetwork: Network? = null
    var currentSubnet: Subnet? = null
    var currentGroup: Group? = null
    var deviceToConfigure: MeshNode? = null
    var provisionedBluetoothConnectableDevice: BluetoothConnectableDevice? = null

    fun shareNetworkKeys(): Intent {
        val keys = prepareKeys(currentNetwork)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/json"

        val fileName = "MeshDictionary.json"
        val filePath = context.filesDir.path.toString() + "/" + fileName
        val file = File(filePath)

        file.createNewFile()
        file.writeText(keys)

        val uri = FileProvider.getUriForFile(context, "com.siliconlabs.bluetoothmesh.fileprovider", file)

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

        return shareIntent
    }

    fun prepareSaveKeysIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }

    fun saveKeysToLocalStorage(treeUri: Uri) {
        val keys = prepareKeys(currentNetwork)

        val file = DocumentFile.fromTreeUri(context, treeUri)
        val fileName = "MeshDictionary.json"
        val createdFile = file?.createFile("application/json", fileName)
        context.contentResolver?.openOutputStream(createdFile?.uri!!)?.apply {
            write(keys.toByteArray())
            flush()
            close()
        }
    }

    private fun prepareKeys(network: Network?): String {
        val setOfNetworks = hashSetOf(network)
        val exportKeys = ExportKeys(setOfNetworks)
        return exportKeys.export()
    }

}