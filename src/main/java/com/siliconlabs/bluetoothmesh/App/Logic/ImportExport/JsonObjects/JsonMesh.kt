package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects

import com.google.gson.annotations.SerializedName
class JsonMesh {
    @SerializedName("\$schema")
    var id: String? = null
    var schema: String? = null
    var version: String? = null
    var meshUUID: String? = null
    var meshName: String? = null
    var timestamp: String? = null
    var provisioners: Array<JsonProvisioner> = emptyArray()
    var netKeys: Array<JsonNetKey> = emptyArray()
    var appKeys: Array<JsonAppKey> = emptyArray()
    var nodes: Array<JsonNode> = emptyArray()

    var groups: Array<JsonGroup> = emptyArray()
    var scenes: Array<JsonScene> = emptyArray()
}
