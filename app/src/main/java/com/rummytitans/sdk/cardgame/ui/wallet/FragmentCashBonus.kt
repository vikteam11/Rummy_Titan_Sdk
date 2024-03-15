package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.FragmentCashBonusRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.CashBonusViewModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rummytitans.sdk.cardgame.RummyTitanSDK

import com.rummytitans.sdk.cardgame.ui.wallet.adapter.CashBonusAdapter

class FragmentCashBonus : BaseFragment(), MainNavigationFragment,
    BaseNavigator {

    lateinit var binding: FragmentCashBonusRummyBinding

    lateinit var viewModel: CashBonusViewModel

    lateinit var adapter: CashBonusAdapter

    var type = 1
    var url = ""

    companion object {
        fun newInstance(isActivity: Boolean=false,title:String=""): FragmentCashBonus {
            val frag = FragmentCashBonus()
            val bundle = Bundle()
            bundle.putBoolean("isActivity", isActivity)
            bundle.putString("title", title)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this
        ).get(CashBonusViewModel::class.java)

        viewModel.title = arguments?.getString("title") ?: getString(R.string.game_bonus)
        binding =
            FragmentCashBonusRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
                lifecycleOwner = this@FragmentCashBonus
                viewmodel = this@FragmentCashBonus.viewModel
                viewModel.myDialog = MyDialog(requireActivity())
            }

        arguments?.getBoolean("isActivity")?.let {
            viewModel.isAvtivity.set(it)
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigator = this
        viewModel.isLoading.set(true)

        binding.icBack.setOnClickListener { requireActivity().onBackPressed() }

        url =
            if (viewModel.title.equals("Credit Bonus", ignoreCase = true) || viewModel.title.equals(
                    "Game Bonus",
                    ignoreCase = true
                )
            ) "transaction/v1/bonus/list"
            else if (viewModel.title.equals("Conversion Bonus", ignoreCase = true)) "transaction/v1/conversion-bonus/list"
            else "transaction/v1/promo-bonus/list"


        viewModel.fetchCashBonus(url)

        binding.rvCashBonus.layoutManager = LinearLayoutManager(requireActivity())
        adapter = CashBonusAdapter(ArrayList())
        binding.rvCashBonus.adapter = adapter

        viewModel.listmodel.observe(viewLifecycleOwner, Observer {
            adapter.updateItems(it)
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.fetchCashBonus(url)
        }

    }


    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        println(throwable?.message.toString())
        binding.swipeRefresh?.isRefreshing = false
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        binding.swipeRefresh?.isRefreshing = false
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        message?.let { showErrorMessageView(it) }
    }

    override fun showMessage(message: String?) {
        binding.swipeRefresh?.isRefreshing = false
    }


    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        RummyTitanSDK.rummyCallback?.logoutUser()
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}