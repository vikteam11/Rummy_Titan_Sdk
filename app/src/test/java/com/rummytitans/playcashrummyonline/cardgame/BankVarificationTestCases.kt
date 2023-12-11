package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.isEmptyString
import com.rummytitans.sdk.cardgame.utils.validIfscCodetestCases
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class BankVarificationTestCases {
    @Test
    fun validIFSCCode() {
        assertTrue(validIfscCodetestCases("uia12121212"))
    }

    @Test
    fun inValidIFSCCode() {
        assertFalse(validIfscCodetestCases("afdfd"))
    }

    @Test
    fun longDigitIFSCCode() {
        assertFalse(validIfscCodetestCases("12sdfgkjkjkljlm12"))
    }

    @Test
    fun blankIFSCCode() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun nullIFSCCode() {
        assertTrue(isEmptyString(null))
    }


    @Test
    fun invalidImgUrl() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun validImgUrl() {
        assertTrue(!isEmptyString("fbhfdhdfhfg"))
    }

    @Test
    fun invalidName() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun validName() {
        assertTrue(!isEmptyString("fbhfdhdfhfg"))
    }


    @Test
    fun invalidNumber() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun validNumber() {
        assertTrue(!isEmptyString("fbhfdhdfhfg"))
    }


    @Test
    fun invalidBranchName() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun validBranchName() {
        assertTrue(!isEmptyString("fbhfdhdfhfg"))
    }


    @Test
    fun invalidBankBranch() {
        assertTrue(isEmptyString(""))
    }

    @Test
    fun validBankBranch() {
        assertTrue(!isEmptyString("fbhfdhdfhfg"))
    }
}