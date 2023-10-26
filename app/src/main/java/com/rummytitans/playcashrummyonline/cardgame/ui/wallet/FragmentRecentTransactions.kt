package com.rummytitans.playcashrummyonline.cardgame.ui.wallet

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentRecentTransactionRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.TransactionModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.RecentTransactionViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.withdrawal.WithdrawDetailActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.transactions.RecentTranscationAdapter
import com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper
import com.rummytitans.playcashrummyonline.cardgame.utils.extensions.openPdfFile
import com.rummytitans.playcashrummyonline.cardgame.utils.permissions.PermissionActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.sendToInternalBrowser
import com.rummytitans.playcashrummyonline.cardgame.widget.EndlessRecyclerView
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.fragment_recent_transaction_rummy.*
import java.io.File

class FragmentRecentTransactions : BaseFragment(), MainNavigationFragment,
    BaseNavigator,
    TransactionItemNavigator, EndlessRecyclerView.Pager,SortItemListener {

    lateinit var binding: FragmentRecentTransactionRummyBinding


    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: RecentTransactionViewModel

    internal var PERMISSION_REQUEST_CODE = 11
    internal var permisions =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    lateinit var itemData: TransactionModel.TransactionListModel

    companion object {
        fun newInstance(selectedTab: Int, currentBalance: Double): FragmentRecentTransactions {
            val frag = FragmentRecentTransactions()
            val bundle = Bundle()
            bundle.putInt("selectedTab", selectedTab)
            bundle.putDouble("currentBalance", currentBalance)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this).get(RecentTransactionViewModel::class.java)
        binding =
            FragmentRecentTransactionRummyBinding.inflate(localInflater ?: inflater, container, false)
                .apply {
                    lifecycleOwner = this@FragmentRecentTransactions
                    viewmodel = this@FragmentRecentTransactions.viewModel
                    viewModel.myDialog = MyDialog(requireActivity())
                }

        binding.executePendingBindings()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            if (TextUtils.isEmpty(LocaleHelper.getLanguage(it))) {
                LocaleHelper.setLocale(
                    it, getString(R.string.english_code), getString(R.string.english)
                )
            } else {
                LocaleHelper.onAttach(it)
            }
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigator = this
        viewModel.navigatorAct = this
        if (arguments?.containsKey("currentBalance")!!)
            viewModel.currentBalance.set(arguments?.getDouble("currentBalance", 0.0))
        else
            viewModel.fetchCurrentBalance()
        binding.initView()
        viewModel.initApis()
        viewModel.fetchRecentTransaction()
    }

    private fun RecentTransactionViewModel.initApis(){
        sortedTransactions.observe(viewLifecycleOwner) {
            (binding.rvTransaction.adapter as? RecentTranscationAdapter)
                ?.updateItems(
                    it as ArrayList<TransactionModel.TransactionListModel>,
                    viewModel.tabName
                )
        }

        transactionDetail.observe(viewLifecycleOwner) {
            (binding.rvTransaction.adapter as? RecentTranscationAdapter)
                ?.updateModel(it)
        }
    }

    private fun FragmentRecentTransactionRummyBinding.initView(){
        icBack.setOnClickListener { activity?.onBackPressed() }
        rvTransaction.apply {
            layoutManager = LinearLayoutManager(activity)
            setPager(this@FragmentRecentTransactions)
            setProgressView(R.layout.layout_load_more_rummy)
            adapter = RecentTranscationAdapter(ArrayList(), this@FragmentRecentTransactions,
                viewModel.selectedColor.get()?:"#0088ff").apply {
                setHasStableIds(true)
            }
        }

        rvSort.apply {
            if (adapter==null){
                adapter =
                    SortAdapter(this@FragmentRecentTransactions, SortAdapter.VIEW_TRAN_SORT).apply {
                        mLastIndex=0
                        sortItemList.add(SortItemModel("All"))
                        sortItemList.add(SortItemModel("Add Cash"))
                        sortItemList.add(SortItemModel("Withdrawal"))
                        sortItemList.add(SortItemModel("Join"))
                        sortItemList.add(SortItemModel("Winning"))

                        sortItemList[0].sortType.set(1)
                    }
                layoutManager = LinearLayoutManager(context)
            }
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.pageNo =1
            viewModel.fetchRecentTransaction()
        }

        ivSupport.setOnClickListener {
            startActivity(
                Intent(activity, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "RecentTransaction")
            )
        }
    }

    override fun onTrackDetailRequest(data: TransactionModel.TransactionListModel) {
        startActivityForResult(Intent(requireActivity(), WithdrawDetailActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_TRANSACTION_ID,data.TxnId),1001)
    }



    override fun asyncRequestDetails(data: TransactionModel.TransactionListModel) {
        viewModel.fetchTransactionDetails(data)
    }


    override fun goBack() {
        activity?.onBackPressed()
    }


    override fun handleError(throwable: Throwable?) {
        swipeRefresh?.isRefreshing = false
    }

    override fun showMessage(message: String?) {
        swipeRefresh?.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
    }

    override fun showError(message: String?) {
        swipeRefresh?.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
    }

    override fun showError(message: Int) {
        swipeRefresh?.isRefreshing = false
        if (message == 0) return
    }

    override fun logoutUser() {
        swipeRefresh?.isRefreshing = false
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun onDownloadClick(data: TransactionModel.TransactionListModel) {
        itemData = data
        if (hasPermissions()) {
            val downloadManager =
                activity?.getSystemService(DaggerAppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            val Download_Uri = Uri.parse(data.Url) // Path where you want to download file.
            val request = DownloadManager.Request(Download_Uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(true)
            request.setVisibleInDownloadsUi(true)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "RummyTitans_" + data.ID + ".pdf"
            )
            request.setTitle(getString(R.string.app_name))
            request.setMimeType("application/pdf")
            request.setDescription("Downloading Transaction History")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager.enqueue(request)
            showMessage("Downloading Start. You can check progress in notification bar.")
        } else {
            requestPermission()
        }
    }


    override fun onSortClick(type: Int, isSortOn: Boolean,sortingName:String) {
        with(viewModel) {
            showSortView.set(false)
            isDataSorted.set(isSortOn)
            viewModel.currentSortType = if (isSortOn) type else  0
            viewModel.pageNo = 1
            viewModel.fetchRecentTransaction()
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in permisions) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permisions,
                PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PermissionActivity.PERMISSION_REQUEST_CODE ){
            if(resultCode == Activity.RESULT_OK){
                viewModel.downLoadInvoice()
            }else{
                showMessageView("Please allow required permission to download InVoice")
            }
        }else if(resultCode==Activity.RESULT_OK)
            viewModel.fetchRecentTransaction()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onDownloadClick(itemData)
            } else {
                showError(activity?.getString(R.string.permission_denied))
            }
        }
    }

    override fun shouldLoad() = viewModel.loadNextPage

    override fun loadNextPage() {
        viewModel.fetchRecentTransaction()
    }

    override fun sendToWebView(url: String) {
        sendToInternalBrowser(
            requireActivity(),
            url,
            getString(R.string.replay)
        )
    }
    override fun downloadInvoiceUrl(transaction:TransactionModel.TransactionListModel) {
        viewModel.transaction = transaction
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModel.downLoadInvoice()
        } else {
            PermissionActivity.startActivityForPermissionWithResult(
                this@FragmentRecentTransactions,
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PermissionActivity.PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun openInvoicePdfFile(file: File) {
        binding.root.postDelayed({
            requireContext().openPdfFile(file)
        },500)
    }
}

interface TransactionItemNavigator {
    fun sendToWebView(url:String){}
    fun onDownloadClick(data: TransactionModel.TransactionListModel)
    fun downloadInvoiceUrl(transaction:TransactionModel.TransactionListModel){}
    fun openInvoicePdfFile(file: File){}
    fun onTrackDetailRequest(data: TransactionModel.TransactionListModel)
    fun asyncRequestDetails(data: TransactionModel.TransactionListModel)
}