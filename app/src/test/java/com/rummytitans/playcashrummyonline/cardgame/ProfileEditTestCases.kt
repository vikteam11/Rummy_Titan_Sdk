package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.isEmptyString
import com.rummytitans.sdk.cardgame.utils.validPinCode
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class ProfileEditTestCases {

    @Test
    fun blankName() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun ValidName() {
        assertTrue(!isEmptyString("Avinash"))
    }


    @Test
    fun blankDOB() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun ValidDOB() {
        assertTrue(!isEmptyString("12-12-12"))
    }


    @Test
    fun blankGender() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun ValidGender() {
        assertTrue(!isEmptyString("G"))
    }

    @Test
    fun blankAddress() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun ValidAddress() {
        assertTrue(!isEmptyString("Avinash"))
    }

    @Test
    fun blankPINCode() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun inValidPINCode() {
        assertFalse(validPinCode("123"))
    }

    @Test
    fun ValidPinCode() {
        assertTrue(validPinCode("121212"))
    }

    @Test
    fun blankState() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun ValidSate() {
        assertTrue(!isEmptyString("Avinash"))
    }


}

