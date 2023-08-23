package com.rummytitans.playcashrummyonline.cardgame

import com.rummytitans.playcashrummyonline.cardgame.utils.validAmount
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Instrumented test for login screen, which will execute on an Android device.
 */

class WithdrawAmmountTestCases {

    @Test
    fun validWithdrawAmmount() {
        assertTrue(validAmount("12"))
    }

    @Test
    fun blankInvalidWithdrawAmmount() {
        assertFalse(validAmount(""))
    }

    @Test
    fun invalidWithdrawAmmount() {
        assertFalse(validAmount("."))
    }
}
