package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.validAmount
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Instrumented test for login screen, which will execute on an Android device.
 */

class AddCashTestCases {

    @Test
    fun validAddCash() {
        assertTrue(validAmount("12"))
    }

    @Test
    fun blankInvalidAddCash() {
        assertFalse(validAmount(""))
    }

    @Test
    fun invalidAddCash() {
        assertFalse(validAmount("."))
    }
}
