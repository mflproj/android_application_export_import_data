package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport.JsonObjects

class JsonProvisioner {
    var provisionerName: String? = null
    var UUID: String? = null
    var allocatedUnicastRange = arrayOf<JsonAddressRange>()
    var allocatedGroupRange = arrayOf<JsonAddressRange>()
    var allocatedSceneRange = arrayOf<JsonSceneRange>()
}