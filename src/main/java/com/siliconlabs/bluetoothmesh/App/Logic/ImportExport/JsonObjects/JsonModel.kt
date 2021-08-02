package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects

class JsonModel {
    var modelId: String? = null
    var subscribe: Array<String> = emptyArray()
    var publish: JsonPublish? = null
    var bind: Array<Int> = emptyArray()
    var knownAddresses: Array<String>? = null
}