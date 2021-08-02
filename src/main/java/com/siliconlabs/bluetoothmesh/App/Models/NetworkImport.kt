package com.siliconlabs.bluetoothmesh.App.Models

import java.util.*

//Data structure for storing IV index value for network to be imported into database
class NetworkImport(
    var uuid: UUID,
    var fourIndex: Int,
    var provisionerAddress: Int
)
