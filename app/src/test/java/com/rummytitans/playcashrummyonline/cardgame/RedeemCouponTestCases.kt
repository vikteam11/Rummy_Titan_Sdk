package com.rummytitans.playcashrummyonline.cardgame

import com.rummytitans.playcashrummyonline.cardgame.utils.validRedemCodeTestCases
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class RedeemCouponTestCases {

    @Test
    fun validReddemCode() {
        assertTrue(validRedemCodeTestCases("Myteam11Redeem"))
    }

    @Test
    fun blankInvalidRedeemCode() {
        assertFalse(validRedemCodeTestCases(""))
    }

    @Test
    fun invalidRedeemCode() {
        assertFalse(validRedemCodeTestCases("asd"))
    }

}
