package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.isMobileNumber
import com.rummytitans.sdk.cardgame.utils.validEmailTestCase
import com.rummytitans.sdk.cardgame.utils.validPasswordTestCases
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Instrumented test for login screen, which will execute on an Android device.
 */

class LoginTestCases {

    @Test
    fun validEmailCase() {
        assertTrue(validEmailTestCase("name@email.com"))
    }

    @Test
    fun validEmailWithSubDomain() {
        assertTrue(validEmailTestCase("name@email.co.uk"))
    }

    @Test
    fun invalidEmailWithoutDomain() {
        assertFalse(validEmailTestCase("name@email"))
    }

    @Test
    fun invalidEmailWithDoubleDot() {
        assertFalse(validEmailTestCase("name@email..com"))
    }

    @Test
    fun invalidEmailWithoutUserName() {
        assertFalse(validEmailTestCase("@email.com"))
    }

    @Test
    fun invalidEmailWithEmpty() {
        assertFalse(validEmailTestCase(""))
    }


    @Test
    fun blankPassword() {
        assertFalse(validPasswordTestCases(""))
    }

    @Test
    fun lessDigitPassword() {
        assertFalse(validPasswordTestCases("12121"))
    }

    @Test
    fun validPassword() {
        assertTrue(validPasswordTestCases("1234567890"))
    }

    @Test
    fun checkBlankMobileNo() {
        assertFalse(isMobileNumber(""))
    }

    @Test
    fun textAsMobileNo() {
        assertFalse(isMobileNumber("asfda"))
    }

    @Test
    fun validMobileNo() {
        assertTrue(isMobileNumber("1234567890"))
    }

}
