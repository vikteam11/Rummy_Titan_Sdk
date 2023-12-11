package com.rummytitans.sdk.cardgame

import com.rummytitans.sdk.cardgame.utils.isEmptyString
import com.rummytitans.sdk.cardgame.utils.validPancard
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class PanCardVarificationTextCases {

    @Test
    fun validPanCard() {
        assertTrue(validPancard("adada1234a"))
    }

    @Test
    fun inValidPanCard() {
        assertFalse(validPancard("adad1234aa"))
    }

    @Test
    fun blankPanCard() {
        assertFalse(validPancard(""))
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
      fun invalidDOB() {
          assertTrue(isEmptyString(""))
      }

      @Test
      fun validDOB() {
          assertTrue(!isEmptyString("1/12/1999"))
      }


      @Test
      fun invalidState() {
          assertTrue(isEmptyString(""))
      }

      @Test
      fun validSate() {
          assertTrue(!isEmptyString("fbhfdhdfhfg"))
      }

}