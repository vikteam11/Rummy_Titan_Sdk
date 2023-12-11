package com.rummytitans.sdk.cardgame.bubbleview

import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCaseBuilder.Companion.Favourite_Home
import android.app.Activity
import android.content.Context

class BubbleShowCaseSequence {
    private val mBubbleShowCaseBuilderList = ArrayList<BubbleShowCaseBuilder>()
    private var onSkipInterface: OnSkipInterface? = null

    var currentPosition = 0

    init {
        mBubbleShowCaseBuilderList.clear()
    }

    fun addShowCase(bubbleShowCaseBuilder: BubbleShowCaseBuilder): BubbleShowCaseSequence {
        bubbleShowCaseBuilder.setListner(onSkipInterface)
        mBubbleShowCaseBuilderList.add(bubbleShowCaseBuilder)
        return this
    }

    fun addShowCases(bubbleShowCaseBuilderList: List<BubbleShowCaseBuilder>): BubbleShowCaseSequence {
        mBubbleShowCaseBuilderList.addAll(bubbleShowCaseBuilderList)
        return this
    }

    fun show() {
    //hide show case as version 119.
    //show(0)
    }

    fun getShowCase() = mBubbleShowCaseBuilderList[currentPosition]

    private fun show(position: Int) {
        currentPosition = position
        if (position >= mBubbleShowCaseBuilderList.size) {
            onSkipInterface?.onSkipClick()
            return
        }

        when (position) {
            0 -> {
                mBubbleShowCaseBuilderList[position].isFirstOfSequence(true)
                mBubbleShowCaseBuilderList[position].isLastOfSequence(false)
            }
            mBubbleShowCaseBuilderList.size - 1 -> {
                mBubbleShowCaseBuilderList[position].isFirstOfSequence(false)
                mBubbleShowCaseBuilderList[position].isLastOfSequence(true)
            }
            else -> {
                mBubbleShowCaseBuilderList[position].isFirstOfSequence(false)
                mBubbleShowCaseBuilderList[position].isLastOfSequence(false)
            }
        }
        //for those where only one tutorial
        if (mBubbleShowCaseBuilderList.size==1){
            mBubbleShowCaseBuilderList[position].isFirstOfSequence(true)
            mBubbleShowCaseBuilderList[position].isLastOfSequence(true)
        }
        mBubbleShowCaseBuilderList[position].sequenceListener(object :
            BubbleShowCase.SequenceShowCaseListener {
            override fun onDismiss() {
                show(position + 1)
            }
        }).show()
    }

    fun savedShowcaseInPreference(activity: Activity?,id:String){
        activity?.getSharedPreferences("BubbleShowCasePrefs", Context.MODE_PRIVATE)?.let {mPrefs->
            mPrefs.edit().apply{
                putString(id, id)
                apply()
            }
        }
    }

    fun saveAllShowCaseIds(activity:Activity){
        activity.getSharedPreferences("BubbleShowCasePrefs", Context.MODE_PRIVATE)?.let {mPrefs->
            mPrefs.edit().apply{
                putString(Favourite_Home,Favourite_Home)
                apply()
            }
        }
    }

    fun isShowCasePreviouslyShowed(activity:Activity,id:String):Boolean {
        val mPrefs = activity.getSharedPreferences("BubbleShowCasePrefs", Context.MODE_PRIVATE)
        return  mPrefs.getString(id,null)!=null
    }
    fun setSkipListener(onSkipInterface: OnSkipInterface) {
        this.onSkipInterface = onSkipInterface
    }

    interface OnSkipInterface {
        fun onSkipClick()
        fun onNextClick()
    }

}