/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.Scene

import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.SceneStatusCode
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.response.SceneRegisterStatus
import com.siliconlab.bluetoothmesh.adk.functionality_control.scene.response.SceneStatus
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.DeviceListAdapter
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic
import com.siliconlabs.bluetoothmesh.App.Fragments.DeviceList.Presenters.DeviceListAdapterLogic.Companion.getMeshElementControl
import com.siliconlabs.bluetoothmesh.App.ModelView.MeshNode
import com.siliconlabs.bluetoothmesh.App.Models.MeshElementControl
import com.siliconlabs.bluetoothmesh.App.Models.TransactionId

class DeviceListAdapterSceneLogic(val listener: DeviceListAdapterLogic.DeviceListAdapterLogicListener) {
    private val acknowledged = true

    fun refreshSceneStatus(meshNode: MeshNode, refreshNodeListener: DeviceListAdapter.DeviceListAdapterListener.RefreshNodeListener) {
        val meshElementControl = getMeshElementControl(meshNode)
        refreshNodeListener.startRefresh()
        meshElementControl?.getSceneRegister(object : MeshElementControl.SceneRegisterStatusCallback {
            override fun success(sceneRegisterStatus: SceneRegisterStatus?) {
                refreshNodeListener.stopRefresh()
                if (sceneRegisterStatus!!.statusCode == SceneStatusCode.SUCCESS) {
                    if (sceneRegisterStatus.scenes.contains(1)) {
                        meshNode.sceneOneStatus = MeshNode.SceneStatus.STORED
                    } else {
                        meshNode.sceneOneStatus = MeshNode.SceneStatus.NOT_STORED
                    }
                    if (sceneRegisterStatus.scenes.contains(2)) {
                        meshNode.sceneTwoStatus = MeshNode.SceneStatus.STORED
                    } else {
                        meshNode.sceneTwoStatus = MeshNode.SceneStatus.NOT_STORED
                    }
                    changeActiveScene(meshNode, sceneRegisterStatus.currentScene)
                    listener.notifyItemChanged(meshNode)
                } else {
                    listener.showToast(sceneRegisterStatus.statusCode)
                }
            }

            override fun error(error: ErrorType) {
                refreshNodeListener.stopRefresh()
                listener.showToast(error)
            }
        })
    }

    fun storeScene(meshNode: MeshNode, sceneNumber: Int) {
        val meshElementControl = getMeshElementControl(meshNode)
        meshElementControl?.storeScene(acknowledged, sceneNumber, object : MeshElementControl.SceneRegisterStatusCallback {
            override fun success(sceneRegisterStatus: SceneRegisterStatus?) {
                listener.showToast(sceneRegisterStatus!!.statusCode)
                if (sceneRegisterStatus.statusCode == SceneStatusCode.SUCCESS) {
                    changeActiveScene(meshNode, sceneNumber)

                    listener.notifyItemChanged(meshNode)
                }
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    fun recallScene(meshNode: MeshNode, sceneNumber: Int) {
        val meshElementControl = getMeshElementControl(meshNode)
        meshElementControl?.recallScene(acknowledged, sceneNumber, transactionId.next(), null, null, object : MeshElementControl.SceneStatusCallback {
            override fun success(sceneStatus: SceneStatus?) {
                listener.showToast(sceneStatus!!.statusCode)
                if (sceneStatus.statusCode == SceneStatusCode.SUCCESS) {
                    changeActiveScene(meshNode, sceneNumber)
                    listener.notifyItemChanged(meshNode)
                }
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    fun deleteScene(meshNode: MeshNode, sceneNumber: Int) {
        val meshElementControl = getMeshElementControl(meshNode)
        meshElementControl?.deleteScene(acknowledged, sceneNumber, object : MeshElementControl.SceneRegisterStatusCallback {
            override fun success(sceneRegisterStatus: SceneRegisterStatus?) {
                listener.showToast(sceneRegisterStatus!!.statusCode)

                if (sceneRegisterStatus.statusCode == SceneStatusCode.SUCCESS) {
                    when (sceneNumber) {
                        1 -> {
                            meshNode.sceneOneStatus = MeshNode.SceneStatus.NOT_STORED
                        }
                        2 -> {
                            meshNode.sceneTwoStatus = MeshNode.SceneStatus.NOT_STORED
                        }
                    }
                    listener.notifyItemChanged(meshNode)
                }
            }

            override fun error(error: ErrorType) {
                listener.showToast(error)
            }
        })
    }

    private fun changeActiveScene(meshNode: MeshNode, sceneNumber: Int) {
        when (sceneNumber) {
            1 -> {
                meshNode.sceneOneStatus = MeshNode.SceneStatus.ACTIVE
                if (meshNode.sceneTwoStatus == MeshNode.SceneStatus.ACTIVE) {
                    meshNode.sceneTwoStatus = MeshNode.SceneStatus.STORED
                }
            }
            2 -> {
                meshNode.sceneTwoStatus = MeshNode.SceneStatus.ACTIVE
                if (meshNode.sceneOneStatus == MeshNode.SceneStatus.ACTIVE) {
                    meshNode.sceneOneStatus = MeshNode.SceneStatus.STORED
                }
            }
        }
    }

    companion object {
        private var transactionId = TransactionId()
    }
}