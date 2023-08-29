package com.rummytitans.playcashrummyonline.cardgame.ui.rakeback

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.DialogAlertPopupRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.DialogLottieRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentRakebackBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.rakeback.adapter.RakeBackHistoryAdapter
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.playcashrummyonline.cardgame.widget.FontSpan
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject
@AndroidEntryPoint
class RakeBackFragment :  BaseFragment(),RakeBackNavigator{

    lateinit var binding : FragmentRakebackBinding
    lateinit var viewModel: RakeBackViewModel
   // @Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory

    private val rakeBackHistoryAdapter:RakeBackHistoryAdapter by lazy {
        RakeBackHistoryAdapter(arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRakebackBinding.inflate(localInflater ?: localInflater ?: inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this).get(RakeBackViewModel::class.java)

        (requireActivity() as? BaseActivity )?.let {
            viewModel.navigator = it
            viewModel.navigatorAct = this
            viewModel.myDialog = MyDialog(it)
        }
        binding.viewModel = viewModel

        binding.rakebackItemList.setHasFixedSize(true)
        binding.rakebackItemList.adapter = rakeBackHistoryAdapter

        initClicks()
        observeHistoryData()
        viewModel.fetchRakeBackDetail()

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Rakeback
            )
        )
    }

    private fun initClicks() {
        binding.txtRedeem.setOnClickListenerDebounce {
            val amount = viewModel.rakeBackDetail.value?.availableRakeBackAmount?:0.0
            if(amount > 0.0){
                viewModel.myDialog?.getAlertPopup<DialogAlertPopupRummyBinding>(
                    AlertdialogModel(
                        title = getString(R.string.app_name),
                        description = "Are you sure you want to redeem your Rakeback amount?",
                        positiveText = getString(R.string.yes),
                        negativeText = getString(R.string.no),
                        onPositiveClick = {
                            redeemAmount()
                        }
                    ),R.layout.dialog_alert_popup_rummy
                )?.show()
            }else{
                viewModel.navigator.showError("You don't have any rakeback amount for redemption")
            }
        }
    }

    private fun redeemAmount() {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.RedeemRakebackAmount,
                AnalyticsKey.Keys.RedeemAmount to  "${viewModel.rakeBackDetail.value?.availableRakeBackAmount}",
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Rakeback,
            )
        )
        viewModel.redeemRakeBackAmount()
    }

    private fun observeHistoryData() {
        viewModel.rakeBackDetail.observe(viewLifecycleOwner){
            rakeBackHistoryAdapter.updateData(it.redeemHistory)
            setRakebackTerms()
        }
    }

    private fun setRakebackTerms() {
        val clickAbleText = "click here"
        val amountStr = "₹".plus(DecimalFormat("##.##")
            .format(viewModel.rakeBackDetail.value?.totalRakeBackAmount?:0.0))
        val desc = "Your earned rakeback amount $amountStr " +
                "is redeemed into Rummy Ticket. To know more rakeback\nprogram, $clickAbleText"
        val indStart = desc.indexOf(clickAbleText)
        val indEnd = indStart+clickAbleText.length
        val span = SpannableString(desc)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                sendToCloseAbleInternalBrowser(requireActivity(),
                    viewModel.rakeBackDetail.value?.tncUrl?:"",getString(R.string.rakeback))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        span.setSpan(clickableSpan, indStart,indEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(requireContext().getColorInt(R.color.redVariant1)),
            indStart,indEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(ForegroundColorSpan(requireContext().getColorInt(R.color.white)),
            desc.indexOf(amountStr), desc.indexOf(amountStr) + amountStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(FontSpan(ResourcesCompat.getFont(requireActivity(), R.font.rubik_bold)),
            desc.indexOf(amountStr), desc.indexOf(amountStr) + amountStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD),
            desc.indexOf(amountStr), desc.indexOf(amountStr) + amountStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.txtTerms.text= span
        binding.txtTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onRedeemAmount(msg:String) {
        val statusDataModel = BottomSheetStatusDataModel().also {
            it.title = "Congratulations!"
            it.description = "Your rakeback amount is successfully redeemed into Rummy Ticket."
            it.positiveButtonName = "CHECK YOUR TICKET"
            it.isSuccess = true
            it.imageIcon = 0
            it.animationFileId = R.raw.withdrawal_done_anim
            it.showSuccessAnim = R.raw.success_blast_anim
        }
        viewModel.myDialog?.apply {
            val binding = getDataBindingDialog<DialogLottieRummyBinding>(R.layout.dialog_lottie_rummy)
            val dialog = getFullScreenDialog(binding.root)
            dialog.setCancelable(false)
            setViewBackground(binding.layoutBottomSheet.layoutMain,
                requireContext().getColorInt(R.color.text_color6),
                16,
                -1)
            binding.model = statusDataModel

            binding.layoutBottomSheet.btnSubmitDone.setOnClickListener {
                dialog.dismiss()
                // we have to use intent_filter here.
               /* viewModel.fetchRakeBackDetail(true)
                startActivity(Intent(requireActivity(),GamesTicketActivity::class.java))*/
            }
            binding.layoutBottomSheet.imgCross.setOnClickListener {
                viewModel.fetchRakeBackDetail(true)
                dialog.dismiss()
            }
            binding.executePendingBindings()
            dialog.show()
        }
    }
}

interface RakeBackNavigator{
    fun onRedeemAmount(msg:String)
}