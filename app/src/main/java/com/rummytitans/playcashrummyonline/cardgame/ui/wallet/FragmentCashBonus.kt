package com.rummytitans.playcashrummyonline.cardgame.ui.wallet

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentCashBonusRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.CashBonusViewModel
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter.CashBonusAdapter
import kotlinx.android.synthetic.main.fragment_cash_bonus_rummy.*
import javax.inject.Inject

class FragmentCashBonus : BaseFragment(), MainNavigationFragment,
    BaseNavigator {

    lateinit var binding: FragmentCashBonusRummyBinding

    //@Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: CashBonusViewModel

    lateinit var adapter: CashBonusAdapter

    var type = 1

    companion object {
        fun newInstance(isActivity: Boolean=false): FragmentCashBonus {
            val frag = FragmentCashBonus()
            val bundle = Bundle()
            bundle.putBoolean("isActivity", isActivity)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this
        ).get(CashBonusViewModel::class.java)
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
        viewModel.fetchCashBonus()
        icBack.setOnClickListener { activity?.onBackPressed() }


        rvCashBonus?.layoutManager = LinearLayoutManager(activity)

        adapter = CashBonusAdapter(ArrayList())
        rvCashBonus?.adapter = adapter

        viewModel.listmodel.observe(viewLifecycleOwner, Observer {
            adapter.updateItems(it)
        })

        swipeRefresh?.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.fetchCashBonus()
        }

    }


    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        println(throwable?.message.toString())
        swipeRefresh?.isRefreshing = false
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        swipeRefresh?.isRefreshing = false
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        message?.let { showErrorMessageView(it) }
    }

    override fun showMessage(message: String?) {
        swipeRefresh?.isRefreshing = false
    }


    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}