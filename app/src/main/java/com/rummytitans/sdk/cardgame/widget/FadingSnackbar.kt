package com.rummytitans.sdk.cardgame.widget


import com.rummytitans.sdk.cardgame.R
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed

class FadingSnackbar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val message: TextView
    private val layout: LinearLayout
    private val action: Button
    var viewBackground: Drawable? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_fading_snackbar_rummy, this, true)
        message = view.findViewById(R.id.snackbar_text)
        layout = view.findViewById(R.id.snackbar_layout)
        action = view.findViewById(R.id.snackbar_action)
        viewBackground = ContextCompat.getDrawable(context, R.drawable.rounded_rect_white_20)
    }

    fun dismiss() {
        if (visibility == VISIBLE && alpha == 1f)
            animate().alpha(0f).withEndAction { visibility = GONE }.duration = EXIT_DURATION

    }

    fun show(
        @StringRes messageId: Int = 0, messageText: CharSequence? = null,
        @StringRes actionId: Int? = null, longDuration: Boolean = true,
        actionClick: () -> Unit = { dismiss() }, dismissListener: () -> Unit = { }
    ) {
        message.text = messageText ?: context.getString(messageId)
        if (actionId != null) {
            action.run {
                visibility = VISIBLE
                text = context.getString(actionId)
                setOnClickListener {
                    actionClick()
                }
            }
        } else action.visibility = GONE

        alpha = 0f
        visibility = VISIBLE
        animate().alpha(1f).duration = ENTER_DURATION
        val showDuration = ENTER_DURATION + if (longDuration) LONG_DURATION else SHORT_DURATION
        postDelayed(showDuration) {
            dismiss()
            dismissListener()
        }
    }

    fun show(
        @StringRes messageId: Int = 0, isError: Boolean = false, messageText: CharSequence? = null,
        longDuration: Boolean = true, dismissListener: () -> Unit = { }
    ) {
        message.text = messageText ?: context.getString(messageId)
        if (TextUtils.isEmpty(message.text)) return

        layout.background = viewBackground

        layout.background.setColorFilter(
            ContextCompat.getColor(context, if (isError) R.color.orange else R.color.blue),
            PorterDuff.Mode.SRC_OVER
        )
        action.visibility = GONE
        alpha = 0f
        visibility = VISIBLE
        animate().alpha(1f).duration = ENTER_DURATION
        val showDuration = ENTER_DURATION + if (longDuration) LONG_DURATION else SHORT_DURATION
        postDelayed(showDuration) {
            dismiss()
            dismissListener()
        }
    }

    companion object {
        private const val ENTER_DURATION = 300L
        private const val EXIT_DURATION = 200L
        private const val SHORT_DURATION = 1_500L
        private const val LONG_DURATION = 2_750L
    }
}
