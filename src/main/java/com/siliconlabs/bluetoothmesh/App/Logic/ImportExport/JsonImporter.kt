package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport

import com.siliconlab.bluetoothmesh.adk.BluetoothMesh
import com.siliconlab.bluetoothmesh.adk.data_model.Security
import com.siliconlab.bluetoothmesh.adk.data_model.model.Credentials
import com.siliconlab.bluetoothmesh.adk.importer.*
import com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects.*
import java.util.*

class JsonImporter(private val jsonMesh: JsonMesh) {
    private val appKeyImports = HashSet<AppKeyImport>()
    fun import() {
        val importer = createImporter()
        BluetoothMesh.getInstance().clearDatabase()

        importer.performImport()
    }
    private fun createImporter(): Importer {
        val importer = Importer()
        importer.createNetwork(Converter.stringToUuid(jsonMesh.meshUUID!!))
            .apply { name = jsonMesh.meshName }
            .also {
                handleAppKeys()
                handleSubnets(it)
                handleNodes(it)
                handleProvisioners(it)
                handleScenes(it)
            }
        return importer
    }
    private fun handleAppKeys() {
        jsonMesh.appKeys.map { jsonAppKey ->
            AppKeyImport(
                jsonAppKey.index,
                Converter.hexToBytes(jsonAppKey.key),
                Converter.hexToBytes(jsonAppKey.oldKey))
                .apply { name = jsonAppKey.name }
        }.forEach { appKeyImports.add(it) }
    }
    private fun handleSubnets(networkImport: NetworkImport) {
        jsonMesh.netKeys.forEach { jsonNetKey ->
            val subnetImport = createSubnetImport(jsonNetKey, networkImport)
            handleGroups(subnetImport)
            subnetImport.createSubnetSecurity(
                jsonNetKey.phase,
                Converter.timestampToLong(jsonNetKey.timestamp) ?: 0)
        }
    }
    private fun createSubnetImport(jsonNetKey: JsonNetKey, networkImport: NetworkImport):
            SubnetImport {
        val netKeyImport = NetKeyImport(
            jsonNetKey.index,
            Converter.hexToBytes(jsonNetKey.key),
            Converter.hexToBytes(jsonNetKey.oldKey))
        return networkImport.createSubnet(netKeyImport)
            .apply { name = jsonNetKey.name }
    }
    private fun handleGroups(subnetImport: SubnetImport) {
        jsonMesh.groups.filter { jsonGroup ->
            val appKey = jsonMesh.appKeys.find { it.index == jsonGroup.appKeyIndex }
            appKey?.boundNetKey == subnetImport.netKey.index
        }.forEach { createGroupImport(subnetImport, it) }
        handleParentGroups(jsonMesh.groups, subnetImport.groups)
    }
    private fun createGroupImport(subnetImport: SubnetImport, jsonGroup: JsonGroup) {
        if (Converter.isVirtualAddress(jsonGroup.address!!)) {
// Creating groups with virtual addresses is not supported currently
        } else {
            val groupImport = subnetImport.createGroup(
                Converter.hexToInt(jsonGroup.address)!!,
                findAppKeyImport(jsonGroup.appKeyIndex))
            groupImport.name = jsonGroup.name
        }
    }
    private fun findAppKeyImport(appKeyIndex: Int): AppKeyImport? {
        return appKeyImports.find { it.index == appKeyIndex }
    }
    private fun handleParentGroups(jsonGroups: Array<JsonGroup>, groupImports: Set<GroupImport>) {
        jsonGroups.forEach { jsonGroup ->
            jsonGroup.parentAddress?.let { parentGroupAddress ->
                jsonGroups.find { it.address == parentGroupAddress }?.let {
                    val child = findGroupImport(jsonGroup, groupImports)
                    val parent = findGroupImport(it, groupImports)
                    child?.parentGroup = parent
                }
            }
        }
    }
    private fun findGroupImport(jsonGroup: JsonGroup, groupImports: Set<GroupImport>): GroupImport?
    {
        return groupImports.find { Converter.hexToInt(jsonGroup.address) == it.address }
    }
    private fun handleNodes(networkImport: NetworkImport) {
        jsonMesh.nodes.forEach { jsonNode ->
            val nodeImport = createNodeImport(networkImport, jsonNode)
            fillNodeSubnets(networkImport, nodeImport, jsonNode)
            fillNodeGroups(nodeImport, jsonNode)
            handleElements(nodeImport, nodeImport.groups, jsonNode)
            handleDeviceCompositionData(nodeImport, jsonNode)
            handleNodeSettings(nodeImport.settings, jsonNode)
            handleNodeSecurity(nodeImport.security, jsonNode)
        }
    }
    private fun createNodeImport(networkImport: NetworkImport, jsonNode: JsonNode): NodeImport {
        return networkImport.createNode(
            Converter.hexToBytes(jsonNode.UUID),
            Converter.hexToInt(jsonNode.unicastAddress)!!,
            DevKeyImport(Converter.hexToBytes(jsonNode.deviceKey)))
            .apply { name = jsonNode.name }
    }
    private fun fillNodeSubnets(networkImport: NetworkImport, nodeImport: NodeImport, jsonNode:
    JsonNode) {
        networkImport.subnets
            .filter { subnet ->
                jsonNode.netKeys.any { subnet.netKey.index == it.index }
            }.forEach { nodeImport.addSubnet(it) }
    }
    private fun fillNodeGroups(nodeImport: NodeImport, jsonNode: JsonNode) {
        nodeImport.subnets.flatMap { it.groups }
            .filter { group ->
                jsonNode.knownAddresses?.any { Converter.hexToInt(it) == group.address } !=
                        false
                        && jsonNode.appKeys.any { it.index == group.appKey.index }
            }.forEach { nodeImport.addGroup(it) }
    }
    private fun handleElements(nodeImport: NodeImport, nodeImportGroups: Set<GroupImport>,
                               jsonNode: JsonNode) {
        jsonNode.elements.forEach {
            val elementImport = nodeImport
                .createElement(it.index, Converter.hexToInt(it.location)!!)
                .apply { name = it.name }
            handleModels(elementImport, nodeImportGroups, it)
        }
    }
    private fun handleModels(elementImport: ElementImport, nodeImportGroups: Set<GroupImport>,
                             jsonElement: JsonElement) {
        jsonElement.models.forEach {
            val modelImport = elementImport.createModel(Converter.hexToInt(it.modelId)!!)
            fillModelGroups(modelImport, nodeImportGroups, it)
            handleModelSettings(modelImport.settings, it)
        }
    }
    private fun fillModelGroups(modelImport: ModelImport, nodeImportGroups: Set<GroupImport>,
                                jsonModel: JsonModel) {
        nodeImportGroups
            .filter { group ->
                jsonModel.knownAddresses?.any { Converter.hexToInt(it) == group.address } !=
                        false
                        && jsonModel.bind.contains(group.appKey.index)
            }.forEach { modelImport.addGroup(it) }
    }
    private fun handleModelSettings(modelSettingsImport: ModelSettingsImport, jsonModel: JsonModel)
    {
        modelSettingsImport.apply {
            handlePublish(this, jsonModel.publish)
            jsonModel.subscribe.forEach { address ->
                if (Converter.isVirtualAddress(address)) {
                    createSubscription(Converter.hexToBytes(address))
                } else {
                    createSubscription(Converter.hexToInt(address)!!)
                }
            }
        }
    }
    private fun handlePublish(modelSettingsImport: ModelSettingsImport, jsonPublish: JsonPublish?)
    {
        jsonPublish?.let {
            modelSettingsImport.createPublish().apply {
                ttl = it.ttl
                period = it.period
                credentials = Credentials.fromValue(it.credentials)
                if (Converter.isVirtualAddress(it.address!!)) {
                    createAddress(Converter.hexToBytes(it.address))
                } else {
                    createAddress(Converter.hexToInt(it.address)!!)
                }
                it.retransmit?.let { createRetransmit(it.count, it.interval) }

            }
        }
    }
    private fun handleDeviceCompositionData(nodeImport: NodeImport, jsonNode: JsonNode) {
        jsonNode.features?.let { jsonFeatures ->
            val isEveryFieldNotNull = jsonFeatures.friend != null && jsonFeatures.relay != null &&
                    jsonFeatures.proxy != null && jsonFeatures.lowPower != null &&
                    jsonNode.cid != null && jsonNode.pid != null && jsonNode.vid != null &&
                    jsonNode.crpl != null
            if (isEveryFieldNotNull) {
                val deviceCompositionData = nodeImport.createDeviceCompositionData()
                deviceCompositionData.supportsRelay = isFeatureSupported(jsonFeatures.relay)
                deviceCompositionData.supportsProxy = isFeatureSupported(jsonFeatures.proxy)
                deviceCompositionData.supportsFriend = isFeatureSupported(jsonFeatures.friend)
                deviceCompositionData.supportsLowPower = isFeatureSupported(jsonFeatures.lowPower)
                deviceCompositionData.cid = Converter.hexToInt(jsonNode.cid)
                deviceCompositionData.pid = Converter.hexToInt(jsonNode.pid)
                deviceCompositionData.vid = Converter.hexToInt(jsonNode.vid)
                deviceCompositionData.crpl = Converter.hexToInt(jsonNode.crpl)
            }
        }
    }
    private fun isFeatureSupported(featureState: Int?): Boolean? {
        return when (featureState) {
            0, 1 -> true
            2 -> false
            else -> null
        }
    }
    private fun handleNodeSettings(nodeSettings: NodeSettingsImport, jsonNode: JsonNode) {
        nodeSettings.isConfigComplete = jsonNode.configComplete
        nodeSettings.defaultTTL = jsonNode.defaultTTL
        jsonNode.features?.let { handleFeatures(nodeSettings.createFeatures(), it) }
        jsonNode.networkTransmit?.let { nodeSettings.createNetworkTransmit(it.count, it.interval) }
        jsonNode.relayRetransmit?.let { nodeSettings.createRelayRetransmit(it.count, it.interval) }
    }
    private fun handleFeatures(featuresImport: FeaturesImport, jsonFeature: JsonFeature) {
        featuresImport.isRelayEnabled = isFeatureEnabled(jsonFeature.relay)
        featuresImport.isProxyEnabled = isFeatureEnabled(jsonFeature.proxy)
        featuresImport.isFriendEnabled = isFeatureEnabled(jsonFeature.friend)
        featuresImport.isLowPower = isFeatureEnabled(jsonFeature.lowPower)
    }
    private fun isFeatureEnabled(featureState: Int?): Boolean? {
        return when (featureState) {
            0 -> false
            1 -> true
            else -> null
        }
    }
    private fun handleNodeSecurity(nodeSecurity: NodeSecurityImport, jsonNode: JsonNode) {
        jsonNode.appKeys.forEach { nodeSecurity.createNodeAppKey(it.index, it.updated) }
        jsonNode.netKeys.forEach { nodeSecurity.createNodeNetKey(it.index, it.updated) }
        nodeSecurity.isBlacklisted = jsonNode.blacklisted
        nodeSecurity.security = Security.valueOf(jsonNode.security!!.toUpperCase(Locale.ROOT))
        nodeSecurity.isSecureNetworkBeacon = jsonNode.secureNetworkBeacon
    }
    private fun handleProvisioners(networkImport: NetworkImport) {
        jsonMesh.provisioners.forEach { jsonProvisioner ->
            networkImport.createProvisioner(Converter.stringToUuid(jsonProvisioner.UUID!!))
                .apply { name = jsonProvisioner.provisionerName }
                .also {
                    handleUnicastRanges(it, jsonProvisioner.allocatedUnicastRange)
                    handleGroupRanges(it, jsonProvisioner.allocatedGroupRange)
                    handleSceneRanges(it, jsonProvisioner.allocatedSceneRange)
                }
        }
    }
    private fun handleUnicastRanges(provisionerImport: ProvisionerImport, jsonUnicastRanges:
    Array<JsonAddressRange>) {
        jsonUnicastRanges.forEach {
            val low = Converter.hexToInt(it.lowAddress)!!
            val high = Converter.hexToInt(it.highAddress)!!
            provisionerImport.createUnicastRange(low, high)
        }
    }
    private fun handleGroupRanges(provisionerImport: ProvisionerImport, jsonGroupRanges:
    Array<JsonAddressRange>) {
        jsonGroupRanges.forEach {
            val low = Converter.hexToInt(it.lowAddress)!!
            val high = Converter.hexToInt(it.highAddress)!!
            provisionerImport.createGroupRange(low, high)
        }
    }
    private fun handleSceneRanges(provisionerImport: ProvisionerImport, jsonSceneRanges:
    Array<JsonSceneRange>) {
        jsonSceneRanges.forEach {
            val low = Converter.hexToInt(it.firstScene)!!
            val high = Converter.hexToInt(it.lastScene)!!
            provisionerImport.createSceneRange(low, high)
        }
    }
    private fun handleScenes(networkImport: NetworkImport) {
        jsonMesh.scenes.forEach { jsonScene ->
            val sceneImport = networkImport
                .createScene(Converter.hexToInt(jsonScene.number)!!)
                .apply { name = jsonScene.name }
            jsonScene.addresses.mapNotNull { address ->
                networkImport.nodes.find {
                    it.primaryElementAddress == Converter.hexToInt(address)
                }
            }.forEach { sceneImport.addNode(it) }
        }
    }
}
