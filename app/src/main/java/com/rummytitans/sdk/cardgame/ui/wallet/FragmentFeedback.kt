package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.FragmentFeedbackRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.more.module.FeedbackViewModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import kotlinx.android.synthetic.main.fragment_feedback_rummy.*
import kotlinx.android.synthetic.main.fragment_feedback_rummy.icBack
import javax.inject.Inject


class FragmentFeedback : BaseFragment(), MainNavigationFragment,
    BaseNavigator {

    lateinit var binding: FragmentFeedbackRummyBinding
    lateinit var viewModel: FeedbackViewModel
    lateinit var spinnerTextView: TextView
    var b: Boolean = false

    companion object {
        fun newInstance() = FragmentFeedback()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this
        ).get(FeedbackViewModel::class.java)

        binding = FragmentFeedbackRummyBinding.inflate(
            localInflater ?: localInflater ?: inflater,
            container,
            false
        ).apply {
            lifecycleOwner = this@FragmentFeedback
            viewmodel = this@FragmentFeedback.viewModel
        }

        binding.edtFeedback.addTextChangedListener {
            if (it.toString().isNotEmpty())
                binding.inputLayout.error = null
        }

        binding.editTitle.addTextChangedListener {
            if (it.toString().isNotEmpty())
                binding.inputLayout1.error = null
        }
        binding.editMessage.addTextChangedListener {
            if (it.toString().isNotEmpty())
                binding.inputLayout2.error = null
        }

        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener(null)

        viewModel.navigator = this@FragmentFeedback
        viewModel.myDialog = MyDialog(requireActivity())

        icBack.setOnClickListener {
            goBack()
        }
        binding.edtFeedback.setOnClickListener {
            //  b = true
            binding.spnFeedbackType.performClick()
        }

        binding.spnFeedbackType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                spinnerTextView = view as TextView
                view.textSize = 12.0f
                val selectedItem = parent.getItemAtPosition(position).toString()
                binding.edtFeedback.setText(selectedItem)

                if (selectedItem.equals("Select Feedback Category", true)) {
                    view.setTextColor(resources.getColor(R.color.cool_grey))

                    // do your stuff
                }

            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSubmit.setOnClickListener {
            if (binding.spnFeedbackType.selectedItemId == 0L) {
                binding.inputLayout.error = "Please select feedback category!"
            } else if (TextUtils.isEmpty(binding.edtFeedback.text)) {
                binding.inputLayout.error = "Please select feedback category!"
            } else if (TextUtils.isEmpty(binding.editTitle.text)) {
                binding.inputLayout1.error = getString(R.string.enter_title)
            } else if (TextUtils.isEmpty(binding.editMessage.text)) {
                binding.inputLayout2.error = getString(R.string.enter_your_message)
            } else {
                viewModel.submit(
                    editTitle.text.toString(), editMessage.text.toString(),
                    binding.spnFeedbackType.selectedItemId
                )
            }
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }


    override fun handleError(throwable: Throwable?) {
        if (TextUtils.isEmpty(throwable?.message)) return
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "", false)
    }

    override fun showError(message: String?) {
        if (TextUtils.isEmpty(message)) return
        message?.let { showErrorMessageView(it) }

    }

    override fun showError(message: Int) {
        if (message == 0) return
        showErrorMessageView(message)
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}

