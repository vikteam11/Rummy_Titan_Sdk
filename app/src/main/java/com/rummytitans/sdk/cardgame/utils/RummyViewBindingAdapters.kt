package com.rummytitans.sdk.cardgame.utils

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.widget.FontSpan
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.text.*
import android.text.TextUtils.TruncateAt
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.rummytitans.sdk.cardgame.ui.profile.completeprofile.AvatarAdapter
import com.rummytitans.sdk.cardgame.widget.EndlessRecyclerView
//import com.loopeer.shadow.ShadowView
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("setCompactDrawableWithTrophyRight")
fun setCompactDrawableWithTrophyRight(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        VectorDrawableCompat.create(view.resources, R.drawable.ic_trophy, view.context.theme),
        null, VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null
    )
}

@BindingAdapter("setGuideLinePercent")
fun setGuideLinePercent(guideline: Guideline, percent: Float){
    guideline.setGuidelinePercent(percent)
}

@BindingAdapter(value = ["bind_htmlText"], requireAll = true)
fun setHtmlText(textView: TextView, str: String?) {
    textView.text = HtmlCompat.fromHtml(str ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

@BindingAdapter("avatarAdapter")
fun addAvatarAdapter(recyclerView: RecyclerView, avatarList: ArrayList<AvatarModel>) {
    val adapter: AvatarAdapter = recyclerView.adapter as AvatarAdapter
    adapter.updateItems(avatarList)
}

@BindingAdapter("lottieFile")
fun setLottieAnimation(view: View?,lottieFile: String?){
    if (TextUtils.isEmpty(lottieFile)) return
    runCatching {
        (view as? LottieAnimationView)?.apply {
            setAnimation(lottieFile)
            setFailureListener {
                Log.e("setLottieAnimation"," Er "+it.message)
                it.printStackTrace()
            }
            playAnimation()
        }
    }
}

@BindingAdapter("setRefreshing")
fun EndlessRecyclerView.setRefreshing(flag:Boolean){
    isRefreshing = flag
}


@BindingAdapter("adapter")
fun addAdapter(spinner: Spinner, dataList: MutableList<String>) {
    if (spinner.adapter == null) {
        val adapter = ArrayAdapter<String>(spinner.context, R.layout.item_states_rummy)
        adapter.addAll(dataList)
        adapter.notifyDataSetChanged()
        spinner.adapter = adapter
    } else {
        val adapter = spinner.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(dataList)
        adapter.notifyDataSetChanged()
    }
}

@BindingAdapter(value = ["marqueeText"], requireAll = true)
fun setMarqueeText(textView: TextView, str: String?) {
    textView.ellipsize = TruncateAt.MARQUEE
    textView.text = str
    textView.isSelected = true
    textView.isSingleLine = true
}

@BindingAdapter("setTint")
fun setImageViewTint(view: ImageView, color: Int) {
    ImageViewCompat.setImageTintList(view,ColorStateList.valueOf(color))
}



@BindingAdapter("setCompactDrawableRight")
fun setCompactDrawableRight(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null, null, VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null
    )
}

@BindingAdapter(value = ["itemText", "isCurrent","isCompleteMatch"], requireAll = false)
fun isCurrentPlaying(view: TextView, itemText: String?, isCurrent: Boolean,isCompleteMatch:Boolean) {
    itemText?.let {
        runCatching {
            if (TextUtils.isEmpty(itemText)) return
            var extraText=""
            view.typeface = when {
                isCompleteMatch ->   ResourcesCompat.getFont(view.context, R.font.rubik_regular)
                isCurrent -> {
                    extraText="*"
                    ResourcesCompat.getFont(view.context, R.font.rubik_medium)
                }
                else ->  ResourcesCompat.getFont(view.context, R.font.rubik_regular)
            }
            (itemText+extraText).also { view.text = it }
        }.onFailure { }
    }
}



@BindingAdapter(value = ["colorCodeFirst", "colorCodeSecond","colorCodeThird","radius"], requireAll = false)
fun setGradientBack(view: View, colorCodeFirst: Int, colorCodeSecond: Int,colorCodeThird: Int,radius:Float) {
    view.background = GradientDrawable().apply {
        colors = intArrayOf(
            colorCodeSecond,
           colorCodeFirst,
            colorCodeThird
        )
        cornerRadius = radius
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
        gradientType = GradientDrawable.LINEAR_GRADIENT

    }

}

@BindingAdapter(value = ["colorFirst", "colorSecond","colorThird","radius"], requireAll = false)
fun setVerticalGradientBack(view: View, colorFirst: Int, colorSecond: Int,colorThird: Int,radius:Float) {
    view.background = GradientDrawable().apply {
        colors = intArrayOf(
            colorSecond,
            colorFirst,
            colorThird
        )
        cornerRadius = radius
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
        gradientType = GradientDrawable.LINEAR_GRADIENT

    }

}


@BindingAdapter("gifDrawable")
fun animateGif(view: ImageView, gifDrawable: Int) {
    if (gifDrawable == 0) return
    Glide.with(view.context).asGif().load(gifDrawable).into(view)
}


@BindingAdapter("rotateView")
fun rotateAnimation(view: View?, value: Float) {
    var from = .0f
    var to = .0f
    if (value < 1) {
        from = 0f
        to = 0f
    } else {
        from = 0f
        to = 180f
    }
    ObjectAnimator.ofFloat(view, View.ROTATION, from, to).setDuration(300).start();
}

@BindingAdapter("colorCode")
fun colorCode(view: CardView, colorCode: Int) {
    view.setCardBackgroundColor(ContextCompat.getColor(view.context, colorCode))
}

@BindingAdapter("setCompactDrawableLeft")
fun setCompactDrawableLeft(view: EditText, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme),
        null, null, null
    )
}


@BindingAdapter("setCompactDrawableLeft")
fun setCompactDrawableLeft(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null,
        VectorDrawableCompat.create(view.resources, R.drawable.ic_next, view.context.theme), null
    )
}

@BindingAdapter("setDrawableLeft")
fun setCompactDrawable(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null,
        null, null
    )
}

@BindingAdapter("setCompactDrawableTop")
fun setCompactDrawableTop(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null,
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null, null
    )
}

@BindingAdapter("setCompactDrawableLeftTextView")
fun setCompactDrawableLeftTextView(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null, null,
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null
    )
}

@BindingAdapter("setCompactDrawableStartTextView")
fun setCompactDrawableStartTextView(view: TextView, drawable: Int) {
    if (drawable == 0) return
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        VectorDrawableCompat.create(view.resources, drawable, view.context.theme), null,
        null, null
    )
}

@BindingAdapter("drawableStartTint")
fun drawableStartTint(view: AppCompatTextView, @ColorInt color: Int) {
    for (drawable in view.compoundDrawables) {
        drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

@BindingAdapter("setCompactCardDrawableLeftTextView")
fun setCompactCardDrawableLeftTextView(view: TextView, drawable: Int) {
    if (drawable == 0) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
    } else {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, ContextCompat.getDrawable(view.context, drawable), null
        )
    }
}

@BindingAdapter(value = ["colorCode", "isFirstView"], requireAll = true)
fun roundFirstViewBorder(view: View, colorCode: Int, isFirstView: Boolean) {
    val max = view.let { convertIntToDp(25, it).toFloat() }
    val min = 0f
    view.background = GradientDrawable().apply {
        setColor(colorCode)
        cornerRadii = if (isFirstView) floatArrayOf(max, max, max, max, min, min, min, min)
        else floatArrayOf(min, min, min, min, min, min, min, min)
    }
}


@BindingAdapter("setSrcCompact")
fun setSrcCompact(view: ImageView, drawable: Int) {
    if (drawable == 0) return
    runCatching { view.setImageDrawable(AppCompatResources.getDrawable(view.context, drawable)) }
        .onFailure { }
}

@BindingAdapter("setSrcCompact")
fun setSrcCompact(view: ImageView, drawable: Drawable) {
    runCatching { view.setImageDrawable(drawable) }.onFailure { }
}

@BindingAdapter("setAvatar")
fun setAvatar(view: ImageView, avtarId: Int) {
    runCatching { view.setImageResource(getAvtar(avtarId)) }.onFailure { }
}

@BindingAdapter(
    value = ["setConditionalBackgroundCustomPrimary", "colorPrimary"], requireAll = true
)
fun setConditionalBackgroundCustomPrimary(
    view: View, setConditionalBackgroundCustomPrimary: Boolean, colorPrimary: Int
) {
    val megaColor = if (setConditionalBackgroundCustomPrimary) colorPrimary
    else ContextCompat.getColor(view.context, R.color.cool_grey)
    view.setBackgroundColor(megaColor)
}

@BindingAdapter(
    value = ["setCaptainConditionalBackgroundCustomPrimary", "colorPrimary"], requireAll = true
)
fun setCaptainConditionalBackgroundCustomPrimary(
    view: View, setConditionalBackgroundCustomPrimary: Boolean, colorPrimary: Int
) {
    val megaColor = if (setConditionalBackgroundCustomPrimary) colorPrimary
    else ContextCompat.getColor(view.context, R.color.pale_grey)
    view.setBackgroundColor(megaColor)
}

@BindingAdapter(value = ["setConditionalTextPrimary", "colorSecondary"], requireAll = true)
fun setConditionalTextPrimary(
    view: TextView, setConditionalBackgroundColorPrimary: Boolean, colorSecondary: Int
) {
    val outValue = TypedValue()
    view.context.theme.resolveAttribute(R.attr.colorPrimary, outValue, true)
    val megaColor = ContextCompat.getColor(
        view.context,
        if (setConditionalBackgroundColorPrimary) outValue.resourceId else colorSecondary
    )
    view.setTextColor(megaColor)
}

@BindingAdapter(value = ["setTabSelectedTextColor"], requireAll = true)
fun setTabSelectedTextColor(view: TabLayout, setTabSelectedTextColor: Int) {
    view.setTabTextColors(R.color.black, setTabSelectedTextColor)
    view.setSelectedTabIndicatorColor(setTabSelectedTextColor)
}

@BindingAdapter("setMegaTintBackground")
fun setMegaTintBackground(view: View, color: Boolean) {
    val outValue = TypedValue()
    view.context.theme.resolveAttribute(R.attr.colorPrimary, outValue, true)
    val megaColor =
        ContextCompat.getColor(view.context, if (!color) outValue.resourceId else R.color.sand)
    view.background.setColorFilter(megaColor, PorterDuff.Mode.SRC_ATOP)
}

@BindingAdapter("setTeamListBackground")
fun setTeamListBackground(view: ConstraintLayout, color: Int) {
    if (color != -1) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.WHITE)
        shape.setStroke(5, color)
        shape.cornerRadius = 10f
        view.background = shape
    }
}

@BindingAdapter("circleBgColor")
fun roundView(view: View, colorCode: Int) {
    view.background = ShapeDrawable(OvalShape()).apply { paint.color = colorCode }
}

@BindingAdapter(value = ["circleBorderColor", "fillColorColor"], requireAll = false)
fun createCircleWithFillOrNot(view: View, circleBorderColor: Int, fillColorColor: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.OVAL
    shape.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    shape.setColor(fillColorColor)
    shape.setStroke(4, circleBorderColor)
    view.background = shape
}

@BindingAdapter(value = ["setCount", "categoryName"], requireAll = true)
fun setColorText(textView: TextView, count: String, category: String) {
    if (!TextUtils.isEmpty(category) && !TextUtils.isEmpty(count)) {
        val categoryMsg = "$category "
        val span = SpannableString(categoryMsg + count)
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.steel_grey)),
            0, categoryMsg.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.gunmetal)),
            categoryMsg.length, categoryMsg.length + count.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        textView.text = span
    }
}

@BindingAdapter("setTeamListBorder")
fun setTeamListBorder(view: View, color: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setStroke(5, color)
    shape.cornerRadius = view.context.resources.getDimension(R.dimen.dimen_16)
    view.background = shape
}

@BindingAdapter("viewBorder")
fun setViewBorder(view: View, color: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setStroke(5, color)
    shape.cornerRadius = view.context.resources.getDimension(R.dimen.dimen_4)
    view.background = shape
}

@BindingAdapter("setViewBorderLine")
fun setViewBorderLine(view: View, color: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setStroke(4, color)
    shape.cornerRadius = view.context.resources.getDimension(R.dimen.dimen_4)
    view.background = shape
}

@BindingAdapter(value = ["setTextViewBorderBackgrrond", "fillColor"], requireAll = false)
fun setViewBorderBackgrrond(view: View, color: Int, fillColor: Boolean = false) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    if (fillColor) {
        shape.setColor(ContextCompat.getColor(view.context, R.color.transparent_green))
        shape.setStroke(4, ContextCompat.getColor(view.context, R.color.winning_green))
    } else shape.setStroke(2, color)
    shape.cornerRadius = 10f
    view.background = shape
}

@BindingAdapter("setViewBorderBackground")
fun setViewBorderBackground(view: View, color: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setColor(ContextCompat.getColor(view.context, R.color.white))
    shape.setStroke(2, color)
    shape.cornerRadius = view.context.resources.getDimension(R.dimen.margin_min)
    view.background = shape
}


@BindingAdapter(value=["setViewBorderBackground","strokeWidth","radius"],requireAll = true)
fun setViewBorderBackground(view: View, color: Int,strokeWidth:Int,radius:Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setColor(ContextCompat.getColor(view.context, R.color.white))
    shape.setStroke(strokeWidth, color)
    shape.cornerRadius =radius.toFloat()
    view.background = shape
}

@BindingAdapter(
    value = ["layout_marginStart", "layout_marginTop", "layout_marginEnd", "layout_marginBottom"],
    requireAll = false
)
fun bindingSetMargins(view: View, start: Float?, top: Float?, end: Float?, bottom: Float?) {
    view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
        start?.toInt()?.let { leftMargin = it }
        top?.toInt()?.let { topMargin = it }
        end?.toInt()?.let { rightMargin = it }
        bottom?.toInt()?.let { bottomMargin = it }
    }
}

@BindingAdapter(value = ["setViewBackground", "viewRadius", "strokeColor"], requireAll = false)
fun setViewBackground(view: View, fillColor: Int, radius: Int, strokeColor: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setColor(
        if (fillColor == -1) ContextCompat.getColor(view.context, R.color.white) else fillColor
    )
    shape.setStroke(2, if (strokeColor == -1) fillColor else strokeColor)
    shape.cornerRadius = convertIntToDp(radius, view).toFloat()
    view.background = shape
}

@BindingAdapter(value = ["borderColor", "fillColor"], requireAll = false)
fun roundViewfillColor(view: View, borderColor: Int, fillColor: Int) {
    view.background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(ContextCompat.getColor(view.context, fillColor))
        setStroke(3, ContextCompat.getColor(view.context, fillColor))
        cornerRadius = 10f
    }
}

@BindingAdapter(
    value = ["viewBorderColor", "viewFillColor", "viewRadius", "isAlpha"], requireAll = true
)
fun fillViewAndBorder(view: View, borderColor: Int, fillColor: Int, radius: Int, isAlpha: Boolean) {
    view.background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        if (isAlpha) setColor(ColorUtils.setAlphaComponent(fillColor, 10))
        setStroke(2, borderColor)
        cornerRadius = radius.toFloat()
    }
}

@BindingAdapter(value = ["cardBorderColor", "fillColor"], requireAll = false)
fun cardWithFillColor(view: View, cardBorderColor: Int, fillColor: Int) {
    view.background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(ContextCompat.getColor(view.context, fillColor))
        setStroke(3, ContextCompat.getColor(view.context, cardBorderColor))
        cornerRadius = convertIntToDp(4, view).toFloat()
    }
}

@BindingAdapter("setDashBorder")
fun setDashBorder(view: View, color: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setStroke(8, color, 30.0f, 10.0f)
    shape.cornerRadius = 16f
    view.background = shape
}

@BindingAdapter("setViewBorderBackgrrond")
fun setViewBorderBackgrrond(view: View?, color: Int) {
   /* val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setStroke(2, color)
    (view?.rootView as? ShadowView)?.shadowRadius?.let { shape.cornerRadius = it }
    view?.background = shape*/
}

@BindingAdapter(value = ["viewBorderColor", "fillColor"], requireAll = false)
fun viewBorderColor(view: View, color: Int, fillColor: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setColor(fillColor)
    shape.setStroke(2, color)
    shape.cornerRadius = 10f
    view.background = shape
}

@BindingAdapter("customCardColor")
fun customCardView(view: View, customCardColor: Int) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.setColor(customCardColor)
    shape.cornerRadius = 20f
    view.background = shape
}

@BindingAdapter("isCheckBoxSelected")
fun setCheckBoxTint(view: CheckBox, isCheckBoxSelected: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val color = if (isCheckBoxSelected) ContextCompat.getColor(view.context, R.color.white)
        else ContextCompat.getColor(view.context, R.color.rummy_azure_two)
        view.buttonTintList = ColorStateList.valueOf(color)
    }
}





@BindingAdapter("setProgressTint")
fun setProgressTint(bar: ProgressBar, color: Int) {
    if(color != Color.parseColor("#ff0088ff")) {
        bar.progressTintList = ColorStateList.valueOf(color);
    }
}


@BindingAdapter("setImageWithPath","placeHolder", requireAll = false)
fun setImageWithPath(view: ImageView, imagePath: String?,placeHolder:Drawable?= null) {
    runCatching {
        if (!TextUtils.isEmpty(imagePath)) {
            Glide.with(view.context)
                .asBitmap()
                .load(imagePath)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16.toDp(view.context))))
                .into(view)
        }else{
            view.setImageDrawable(placeHolder)
        }
    }.onFailure {

    }
}

@BindingAdapter("setImageUrl")
fun setImageUrl(view: ImageView, imageUrl: String?) {
    runCatching {
        if (!TextUtils.isEmpty(imageUrl) && URLUtil.isValidUrl(imageUrl))
            Glide.with(view.context).load(imageUrl).into(view)
    }.onFailure { }
}

@BindingAdapter("setOfferImage")
fun setOfferImage(view: ImageView, imageUrl: String?) {
    runCatching {
        if (!TextUtils.isEmpty(imageUrl) && URLUtil.isValidUrl(imageUrl))
            Glide.with(view.context).load(imageUrl).placeholder(R.drawable.offer_placeholder)
                .into(view)
    }.onFailure { }
}

@BindingAdapter(value = ["setImageUrl", "setPlaceHolder"], requireAll = true)
fun setImageUrl(view: ImageView, setImageUrl: String?, setPlaceHolder: Int?) {
    val errorDrawable = if (Build.VERSION.SDK_INT >= 21)
        ContextCompat.getDrawable(view.context, setPlaceHolder!!)
    else {
        try {
            VectorDrawableCompat.create(
                view.resources, setPlaceHolder ?: R.drawable.app_logo_rummy, view.context.theme
            )
        } catch (e: Exception) {
            ContextCompat.getDrawable(view.context, setPlaceHolder!!)
        }
    }

    if (!TextUtils.isEmpty(setImageUrl) && URLUtil.isValidUrl(setImageUrl))
        Glide.with(view.context).applyDefaultRequestOptions(
            RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(errorDrawable).placeholder(errorDrawable)
        ).load(setImageUrl).into(view)
    else if (null != setPlaceHolder)
        Glide.with(view.context).load(errorDrawable).into(view)
}

@BindingAdapter("setImageUrl")
fun setImageUrl(view: ImageView, imageUrl: Int) {
    view.setImageResource(imageUrl)
}

@BindingAdapter("setImageVector")
fun setImageVector(view: ImageView, imageUrl: Boolean) {
    runCatching { view.setImageResource(if (imageUrl) R.drawable.ic_minus else R.drawable.ic_add) }
        .onFailure { }
}

@BindingAdapter("setImage")
fun setImage(view: ImageView, imageUrl: Int) {
    if (imageUrl == 0) return
    runCatching { view.setImageResource(imageUrl) }.onFailure { }
}

@BindingAdapter(value = ["normalSpanText", "boldSpanText"], requireAll = true)
fun setBoldTextInString(view: TextView, normalSpanText: String, boldSpanText: String) {
    val str = SpannableStringBuilder(normalSpanText)
    val startIndex = normalSpanText.indexOfFirst { it == '₹' }
    if (startIndex > 0) {
        str.setSpan(
            StyleSpan(Typeface.BOLD), startIndex, startIndex + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    view.text = str
}

@BindingAdapter(value = ["date", "inputPattern", "outputPattern"], requireAll = true)
fun setDate(view: TextView, date: String, inputPattern: String, outputPattern: String) {
    try {
        val inputFormatter =
            SimpleDateFormat(inputPattern, Locale.getDefault())//""yyyy-MM-dd'T'HH:mm:ss"
        val outputFormatter = SimpleDateFormat(outputPattern, Locale.getDefault())
        val finalDate = inputFormatter.parse(date) ?: date
        val finalDateStr = outputFormatter.format(finalDate)
            .replace("am","AM")
            .replace("pm","PM")
        view.text = finalDateStr
    } catch (e: Exception) {
        view.text = date
    }
}


@BindingAdapter(
    value = ["plainText", "plainTextColor", "textArray", "colorsArray","textSizeArray","fontArray"], requireAll = false
)
fun setTextColorDynamically(
    view: TextView, plainText: String, plainTextColor: Int,
    textArray: List<String>?, colorsArray: List<Int>?,textSizeArray: List<Float>?,
    fontArray: List<Int>?
) {
    if (colorsArray.isNullOrEmpty() || textArray.isNullOrEmpty()) {
        view.text = plainText
        return
    }
    val ssBuilder = SpannableStringBuilder(plainText)

    ssBuilder.setSpan(
        ForegroundColorSpan(plainTextColor), 0, plainText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    for (i in 0..textArray.size.minus(1)) {
        ssBuilder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(view.context, colorsArray[i])),
            plainText.indexOf(textArray[i]), plainText.indexOf(textArray[i]) + textArray[i].length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
         textSizeArray?.elementAtOrNull(i)?.let {
            ssBuilder.setSpan(
                RelativeSizeSpan(it),
                plainText.indexOf(textArray[i]), plainText.indexOf(textArray[i]) + textArray[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        fontArray?.elementAtOrNull(i)?.let {
            ssBuilder.setSpan(
                FontSpan(ResourcesCompat.getFont(view.context, it)),
                plainText.indexOf(textArray[i]), plainText.indexOf(textArray[i]) + textArray[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    view.text = ssBuilder
}

@BindingAdapter(
    value = ["plainText", "plainTextColor", "textArray", "colorsArray"], requireAll = false
)
fun setTextColorDynamiclly(
    view: TextView, plainText: String, plainTextColor: Int,
    textArray: List<String>, colorsArray: List<Int>
) {
    val text = plainText
    val ssBuilder = SpannableStringBuilder(text)

    ssBuilder.setSpan(
        ForegroundColorSpan(plainTextColor), 0, plainText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    for (i in 0..textArray.size.minus(1)) {
        ssBuilder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(view.context, colorsArray[i])),
            text.indexOf(textArray[i]), text.indexOf(textArray[i]) + textArray[i].length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    view.text = ssBuilder
}

@BindingAdapter(value = ["spannableText", "clickableText", "urlToOpen"], requireAll = true)
fun TextView.setSpannableTexts(
    spannableText: String, clickableText:String,urlToOpen:()->Unit) {
    val ss = SpannableString(spannableText)
    try {
        val clickableSpan = object : ClickableSpan() {

            override fun onClick(widget: View) {
                urlToOpen()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(
            FontSpan(ResourcesCompat.getFont(context, R.font.rubik_bold)),
            spannableText.indexOf(clickableText),
            spannableText.indexOf(clickableText) + clickableText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            clickableSpan, spannableText.indexOf(clickableText),
            spannableText.indexOf(clickableText) + clickableText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            ForegroundColorSpan(context.getColorInt(R.color.rummy_mainColor)),
            spannableText.indexOf(clickableText),
            spannableText.indexOf(clickableText) + clickableText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = ss
        movementMethod = LinkMovementMethod.getInstance()
    } catch (e: Exception) {
        text = spannableText
    }
}

@BindingAdapter(value = ["plainText", "clickableText", "languageCode"], requireAll = true)
fun setClickableText(
    view: TextView, plainText: String, clickableText: List<String>, languageCode: String
) {
    val ss = SpannableString(plainText)
    try {
        for (i in 0..clickableText.size.minus(1)) {
            val clickableSpan = object : ClickableSpan() {

                override fun onClick(widget: View) {
                    /*if (i == 0) {
                        sendToCloseAbleInternalBrowser(
                            view.context, getWebUrls(WebViewUrls.SHORT_Terms_And_Conditions),
                            clickableText[i]
                        )
                    } else if (i == 1) {
                        sendToCloseAbleInternalBrowser(
                            view.context, getWebUrls(WebViewUrls.SHORT_WebPrivacyPolicy),
                            clickableText[i]
                        )
                    }else if (i == 2) {
                        sendToCloseAbleInternalBrowser(
                            view.context, getWebUrls(WebViewUrls.SHORT_RESPONSIBLE_GAMING),
                            clickableText[i]
                        )
                    }*/
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }
            ss.setSpan(
                FontSpan(ResourcesCompat.getFont(view.context, R.font.rubik_bold)),
                plainText.indexOf(clickableText[i]),
                plainText.indexOf(clickableText[i]) + clickableText[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ss.setSpan(
                clickableSpan, plainText.indexOf(clickableText[i]),
                plainText.indexOf(clickableText[i]) + clickableText[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ss.setSpan(
                ForegroundColorSpan(view.context.getColorInt(R.color.gray1)),
                plainText.indexOf(clickableText[i]),
                plainText.indexOf(clickableText[i]) + clickableText[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        view.text = ss
        view.movementMethod = LinkMovementMethod.getInstance()
    } catch (e: Exception) {
        view.text = plainText
    }
}

@BindingAdapter("setTabColor")
fun setTabColor(view: TabLayout, selecteColor: String) {
    val colorSelected = Color.parseColor(selecteColor)
    val colorNormal = ContextCompat.getColor(view.context, R.color.silver_two)
    view.setTabTextColors(colorNormal, colorSelected)
    view.setSelectedTabIndicatorColor(colorSelected)
}


@BindingAdapter("bottomSheetIsDragable")
fun bottomSheetIsDragable(view:View,flag:Boolean){
    val behavior = BottomSheetBehavior.from(view)
    behavior.isDraggable = flag
}

@BindingAdapter("bottomSheetStateRummy")
fun bottomSheetStateRummy(view: View, event: Int) {
    BottomSheetBehavior.from(view).state = event
}

@BindingAdapter(value = ["topPadding", "bottomPadding","leftPadding","rightPadding"], requireAll = false)
fun setPadding(view: View, topPadding:Int=0, bottomPadding:Int=0,leftPadding:Int=0,rightPadding:Int=0) {
    view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
}

@BindingAdapter("removeDefaultPadding")
fun removeDefaultPadding(editText: EditText,removePadding:Boolean){
    runCatching {
        if (removePadding && editText.background is InsetDrawable) {
            val insetDrawable = editText.background as InsetDrawable
            val originalDrawable = insetDrawable.drawable
            editText.background = originalDrawable
        }
    }
}


@BindingAdapter(value = ["gradientColor1", "gradientColor2", "gradientOrientation"])
fun setGradientOverlay(
    view: View, color1: Int, color2: Int, orientation: GradientDrawable.Orientation
) {
    val gd = GradientDrawable(orientation, intArrayOf(color1, color2))
    gd.cornerRadius =  4.toPx.toFloat()
    view.background = gd
}

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

@BindingAdapter("setDoublePrice")
fun setDoublePrice(view: TextView, amount: Double) {
    view.text = "₹".plus(DecimalFormat("##.##").format(amount))
}

@BindingAdapter("setProgress")
fun setProgress(view: ProgressBar, progress: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) view.setProgress(progress, true)
    else view.progress = progress
}

@BindingAdapter(
    value = ["verifyStatus", "verifyText", "verifyColor", "tintColor"], requireAll = true
)
fun setVerificationStatus(
    view: ImageView, verifyStatus: Boolean, verifyText: String?, verifyColor: Int?, tintColor: Int?
) {
    when {
        TextUtils.isEmpty(verifyText) -> {
            view.setColorFilter(
                ContextCompat.getColor(view.context, R.color.silver), PorterDuff.Mode.SRC_IN
            )
            ViewCompat.setBackgroundTintList(
                view,
                ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.alpha_silver))
            )
        }
        !TextUtils.isEmpty(verifyText) and !verifyStatus -> {
            view.setColorFilter(
                ContextCompat.getColor(view.context, R.color.gunmetal), PorterDuff.Mode.SRC_IN
            )
            ViewCompat.setBackgroundTintList(
                view,
                ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.light_gunmetal))
            )
        }
        !TextUtils.isEmpty(verifyText) and verifyStatus -> {
            view.setColorFilter(
                ContextCompat.getColor(view.context, tintColor!!), PorterDuff.Mode.SRC_IN
            )
            val buttonDrawable = DrawableCompat.wrap(view.background)
            DrawableCompat.setTint(
                buttonDrawable, ContextCompat.getColor(view.context, verifyColor!!)
            )
            view.background = buttonDrawable
        }
    }
}

@BindingAdapter("setAmmountText")
fun setAmmountText(view: TextView, amount: Double) {
    if (amount % 1.0 != 0.0) view.text = "₹".plus("%s".format(amount))
    else view.text = "₹".plus("%.0f".format(amount))
}

@BindingAdapter("setAmmountText")
fun setAmmountText(view: TextView, amount: Int) {
    view.text = "₹$amount"
}

@BindingAdapter("setDOBText")
fun setDOBText(view: TextView, dob: String?) {
    if (!TextUtils.isEmpty(dob) && dob?.contains(" ")!!) view.text = dob.split(" ")[0]
}

@BindingAdapter(value = ["setVerificationDesc"])
fun setVerificationDesc(view: TextView, model: ProfileVerificationModel?) {
    model?.apply {
        view.text =   when{
            !EmailItem.Verify && !TextUtils.isEmpty(EmailItem.Value)->{
                view.context.getString(R.string.please_verify_email)
            }
            !EmailItem.Verify->{
                view.context.getString(R.string.please_verify_email)
            }
            !PancardItem.Verify  && !TextUtils.isEmpty(PancardItem.Value)->{
                view.context.getString(R.string.your_pan_detail_under_process)
            }
            !PancardItem.Verify ->{
                view.context.getString(R.string.please_verify_pan)
            }
            !BankItem.Verify  && !TextUtils.isEmpty(BankItem.Value)->{
                view.context.getString(R.string.your_bankdetail_under_process)
            }
            !BankItem.Verify ->{
                view.context.getString(R.string.please_verify_bank_detail)
            }
            !AddressItem.Verify  && !TextUtils.isEmpty(AddressItem.Value)->{
                view.context.getString(R.string.address_verification_under_process)
            }
            !AddressItem.Verify ->{
                view.context.getString(R.string.your_address_verification_under_process)
            }
            else ->{
                view.context.getString(R.string.update_bank_deatils)
            }
        }
    } ?: run {
        view.text = if(model?.profileVerified() == true) view.context.getString(R.string.profile_verified)  else
            ""
    }
}




@BindingAdapter("onBottomSheetStateChanged")
fun onBottomSheetStateChanged(view: View, state: ObservableBoolean) {
    BottomSheetBehavior.from(view)
        .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> state.set(false)
                    BottomSheetBehavior.STATE_COLLAPSED -> state.set(false)
                    BottomSheetBehavior.STATE_EXPANDED -> state.set(true)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
}

@BindingAdapter("onBottomSheetStateChanged")
fun onBottomSheetStateChanged(view: ConstraintLayout, data: MutableLiveData<Int>) {
    BottomSheetBehavior.from(view)
        .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                data.value = newState
            }
        })
}

@BindingAdapter("setTextColor")
fun setTextColor(view: TextView, colorCode: String) {
    if (colorCode.isNotEmpty())
        runCatching { view.setTextColor(Color.parseColor(colorCode)) }
            .onFailure { view.setTextColor(Color.parseColor("#000000")) }
}

@BindingAdapter("setTextColor")
fun setTextColor(view: TextView, colorCode: Int) {
    if (colorCode != 0)
        runCatching { view.setTextColor(view.context.getColorInt(colorCode)) }
            .onFailure { view.setTextColor(Color.parseColor("#000000")) }
}

@BindingAdapter("setDrawableEndCompact")
fun setDrawableEndCompact(view: TextView,drawable:Drawable?=null) {
    runCatching {
         view.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,drawable,null)
    }
}


@BindingAdapter("setUnderline")
fun setUnderline(view: TextView, b: Boolean) {
    if (b) view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

@BindingAdapter("togglePassword")
fun togglePasswordVisibillity(view: View, b: ObservableBoolean) {
    view.rootView?.findViewById<EditText>(R.id.editPassword)?.transformationMethod =
        if (b.get()) PasswordTransformationMethod.getInstance() else null
}

@BindingAdapter("TextInputTintColor")
fun setUpperHintColor(textInputLayout: TextInputLayout, color: Int) {
    runCatching {
        val states = arrayOf(intArrayOf())
        val colors = intArrayOf(color)
        val myList = ColorStateList(states, colors)
        textInputLayout.hintTextColor = myList
        textInputLayout.boxStrokeColor = color
    }.onFailure { print(it.message) }
}


@BindingAdapter("setTeamName")
fun setTeamName(view: TextView, matchModel: MatchModel?) {
    matchModel?.let {
        val teamName = matchModel.teamName1 + " vs " + matchModel.teamName2
        view.text = if (matchModel.Is3TC == true) teamName + " vs " + matchModel.teamName3
        else teamName
    }
}

@BindingAdapter(value = ["setDummyMobile", "isTwoWayVerification"], requireAll = true)
fun setDummyMobileNumber(view: TextView, mobileNumber: String, isTwoWayVerification: Boolean) {
    runCatching {
        view.text =
            if (isTwoWayVerification) mobileNumber.replaceRange(2, 8, "******") else mobileNumber
    }.onFailure { view.text = mobileNumber }
}



@BindingAdapter("setDecimalAmount","placeHolder","placeHolderAtStart", requireAll = false)
fun setPlayerPoint(view: TextView?,point: Double?,placeHolder:String?="",setPlaceHolderAtStart:Boolean=false){
   val pointStr =  DecimalFormat("##.##").format(point?:0.0)
    view?.text =if (setPlaceHolderAtStart)
         (if(!TextUtils.isEmpty(placeHolder))" $placeHolder" else "") + pointStr
    else
        pointStr + if(!TextUtils.isEmpty(placeHolder))" $placeHolder" else ""
}


@BindingAdapter("setAmountFormat","placeHolder","placeHolderAtStart", requireAll = false)
fun setAmountFormat(view: TextView?,point: Double?,placeHolder:String?="",setPlaceHolderAtStart:Boolean=false){
    val amountStr =  DecimalFormat("##.##").format(point?:0.0)
    view?.text =if (setPlaceHolderAtStart)
        (if(!TextUtils.isEmpty(placeHolder))"$placeHolder" else "") + amountStr
    else
        amountStr + if(!TextUtils.isEmpty(placeHolder))" $placeHolder" else ""
}


@BindingAdapter("formatAmountInRupees")
fun formatAmountInRupees(view: TextView,amount: Double){
    var numberString = ""
    numberString = if (Math.abs(amount / 1000000) > 1) {
        (amount / 1000000).decimalFormat().toString() + "m"
    } else if (Math.abs(amount / 1000) > 1) {
        (amount / 1000).decimalFormat().toString() + "k"
    } else {
        amount.toString() + ""
    }
    view.text = "₹$numberString"
}


@BindingAdapter("setTextSize")
fun setTextSize(view: TextView,size:Float){
    view.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
}

@BindingAdapter("setTextFont")
fun setTextFont(view: TextView,font:Int){
    val typeFace =  ResourcesCompat.getFont(view.context,font)
    view.typeface = typeFace
}

fun getRemainingDays(time: Long):Long {
    return try {
        val now = Date().time
        val diff = (time - now)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val day = if(minutes >0 || hours > 0)TimeUnit.MILLISECONDS.toDays(diff)+1 else TimeUnit.MILLISECONDS.toDays(diff)
        if(day>= 0)
            day
        else
            0
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }
}

@BindingAdapter("lottieFile")
fun setLottieAnimation(view: View?,id: Int =-1){
    if (id <=0  ) return
    runCatching {
        (view as? LottieAnimationView)?.apply {
            setAnimation(id)
            setFailureListener {
                Log.e("setLottieAnimation"," Er "+it.message)
                it.printStackTrace()
            }
            playAnimation()
        }
    }
}


@BindingAdapter(value = ["setContestFee", "couponCount"])
fun setCouponPrice(view: TextView, amount: Double, couponCount: Int) {
    val total = amount * couponCount
    view.text = "- ₹".plus("%.2f".format(total))
}

@BindingAdapter(value = ["playGameConfirmText"], requireAll = false)
fun setJoinOrAddCashText(view: TextView?, usableAmountModel: JoinGameConfirmationModel?) {
        view?.context?.let {cont->
        usableAmountModel?.apply {
            val normalText=if (totalAmountCollect > 0) {
                "Add " + cont.getString(R.string.rupees) + "${DecimalFormat("##.##").format(totalAmountCollect)} & Play"
            } else "Play Now"
            view.text =normalText
        }
    }
}


@BindingAdapter(value = ["plainText", "amountText"], requireAll = true)
fun TextView.setAllSpanBold(
    plainText: String, amountText: List<String>
) {
    val ss = SpannableString(plainText)
    try {
        for (i in 0..amountText.size.minus(1)) {
            ss.setSpan(
                FontSpan(ResourcesCompat.getFont(context, R.font.rubik_bold)),
                plainText.indexOf(amountText[i]),
                plainText.indexOf(amountText[i]) + amountText[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ss.setSpan(
                ForegroundColorSpan(context.getColorInt(R.color.text_color8)),
                plainText.indexOf(amountText[i]),
                plainText.indexOf(amountText[i]) + amountText[i].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        text = ss
        movementMethod = LinkMovementMethod.getInstance()
    } catch (e: Exception) {
        text = plainText
    }
}