package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects

class JsonNode {
    var UUID: String? = null
    var unicastAddress: String? = null
    var deviceKey: String? = null
    var security: String? = null
    var netKeys: Array<JsonNodeKey> = emptyArray()
    var configComplete: Boolean = false
    var name: String? = null
    var cid: String? = null
    var pid: String? = null
    var vid: String? = null
    var crpl: String? = null
    var features: JsonFeature? = null
    var secureNetworkBeacon: Boolean? = null
    var defaultTTL: Int? = null
    var networkTransmit: JsonNetworkTransmit? = null
    var relayRetransmit: JsonRelayRetransmit? = null
    var appKeys: Array<JsonNodeKey> = emptyArray()
    var elements: Array<JsonElement> = emptyArray()
    var blacklisted: Boolean = false
    var knownAddresses: Array<String>? = null
}