package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport

import com.google.gson.GsonBuilder
import com.siliconlab.bluetoothmesh.adk.BluetoothMesh
import com.siliconlab.bluetoothmesh.adk.data_model.dcd.DeviceCompositionData
import com.siliconlab.bluetoothmesh.adk.data_model.element.Element
import com.siliconlab.bluetoothmesh.adk.data_model.model.Model
import com.siliconlab.bluetoothmesh.adk.data_model.model.Retransmit
import com.siliconlab.bluetoothmesh.adk.data_model.node.NetworkTransmit
import com.siliconlab.bluetoothmesh.adk.data_model.node.Node
import com.siliconlab.bluetoothmesh.adk.data_model.node.RelayRetransmit
import com.siliconlab.bluetoothmesh.adk.data_model.provisioner.AddressRange
import com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects.*
import java.util.*

class JsonExporter {
    fun export(): String {
        val jsonMesh = createJsonMesh()
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(jsonMesh)
    }
    private val network = BluetoothMesh.getInstance().networks.iterator().next()
    private fun createJsonMesh(): JsonMesh {
        return JsonMesh().apply {
            schema = "http://json-schema.org/draft-04/schema#"
            id = "https://www.bluetooth.com/specifications/assigned-numbers/mesh-profile/cdbschema.json#"
            version = network.version
            meshUUID = Converter.uuidToString(network.uuid)
            meshName = network.name
            timestamp = Converter.longToTimestamp(System.currentTimeMillis())
            provisioners = createJsonProvisioners()
            netKeys = createJsonNetKeys()
            appKeys = createJsonAppKeys()
            nodes = createJsonNodes()
            groups = createJsonGroups()
            scenes = createJsonScenes()
        }
    }
    private fun createJsonScenes(): Array<JsonScene> {
        return network.scenes.map {
            JsonScene().apply {
                name = it.name
                number = Converter.intToHex(it.number, 4)
                addresses = it.nodes
                    .map { Converter.intToHex(it.primaryElementAddress, 4)!! }
                    .toTypedArray()
            }
        }.toTypedArray()
    }
    private fun createJsonNodes(): Array<JsonNode> {
        return network.subnets.flatMap { it.nodes }.map {
            JsonNode().apply {
                UUID = Converter.bytesToHex(it.uuid)
                unicastAddress = Converter.intToHex(it.primaryElementAddress, 4)
                deviceKey = Converter.bytesToHex(it.devKey.key)
                security = it.nodeSecurity.security.name.toLowerCase(Locale.ROOT)

                netKeys = createJsonNodeNetKeys(it)
                configComplete = it.nodeSettings.isConfigComplete
                name = it.name
                cid = Converter.intToHex(it.deviceCompositionData.cid, 4)
                pid = Converter.intToHex(it.deviceCompositionData.pid, 4)
                vid = Converter.intToHex(it.deviceCompositionData.vid, 4)
                crpl = Converter.intToHex(it.deviceCompositionData.crpl, 4)
                features = createJsonFeatures(it)
                secureNetworkBeacon = it.nodeSecurity.isSecureNetworkBeacon
                defaultTTL = it.nodeSettings.defaultTTL
                networkTransmit = createJsonNetworkTransmit(it.nodeSettings.networkTransmit)
                relayRetransmit = createJsonRelayRetransmit(it.nodeSettings.relayRetransmit)
                appKeys = createJsonNodeAppKeys(it)
                elements = createJsonElements(it)
                blacklisted = it.nodeSecurity.isBlacklisted
                knownAddresses = fillGroupsInJsonNode(it)
            }
        }.toTypedArray()
    }
    private fun fillGroupsInJsonNode(node: Node): Array<String> {
        return node.groups
            .map { Converter.intToHex(it.address)!! }
            .toTypedArray()
    }
    private fun createJsonRelayRetransmit(relayRetransmit: RelayRetransmit?): JsonRelayRetransmit?
    {
        return relayRetransmit?.let {
            JsonRelayRetransmit().apply {
                count = relayRetransmit.count
                interval = relayRetransmit.interval
            }
        }
    }
    private fun createJsonNetworkTransmit(networkTransmit: NetworkTransmit?): JsonNetworkTransmit?
    {
        return networkTransmit?.let {
            JsonNetworkTransmit().apply {
                count = networkTransmit.count
                interval = networkTransmit.interval
            }
        }
    }
    private fun createJsonFeatures(node: Node): JsonFeature? {
        val features = node.nodeSettings.features
        val dcd = node.deviceCompositionData
        return JsonFeature().apply {
            if (features != null) {
                relay = convertFeatureState(dcd?.supportsRelay(), features.isRelayEnabled)
            }
            if (features != null) {
                friend = convertFeatureState(dcd?.supportsFriend(), features.isFriendEnabled)
            }
            if (features != null) {
                lowPower = convertLowPowerFeatureState(dcd, features.isLowPower)
            }
            if (features != null) {
                proxy = convertFeatureState(dcd?.supportsProxy(), features.isProxyEnabled)
            }
        }
    }
    private fun convertFeatureState(supports: Boolean?, enabled: Boolean?): Int? {
        return when {
            supports == false -> 2
            enabled == null -> null
            enabled -> 1
            else -> 0
        }
    }
    private fun convertLowPowerFeatureState(dcd: DeviceCompositionData?, feature: Boolean?): Int? {
        return when {
            dcd != null && !dcd.supportsLowPower() || feature != null && !feature -> 2
            dcd != null && dcd.supportsLowPower() || feature != null && feature -> 1
            else -> null
        }
    }
    private fun createJsonElements(node: Node): Array<JsonElement> {
        return node.elements.map {
            JsonElement().apply {
                name = it.name
                index = it.index
                location = Converter.intToHex(it.location, 4)
                models = createJsonModels(it)
            }
        }.toTypedArray()
    }
    private fun createJsonModels(element: Element): Array<JsonModel> {
        return element.sigModels.union(element.vendorModels).map {
            val idWidth = if (it.isSIGModel) 4 else 8
            JsonModel().apply {
                modelId = Converter.intToHex(it.id, idWidth)
                subscribe = createSubscribe(it)
                publish = createJsonPublish(it)
                bind = createBind(it)
                knownAddresses = fillGroupsInJsonModel(it)
            }
        }.toTypedArray()
    }
    private fun fillGroupsInJsonModel(model: Model): Array<String> {
        return model.boundGroups
            .map { group -> Converter.intToHex(group.address)!! }
            .toTypedArray()
    }
    private fun createBind(model: Model): Array<Int> {
        return model.boundGroups
            .map { it.appKey.keyIndex }
            .toTypedArray()
    }
    private fun createSubscribe(model: Model): Array<String> {
        return model.modelSettings.subscriptions
            .map { Converter.addressToHex(it)!! }
            .toTypedArray()
    }
    private fun createJsonPublish(model: Model): JsonPublish? {
        return model.modelSettings.publish?.let {
            JsonPublish().apply {
                address = Converter.addressToHex(it.address)
                ttl = it.ttl
                period = it.period
                credentials = it.credentials.value
                retransmit = createJsonRetransmit(it.retransmit)
            }

        }
    }
    private fun createJsonRetransmit(retransmit: Retransmit): JsonRetransmit {
        return JsonRetransmit().apply {
            count = retransmit.count
            interval = retransmit.interval
        }
    }
    private fun createJsonNodeAppKeys(node: Node): Array<JsonNodeKey> {
        return node.nodeSecurity.appKeys.map {
            JsonNodeKey().apply {
                index = it.appKeyIndex
                updated = it.isUpdated
            }
        }.toTypedArray()
    }
    private fun createJsonNodeNetKeys(node: Node): Array<JsonNodeKey> {
        return node.nodeSecurity.netKeys.map {
            JsonNodeKey().apply {
                index = it.netKeyIndex
                updated = it.isUpdated
            }
        }.toTypedArray()
    }
    private fun createJsonAppKeys(): Array<JsonAppKey> {
        return network.subnets.flatMap { it.groups }.map {
            JsonAppKey().apply {
                name = it.appKey.name ?: ""
                index = it.appKey.keyIndex
                key = Converter.bytesToHex(it.appKey.key)
                oldKey = Converter.bytesToHex(it.appKey.oldKey)
                boundNetKey = it.subnet.netKey.keyIndex
            }
        }.toTypedArray()
    }
    private fun createJsonGroups(): Array<JsonGroup> {
        return network.subnets.flatMap { it.groups }.map {
            JsonGroup().apply {
                name = it.name
                address = Converter.intToHex(it.address, 4)
                appKeyIndex = it.appKey.keyIndex
                parentAddress = Converter.intToHex(it.parentGroup?.address ?: 0, 4)
            }
        }.toTypedArray()
    }
    private fun createJsonNetKeys(): Array<JsonNetKey> {
        return network.subnets.map {
            JsonNetKey().apply {
                name = it.name
                index = it.netKey.keyIndex
                phase = it.subnetSecurity.keyRefreshPhase ?: 0
                timestamp = Converter.longToTimestamp(it.subnetSecurity.keyRefreshTimestamp)
                key = Converter.bytesToHex(it.netKey.key)
                minSecurity = it.subnetSecurity.minSecurity.name.toLowerCase(Locale.ROOT)
                oldKey = Converter.bytesToHex(it.netKey.oldKey)
            }
        }.toTypedArray()

    }
    private fun createJsonProvisioners(): Array<JsonProvisioner> {
        return network.provisioners.map {
            JsonProvisioner().apply {
                provisionerName = it.name ?: ""
                UUID = Converter.uuidToString(it.uuid)
                allocatedUnicastRange = createJsonAddressRange(it.allocatedUnicastRange)
                allocatedGroupRange = createJsonAddressRange(it.allocatedGroupRange)
                allocatedSceneRange = createJsonSceneRange(it.allocatedSceneRange)
            }
        }.toTypedArray()
    }
    private fun createJsonAddressRange(ranges: List<AddressRange>): Array<JsonAddressRange> {
        return ranges.map {
            JsonAddressRange().apply {
                lowAddress = Converter.intToHex(it.lowAddress, 4)
                highAddress = Converter.intToHex(it.highAddress, 4)
            }
        }.toTypedArray()
    }
    private fun createJsonSceneRange(ranges: List<AddressRange>): Array<JsonSceneRange> {
        return ranges.map {
            JsonSceneRange().apply {
                firstScene = Converter.intToHex(it.lowAddress, 4)
                lastScene = Converter.intToHex(it.highAddress, 4)
            }
        }.toTypedArray()
    }

}
