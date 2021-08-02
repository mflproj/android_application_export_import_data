/*
 * Copyright © 2020 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Models

class TransactionId {

    var currentValue = MIN_VALUE
        private set(value) {
            field = if (value > MAX_VALUE) MIN_VALUE else value
        }

    fun next(): Int {
        return ++currentValue
    }

    companion object {
        const val MIN_VALUE = 0
        const val MAX_VALUE = 255
    }
}