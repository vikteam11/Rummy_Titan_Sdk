package com.rummytitans.sdk.cardgame.bubbleview


import com.rummytitans.sdk.cardgame.R
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference


class BubbleShowCase(builder: BubbleShowCaseBuilder) {
    private val SHARED_PREFS_NAME = "BubbleShowCasePrefs"

    private val FOREGROUND_LAYOUT_ID = 731
    private val FOREGROUND_TEXTVIEW_ID = 732

    private val DURATION_SHOW_CASE_ANIMATION = 200 //ms
    private val DURATION_BACKGROUND_ANIMATION = 700 //ms
    private val DURATION_BEATING_ANIMATION = 700 //ms

    private val MAX_WIDTH_MESSAGE_VIEW_TABLET = 420 //dp

    /**
     * Enum class which corresponds to each valid position for the BubbleMessageView arrow
     */
    enum class ArrowPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * Highlight mode. It represents the way that the target view will be highlighted
     * - VIEW_LAYOUT: Default value. All the view box is highlighted (the rectangle where the view is contained). Example: For a TextView, all the element is highlighted (characters and background)
     * - VIEW_SURFACE: Only the view surface is highlighted, but not the background. Example: For a TextView, only the characters will be highlighted
     */
    enum class HighlightMode {
        VIEW_LAYOUT, VIEW_SURFACE
    }

    interface SequenceShowCaseListener {
        fun onDismiss()
    }


    private val mActivity: WeakReference<Activity> = builder.mActivity!!

    //BubbleMessageView params
    private val mImage: Drawable? = builder.mImage
    private val mTitle: String? = builder.mTitle
    private val mSubtitle: String? = builder.mSubtitle
    private val mCloseAction: Drawable? = builder.mCloseAction
    private val mBackgroundColor: Int? = builder.mBackgroundColor
    private val mTextColor: Int? = builder.mTextColor
    private val mTitleTextSize: Int? = builder.mTitleTextSize
    private val mSubtitleTextSize: Int? = builder.mSubtitleTextSize
    private val mShowOnce: String? = builder.mShowOnce
    private val mDisableTargetClick: Boolean = builder.mDisableTargetClick
    private val mDisableCloseAction: Boolean = builder.mDisableCloseAction
    private val mHighlightMode: BubbleShowCase.HighlightMode? = builder.mHighlightMode
    private val mArrowPositionList: MutableList<ArrowPosition> = builder.mArrowPositionList
    private val mTargetView: WeakReference<View>? = builder.mTargetView
    private val mBubbleShowCaseListener: BubbleShowCaseListener? = builder.mBubbleShowCaseListener

    private var onSkipListner: BubbleShowCaseSequence.OnSkipInterface? = null

    //Sequence params
    private val mSequenceListener: SequenceShowCaseListener? = builder.mSequenceShowCaseListener
    private val isFirstOfSequence: Boolean = builder.mIsFirstOfSequence!!
    private val isLastOfSequence: Boolean = builder.mIsLastOfSequence!!

    //References
    private var backgroundDimLayout: RelativeLayout? = null
    private var hideSkipButton = false

    private var bubbleMessageViewBuilder: BubbleMessageView.Builder? = null

    fun setlistner(onSkipInterface: BubbleShowCaseSequence.OnSkipInterface?) {
        this.onSkipListner = onSkipInterface
    }

    fun setHideSkipbutton(btnStatus: Boolean) {
        hideSkipButton = btnStatus
    }


    fun show() {
        if (mShowOnce != null) {
            if (isBubbleShowCaseHasBeenShowedPreviously(mShowOnce)) {
                notifyDismissToSequenceListener()
                return
            } else {
                registerBubbleShowCaseInPreferences(mShowOnce)
            }
        }

        val rootView = getViewRoot(mActivity.get()!!)
        backgroundDimLayout = getBackgroundDimLayout()
        setBackgroundDimListener(backgroundDimLayout)
        bubbleMessageViewBuilder = getBubbleMessageViewBuilder()

        if (mTargetView != null && mArrowPositionList.size <= 1) {
            //Wait until the end of the layout animation, to avoid problems with pending scrolls or view movements
            val handler = Handler()
            handler.postDelayed({
                if (mTargetView.get() == null) return@postDelayed
                val target = mTargetView.get()!!
                //If the arrow list is empty, the arrow position is set by default depending on the targetView position on the screen
                if (mArrowPositionList.isEmpty()) {
                    if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(
                            mActivity.get()!!,
                            target
                        )
                    ) mArrowPositionList.add(ArrowPosition.TOP) else mArrowPositionList.add(
                        ArrowPosition.BOTTOM
                    )
                    bubbleMessageViewBuilder = getBubbleMessageViewBuilder()
                }

                if (isVisibleOnScreen(target)) {
                    addTargetViewAtBackgroundDimLayout(target, backgroundDimLayout)
                    addBubbleMessageViewDependingOnTargetView(
                        target,
                        bubbleMessageViewBuilder!!,
                        backgroundDimLayout
                    )
                } else {
                    dismiss()
                }
            }, DURATION_BACKGROUND_ANIMATION.toLong())
        } else {
            addBubbleMessageViewOnScreenCenter(bubbleMessageViewBuilder!!, backgroundDimLayout)
        }
        if (isFirstOfSequence) {
            //Add the background dim layout above the root view
            val animation = AnimationUtils.getFadeInAnimation(0, DURATION_BACKGROUND_ANIMATION)

            backgroundDimLayout?.let {
                try {
                    rootView.addView(
                        AnimationUtils.setAnimationToView(
                            backgroundDimLayout!!,
                            animation
                        )
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    fun dismiss() {
        if (backgroundDimLayout != null && isLastOfSequence) {
            //Remove background dim layout if the BubbleShowCase is the last of the sequence
            finishSequence()
        } else {
            //Remove all the views created over the background dim layout waiting for the next BubbleShowCsse in the sequence
            backgroundDimLayout?.removeAllViews()
        }
        notifyDismissToSequenceListener()
    }

    fun finishSequence() {
        val rootView = getViewRoot(mActivity.get()!!)
        rootView.removeView(backgroundDimLayout)
        backgroundDimLayout = null
    }

    private fun notifyDismissToSequenceListener() {
        mSequenceListener?.let { mSequenceListener.onDismiss() }
    }

    private fun getViewRoot(activity: Activity): ViewGroup {
        val androidContent = activity.findViewById<ViewGroup>(android.R.id.content)
        return androidContent.parent.parent as ViewGroup
    }

    private fun getBackgroundDimLayout(): RelativeLayout {
        if (mActivity.get()!!.findViewById<RelativeLayout>(FOREGROUND_LAYOUT_ID) != null) {
            val view = mActivity.get()!!.findViewById<RelativeLayout>(FOREGROUND_LAYOUT_ID)

            val textView = TextView(mActivity.get()!!)
            textView.id = R.id.Coach_Skip
            textView.setTextColor(Color.WHITE)
            textView.text = "SKIP"
            textView.setBackgroundResource(R.drawable.bg_border_cool_gray)
            textView.backgroundTintList =
                ContextCompat.getColorStateList(textView.context, R.color.white)
            textView.textSize = 16f
            textView.gravity = Gravity.CENTER


            val textViewNext = TextView(mActivity.get()!!)
            textViewNext.id = R.id.Coach_Next
            textViewNext.setTextColor(Color.BLACK)
            textViewNext.text = "NEXT"
            textViewNext.setBackgroundResource(R.drawable.bg_joined_team)
            textViewNext.textSize = 16f
            textViewNext.gravity = Gravity.CENTER

            val params = RelativeLayout.LayoutParams(200, 100)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.bottomMargin = 150
            params.leftMargin = 25

            val param2 = RelativeLayout.LayoutParams(200, 100)
            param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            param2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            param2.bottomMargin = 150
            param2.rightMargin = 25

            textView.layoutParams = params
            textViewNext.layoutParams = param2

            textView.elevation = 15f

            textViewNext.elevation = 15f

            if (!hideSkipButton)
                view.addView(textView)

            if (!hideSkipButton)
                view.addView(textViewNext)
            textViewNext.setOnClickListener {
                dismiss()
                mBubbleShowCaseListener?.onCloseActionImageClick(this@BubbleShowCase)
            }

            textView.setOnClickListener {
                finishSequence()
                onSkipListner?.onSkipClick()
            }

            return view
        }

        val backgroundLayout = RelativeLayout(mActivity.get()!!)
        backgroundLayout.id = FOREGROUND_LAYOUT_ID
        backgroundLayout.layoutParams =
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        backgroundLayout.setBackgroundColor(
            ContextCompat.getColor(
                mActivity.get()!!,
                R.color.transparent_grey
            )
        )
        backgroundLayout.isClickable = true

        val textView = TextView(mActivity.get()!!)
        textView.id = R.id.Coach_Skip
        textView.setTextColor(Color.WHITE)
        textView.text = "SKIP"
        textView.setBackgroundResource(R.drawable.bg_border_cool_gray)
        textView.backgroundTintList =
            ContextCompat.getColorStateList(textView.context, R.color.white)
        textView.textSize = 16f
        textView.gravity = Gravity.CENTER


        val textViewNext = TextView(mActivity.get()!!)
        textViewNext.id = R.id.Coach_Next
        textViewNext.setTextColor(Color.BLACK)
        textViewNext.text = "NEXT"
        textViewNext.setBackgroundResource(R.drawable.bg_joined_team)
        textViewNext.textSize = 16f
        textViewNext.gravity = Gravity.CENTER

        val params = RelativeLayout.LayoutParams(200, 100)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        params.bottomMargin = 150
        params.leftMargin = 25

        val param2 = RelativeLayout.LayoutParams(200, 100)
        param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        param2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        param2.bottomMargin = 150
        param2.rightMargin = 25

        textView.layoutParams = params
        textViewNext.layoutParams = param2
        textView.elevation = 15f
        textViewNext.elevation = 15f


        if (!hideSkipButton)
            backgroundLayout.addView(textView)

        if (!hideSkipButton)
            backgroundLayout.addView(textViewNext)

        textViewNext.setOnClickListener {
            dismiss()
            mBubbleShowCaseListener?.onCloseActionImageClick(this@BubbleShowCase)
        }


        textView.setOnClickListener {
            finishSequence()
            onSkipListner?.onSkipClick()
        }

        return backgroundLayout
    }

    private fun setBackgroundDimListener(backgroundDimLayout: RelativeLayout?) {
        backgroundDimLayout?.setOnClickListener {
            mBubbleShowCaseListener?.onBackgroundDimClick(this)
        }
    }

    private fun getBubbleMessageViewBuilder(): BubbleMessageView.Builder {
        return BubbleMessageView.Builder()
            .from(mActivity.get()!!)
            .arrowPosition(mArrowPositionList)
            .backgroundColor(mBackgroundColor)
            .textColor(mTextColor)
            .titleTextSize(mTitleTextSize)
            .subtitleTextSize(mSubtitleTextSize)
            .title(mTitle)
            .subtitle(mSubtitle)
            .image(mImage)
            .closeActionImage(mCloseAction)
            .disableCloseAction(mDisableCloseAction)
            .listener(object : OnBubbleMessageViewListener {
                override fun onBubbleClick() {
                    Log.d("onBubbleClick", "onBubbleClick")
                    mBubbleShowCaseListener?.onBubbleClick(this@BubbleShowCase)
                }

                override fun onCloseActionImageClick() {
                    dismiss()
                    Log.d("onCloseActionImageClick", "onCloseActionImageClick")
                    mBubbleShowCaseListener?.onCloseActionImageClick(this@BubbleShowCase)
                }
            })
    }

    private fun isBubbleShowCaseHasBeenShowedPreviously(id: String): Boolean {
        val mPrefs = mActivity.get()!!.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        return getString(mPrefs, id) != null
    }

    private fun registerBubbleShowCaseInPreferences(id: String) {
        val mPrefs = mActivity.get()!!.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        setString(mPrefs, id, id)
    }

    private fun getString(mPrefs: SharedPreferences, key: String): String? {
        return mPrefs.getString(key, null)
    }

    private fun setString(mPrefs: SharedPreferences, key: String, value: String) {
        val editor = mPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }


    /**
     * This function takes a screenshot of the targetView, creating an ImageView from it. This new ImageView is also set on the layout passed by param
     */
    private fun addTargetViewAtBackgroundDimLayout(
        targetView: View?,
        backgroundDimLayout: RelativeLayout?
    ) {
        if (targetView == null) return

        val targetScreenshot = takeScreenshot(targetView, mHighlightMode)
        val targetScreenshotView = ImageView(mActivity.get()!!)
        targetScreenshotView.setImageBitmap(targetScreenshot)
        targetScreenshotView.setOnClickListener {
            if (!mDisableTargetClick)
                dismiss()
            mBubbleShowCaseListener?.onTargetClick(this)
        }

        val targetViewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        targetViewParams.setMargins(
            getXposition(targetView),
            getYposition(targetView),
            getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width),
            0
        )
        backgroundDimLayout?.addView(
            AnimationUtils.setBouncingAnimation(
                targetScreenshotView,
                0,
                DURATION_BEATING_ANIMATION
            ), targetViewParams
        )
    }

    /**
     * This function creates the BubbleMessageView depending the position of the target and the desired arrow position. This new view is also set on the layout passed by param
     */
    private fun addBubbleMessageViewDependingOnTargetView(
        targetView: View?,
        bubbleMessageViewBuilder: BubbleMessageView.Builder,
        backgroundDimLayout: RelativeLayout?
    ) {
        if (targetView == null) return
        val showCaseParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        when (bubbleMessageViewBuilder.mArrowPosition[0]) {
            ArrowPosition.LEFT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(mActivity.get()!!, targetView)) {
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        getYposition(targetView),
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width)
                        ) else 0,
                        0
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else {
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        0,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width)
                        ) else 0,
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView) - targetView.height
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.RIGHT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                if (ScreenUtils.isViewLocatedAtHalfTopOfTheScreen(mActivity.get()!!, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        getYposition(targetView),
                        getScreenWidth(mActivity.get()!!) - getXposition(targetView),
                        0
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        0,
                        getScreenWidth(mActivity.get()!!) - getXposition(targetView),
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView) - targetView.height
                    )
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.TOP -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                if (ScreenUtils.isViewLocatedAtHalfLeftOfTheScreen(mActivity.get()!!, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity.get()!!) - getXposition(targetView)
                        ) else 0,
                        0
                    )
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - targetView.width else 0,
                        0
                    )
                }
            }
            ArrowPosition.BOTTOM -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                if (false) {//if (ScreenUtils.isViewLocatedAtHalfLeftOfTheScreen(mActivity.get()!!, targetView)) {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - getMessageViewWidthOnTablet(
                            getScreenWidth(mActivity.get()!!) - getXposition(targetView)
                        ) else 0,
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView)
                    )
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(
                            getXposition(targetView)
                        ) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - targetView.width else 0,
                        150//getScreenHeight(mActivity.get()!!) - getYposition(targetView)
                    )
                }
            }
        }

        val bubbleMessageView = bubbleMessageViewBuilder.targetViewScreenLocation(
            RectF(
                getXposition(targetView).toFloat(),
                getYposition(targetView).toFloat(),
                getXposition(targetView).toFloat() + targetView.width,
                getYposition(targetView).toFloat() + targetView.height
            )
        )
            .build()

        bubbleMessageView.id = createViewId()
        val animation = AnimationUtils.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(
            AnimationUtils.setAnimationToView(
                bubbleMessageView,
                animation
            ), showCaseParams
        )
    }

    /**
     * This function creates a BubbleMessageView and it is set on the center of the layout passed by param
     */
    private fun addBubbleMessageViewOnScreenCenter(
        bubbleMessageViewBuilder: BubbleMessageView.Builder,
        backgroundDimLayout: RelativeLayout?
    ) {
        val showCaseParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        showCaseParams.addRule(RelativeLayout.CENTER_VERTICAL)
        val bubbleMessageView: BubbleMessageView = bubbleMessageViewBuilder.build()
        bubbleMessageView.id = createViewId()
        if (isTablet()) showCaseParams.setMargins(
            if (isTablet()) getScreenWidth(mActivity.get()!!) / 2 - ScreenUtils.dpToPx(
                MAX_WIDTH_MESSAGE_VIEW_TABLET
            ) / 2 else 0,
            0,
            if (isTablet()) getScreenWidth(mActivity.get()!!) / 2 - ScreenUtils.dpToPx(
                MAX_WIDTH_MESSAGE_VIEW_TABLET
            ) / 2 else 0,
            0
        )
        val animation = AnimationUtils.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(
            AnimationUtils.setAnimationToView(
                bubbleMessageView,
                animation
            ), showCaseParams
        )
    }

    private fun createViewId(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            View.generateViewId()
        } else {
            System.currentTimeMillis().toInt() / 1000
        }
    }

    private fun takeScreenshot(targetView: View, highlightMode: HighlightMode?): Bitmap? {
        if (highlightMode == null || highlightMode == HighlightMode.VIEW_LAYOUT)
            return takeScreenshotOfLayoutView(targetView)
        return takeScreenshotOfSurfaceView(targetView)
    }

    private fun takeScreenshotOfLayoutView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        val rootView = getViewRoot(mActivity.get()!!)
        val currentScreenView = rootView.getChildAt(0)
        currentScreenView.buildDrawingCache()

        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap.createBitmap(
                currentScreenView.drawingCache,
                getXposition(targetView),
                getYposition(targetView),
                targetView.width,
                targetView.height,
            )

            /* TRANSPARENT The View*/
            val canvas = Canvas(bitmap)
            val transparentPaint = Paint()
            transparentPaint.color = Color.TRANSPARENT
            transparentPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawPaint(transparentPaint);
            targetView.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            bitmap = null
        }
        currentScreenView.isDrawingCacheEnabled = false
        currentScreenView.destroyDrawingCache()
        return bitmap
    }

    fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, width, height)
        val bgDrawable = v.background
        if (bgDrawable != null)
            bgDrawable.draw(c)
        else
            c.drawColor(Color.WHITE)
        v.draw(c)
        return b
    }

    private fun takeScreenshotOfSurfaceView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        targetView.isDrawingCacheEnabled = true
        val bitmap: Bitmap = Bitmap.createBitmap(targetView.drawingCache)
        targetView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun isVisibleOnScreen(targetView: View?): Boolean {
        if (targetView != null) {
            if (getXposition(targetView) >= 0 && getYposition(targetView) >= 0) {
                return getXposition(targetView) != 0 || getYposition(targetView) != 0
            }
        }
        return false
    }

    private fun getXposition(targetView: View): Int {
        return ScreenUtils.getAxisXpositionOfViewOnScreen(targetView) - getScreenHorizontalOffset()
    }

    private fun getYposition(targetView: View): Int {
        return ScreenUtils.getAxisYpositionOfViewOnScreen(targetView) - getScreenVerticalOffset()
    }

    private fun getScreenHeight(context: Context): Int {
        return ScreenUtils.getScreenHeight(context) - getScreenVerticalOffset()
    }

    private fun getScreenWidth(context: Context): Int {
        return ScreenUtils.getScreenWidth(context) - getScreenHorizontalOffset()
    }

    private fun getScreenVerticalOffset(): Int {
        return if (backgroundDimLayout != null) ScreenUtils.getAxisYpositionOfViewOnScreen(
            backgroundDimLayout!!
        ) else 0
    }

    private fun getScreenHorizontalOffset(): Int {
        return if (backgroundDimLayout != null) ScreenUtils.getAxisXpositionOfViewOnScreen(
            backgroundDimLayout!!
        ) else 0
    }

    private fun getMessageViewWidthOnTablet(availableSpace: Int): Int {
        return if (availableSpace > ScreenUtils.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET)) ScreenUtils.dpToPx(
            MAX_WIDTH_MESSAGE_VIEW_TABLET
        ) else availableSpace
    }

    private fun isTablet(): Boolean = mActivity.get()!!.resources.getBoolean(R.bool.isTablet)


}