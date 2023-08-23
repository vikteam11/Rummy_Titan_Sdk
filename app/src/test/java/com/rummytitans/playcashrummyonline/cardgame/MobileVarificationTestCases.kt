package com.rummytitans.playcashrummyonline.cardgame

import com.rummytitans.playcashrummyonline.cardgame.utils.validMobileTestCases
import com.rummytitans.playcashrummyonline.cardgame.utils.validOTPTestCases
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class MobileVarificationTestCases {

    @Test
    fun blankMobileNo() {
        assertFalse(validMobileTestCases(""))
    }

    @Test
    fun lessDigitsMobileNo() {
        assertFalse(validMobileTestCases("12121212"))
    }

    @Test
    fun maxDigitsMobileNo() {
        assertFalse(validMobileTestCases("121212121212"))
    }

    @Test
    fun validMobileNo() {
        assertTrue(validMobileTestCases("1234567890"))
    }

    @Test
    fun blankOtp() {
        assertFalse(validOTPTestCases(""))
    }

    @Test
    fun longOtp() {
        assertFalse(validOTPTestCases("122324323423424234"))
    }

    @Test
    fun validOtp() {
        assertTrue(validOTPTestCases("123456"))
    }

    @Test
    fun mixTextOtp() {
        assertFalse(validOTPTestCases("1234qw"))
    }
}
