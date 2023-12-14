package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.validContestCodeTestCases
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class PrivateContestCodeTestCases {

    @Test
    fun validPrivateContestCode() {
        assertTrue(validContestCodeTestCases("Myteam11Redeem"))
    }

    @Test
    fun blankInvalidPrivateContestCode() {
        assertFalse(validContestCodeTestCases(""))
    }

    @Test
    fun invalidPrivateContestCode() {
        assertFalse(validContestCodeTestCases("asd"))
    }

}