package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.databinding.FragmentAddCashRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.AddCashViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_add_cash_rummy.*
import javax.inject.Inject

class FragmentAddCash : BaseFragment(), MainNavigationFragment {

    lateinit var binding: FragmentAddCashRummyBinding
    lateinit var viewModel: AddCashViewModel

    companion object {
        fun newInstance(currentBalance: Double): FragmentAddCash {
            val frag = FragmentAddCash()
            val bundle = Bundle()
            bundle.putDouble("currentBalance", currentBalance)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this
        ).get(AddCashViewModel::class.java)
        binding =
            FragmentAddCashRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
                lifecycleOwner = this@FragmentAddCash
                viewmodel = this@FragmentAddCash.viewModel
            }
        binding.rvOffers.layoutManager = LinearLayoutManager(activity)
//        binding.rvOffers.adapter = OffersAdapter(ArrayList())
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icBack.setOnClickListener { activity?.onBackPressed() }
        viewModel.currentBalance.set(arguments?.getDouble("currentBalance"))

        edtAddAmmount.addTextChangedListener {
            if (it?.toString()?.length!! > 0) {
                viewModel.addCashAmmount.set((it.toString().toDouble()))
                edtAddAmmount.setSelection(edtAddAmmount.text.length)
            } else {
                edtAddAmmount.setText("0")
                edtAddAmmount.setSelection(edtAddAmmount.text.length)
            }

        }

    }
}