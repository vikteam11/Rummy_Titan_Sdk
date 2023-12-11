package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Instrumented test for login screen, which will execute on an Android device.
 */

class RegistrantionTestCases {

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
    fun validPassword() {
        assertTrue(passwordPolicyTestCases("abcdefg@123"))
    }

    @Test
    fun invalidPasswordOnlyNumber() {
        assertFalse(passwordPolicyTestCases("45612123"))
    }

    @Test
    fun invalidPasswordOnlyAlphabet() {
        assertFalse(passwordPolicyTestCases("abcdefghi"))
    }

    @Test
    fun invalidPasswordEmpty() {
        assertFalse(passwordPolicyTestCases(""))
    }

    @Test
    fun blankReferCode() {
        assertTrue(validReferCodeTestCase(""))
    }

    @Test
    fun lessDigitsReferCode() {
        assertFalse(validReferCodeTestCase("121"))
    }

    @Test
    fun validRefercode() {
        assertTrue(validReferCodeTestCase("123456"))
    }

    @Test
    fun blankDigitsPassword() {
        assertFalse(passwordDigitTestCase(""))
    }

    @Test
    fun containsDigitsPassword() {
        assertTrue(passwordDigitTestCase("123"))
    }

    @Test
    fun blankAlphabetsPassword() {
        assertFalse(passwordAlphabateTestCases(""))
    }

    @Test
    fun containsAlphabetsPassword() {
        assertTrue(passwordAlphabateTestCases("myteam"))
    }

    @Test
    fun noSpacialSymbolsPassword() {
        assertFalse(passwordSpecialSymbolTestCases(""))
    }

    @Test
    fun containsSymbolsPassword() {
        assertTrue(passwordSpecialSymbolTestCases("@#$%^&!?"))
    }

    @Test
    fun blankPassword() {
        assertFalse(validPasswordTestCases(""))
    }

    @Test
    fun lessDigitPassword() {
        assertFalse(validPasswordTestCases("1212"))
    }

    @Test
    fun ValidPasswordByLength() {
        assertTrue(passwordLengthTestCases("1234567890"))
    }

}
