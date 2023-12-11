package com.rummytitans.sdk.cardgame.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var arrFragments = ArrayList<Fragment>()
    var mTitles: ArrayList<String>? = null

    constructor(fm: FragmentManager, mList: List<String>, frag: ArrayList<Fragment>) : this(fm) {
        arrFragments.clear()
        arrFragments = frag
        mTitles = mList as ArrayList<String>
    }

    override fun getCount(): Int = mTitles?.size ?: 0

    override fun destroyItem(arg0: View, arg1: Int, arg2: Any) {
        (arg0 as? ViewPager)?.removeView(arg2 as View)
    }

    override fun getItem(position: Int) = arrFragments[position]

    override fun getPageTitle(position: Int): CharSequence? = mTitles?.get(position) ?: ""
}