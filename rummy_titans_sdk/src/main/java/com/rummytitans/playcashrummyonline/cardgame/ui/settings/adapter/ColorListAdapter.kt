package com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemThemeColorsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import com.rummytitans.playcashrummyonline.cardgame.ui.settings.ColorChooseInterface
import com.rummytitans.playcashrummyonline.cardgame.ui.settings.SettingsViewModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

data class ThemeColorsModel(val regularColorCode: Int, val safeColorCode: Int)

class ColorListAdapter(
    val themeCodeModel: MutableLiveData<SettingsViewModel.ThemeCodeModel>,
    var prePos: Int = -1, val onSafePlay: Boolean,
    val mColorChooseInterface: ColorChooseInterface? = null

) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var count = 0

    lateinit var context: Context
    val mColorList = ArrayList<ThemeColorsModel>()

    init {
        mColorList.add(ThemeColorsModel(R.color.theme1_regular, R.color.theme1_save))
        mColorList.add(ThemeColorsModel(R.color.theme2_regular, R.color.theme2_save))
        mColorList.add(ThemeColorsModel(R.color.theme3_regular, R.color.theme3_save))
        mColorList.add(ThemeColorsModel(R.color.theme4_regular, R.color.theme4_save))
        mColorList.add(ThemeColorsModel(R.color.theme5_regular, R.color.theme5_save))
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding =
            ItemThemeColorsRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PlayerViewHolder(private val mView: ItemThemeColorsRummyBinding) :
        BaseViewHolder(mView.root) {

        override fun onBind(position: Int) {
            val data = mColorList.get(position)
            mView.model = data
            mView.executePendingBindings()
            context.resources.apply {

                if (prePos == position) {
                    val regular = getString(data.regularColorCode)
                    val safe = getString(data.safeColorCode)
                    val color = if (onSafePlay) data.regularColorCode else data.safeColorCode
                    mView.border.setBackgroundColor(ContextCompat.getColor(context, color))

                    themeCodeModel.value =
                        SettingsViewModel.ThemeCodeModel(regular, safe, adapterPosition)
                } else
                    mView.border.setBackgroundColor(getColor(R.color.white_gray))
            }

            mView.root.setOnClickListener {
                if (prePos == -1) {
                    prePos = adapterPosition
                    notifyItemChanged(prePos)
                } else if (prePos != adapterPosition) {
                    notifyItemChanged(prePos)
                    prePos = adapterPosition
                    notifyItemChanged(adapterPosition)
                }
                mColorChooseInterface?.onColorChoose(prePos)
            }
        }
    }

}