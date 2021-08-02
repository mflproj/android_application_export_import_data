/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.MainFragment

import android.content.Intent

interface MainFragmentView {

    fun setView()

    fun setEnablingButtons()

    fun showShareKeysIntent(intent: Intent)

    fun showSaveKeysIntent(intent: Intent)
}