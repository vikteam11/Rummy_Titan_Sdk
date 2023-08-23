package com.rummytitans.playcashrummyonline.cardgame.ui.verifications

import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.listeners.BottomSheetStatusListener
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.BottomsheetDialogChoosePicRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentAddressVerifyManualRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.AddressKycContentModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.adapter.AadhaarVerifyNoteAdapter
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.BottomSheetDialogBinding
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.LottieBottomSheetDialog
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.QuickGuideBottomSheetDialog
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.playcashrummyonline.cardgame.utils.permissions.PermissionActivity
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.widget.cropImage.CropImage
import com.rummytitans.playcashrummyonline.cardgame.widget.cropImage.CropImageView
import com.rummytitans.playcashrummyonline.cardgame.widget.inputFilter.InputRegexFilter
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FragmentManualAddressVerification : BaseFragment(), AddressVerificationNavigator,
    BottomSheetStatusListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddressVerificationViewModel

    lateinit var binding: FragmentAddressVerifyManualRummyBinding

    var uploadingDialog: LottieBottomSheetDialog?= null

    var isFreshUser=false
    var pickFrom = 0

    private var currentPhotoPath = ""

    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE)

    val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA
    )

    companion object{
        fun newInstance(kycNotes: AddressKycContentModel?, selectedDocument:Int): FragmentManualAddressVerification {
            val fragment = FragmentManualAddressVerification()
            val args = Bundle()
            args.putSerializable(MyConstants.INTENT_PASS_KYS_NOTES,kycNotes)
            args.putInt(MyConstants.INTENT_PASS_DOC_SELECTED,selectedDocument)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(
                this.viewModelStore,
                viewModelFactory
            ).get(AddressVerificationViewModel::class.java)
        viewModel.navigatorAct = this
        (activity as? BaseActivity)?.let {
            viewModel.navigator = it
        }
        arguments?.getSerializable(MyConstants.INTENT_PASS_KYS_NOTES)?.let {
            viewModel.updateKycData(it as AddressKycContentModel)
        }
        arguments?.getInt(MyConstants.INTENT_PASS_DOC_SELECTED,1)?.let {
            viewModel.selectedDocument.set(it)
        }

        binding =
            FragmentAddressVerifyManualRummyBinding.inflate(localInflater ?: inflater, container, false)
                .apply {
                    lifecycleOwner = this@FragmentManualAddressVerification
                    viewmodel = this@FragmentManualAddressVerification.viewModel
                    viewModel.myDialog = MyDialog(requireActivity())
                }
        viewModel.colorTextArray = listOf("Note:")
        viewModel.colorArray = listOf(R.color.text_color8,R.color.text_color2)
        viewModel.fontArray = listOf(R.font.rubik_medium,R.font.rubik_regular)
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotes.adapter = AadhaarVerifyNoteAdapter(
            viewModel.kycNotes.value?.ManualNotes?: arrayListOf(),
            true
        )

        initClicks()

        if(viewModel.selectedDocument.get() == 1){
            (activity as? AddressVerificationActivity)?.setTitle(getString(R.string.upload_aadhar))
        }else{
            (activity as? AddressVerificationActivity)?.setTitle(getString(R.string.upload_license))
        }
    }

    private fun initClicks() {

        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR,-18)
        val datePickerDialog = DatePickerDialog(
            requireActivity(), { _, p1, p2, p3 ->
                //val dob = p1.toString()+ "-" + (p2 + 1).toString() + "-" + p3.toString()
                val calendar = Calendar.getInstance()
                calendar.set(p1,p2,p3)
                val format = SimpleDateFormat("yyyy-MM-dd")
                val strDate = format.format(calendar.time)
                binding.layoutDl.editDob.setText(strDate)
                viewModel.dob = strDate
                viewModel.setButtonEnabledOrDisabled()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = cal.timeInMillis

        binding.layoutDl.editDob.setOnClickListener {
            datePickerDialog.show()
        }

        binding.layoutDl.txtQuickGuide.setOnClickListenerDebounce {
            QuickGuideBottomSheetDialog(
                requireContext(),
                viewModel.selectedColor.get()?:"",
                viewModel.prefs.seletedLanguage?:""
            ).show()
        }
        binding.layoutDl.editDLNumber.filters = arrayOf(
            InputRegexFilter("[A-Za-z0-9]*"),
            LengthFilter(20),
            InputFilter.AllCaps()
        )
        binding.layoutDl.editDLNumber.addTextChangedListener {
           if (it.toString().isNotEmpty()) binding.layoutDl.editDLNumber.error = null

            viewModel.dlNumber = it.toString()
            viewModel.setButtonEnabledOrDisabled()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == MyConstants.REQUEST_CODE_GALLERY) {
                    val uri = data?.data
                    val type = getMimeType(uri)
                    if (type.contains("jpg", ignoreCase = true)
                        or type.contains("jpeg", ignoreCase = true)
                        or type.contains("png", ignoreCase = true)
                    ) {
                        CropImage.activity(uri)
                            .setGuidelines(CropImageView.Guidelines.OFF)
                            .setBorderCornerOffset(8f)
                            .start(requireContext(), this)
                    } else {
                        showErrorMessageView(R.string.only_jpg_png_pdf_supported)
                    }
                } else if (requestCode == MyConstants.REQUEST_CODE_CAMERA) {
                    val file = File(currentPhotoPath)
                    CropImage.activity(Uri.fromFile(file))
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setBorderCornerOffset(8f)
                        .start(requireContext(), this)
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    lifecycleScope.launch {
                        CropImage.getActivityResult(data)?.also { result ->
                            val resultUri = result.uri
                            ImageCompressor.getCompressedImage(requireContext(), resultUri) {
                                val selFile = File(it)
                                val fileSize = (selFile.length() / 1024) / 1024
                                if (fileSize <= 5) {
                                    if (viewModel.selectedDocSide.get() == MyConstants.DOC_SIDE_FRONT) {
                                        viewModel.frontImgUrl.set(it)
                                        viewModel.frontImgName.set(selFile.name)
                                    } else {
                                        viewModel.backImgUrl.set(it)
                                        viewModel.backImgName.set(selFile.name)
                                    }
                                    viewModel.setButtonEnabledOrDisabled()
                                } else {
                                    showErrorMessageView("File size is too large")
                                }

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                getString(R.string.unable_to_retrieve_file).let {
                    showErrorMessageView(it)
                }

            }
        }

        if(requestCode == PermissionActivity.PERMISSION_REQUEST_CODE ){
            if(resultCode == Activity.RESULT_OK){
                if(pickFrom == 0){
                   currentPhotoPath = openCamera()
                }else{
                   openGallery()
                }
            }else{
                onPermissionReject()
            }
        }

    }

    private fun onPermissionReject(){
        showErrorMessageView("Please allow permission to upload document.")
    }

    override fun showChooseUploadSheet(uploadType: Int) {
        BottomSheetDialogBinding<BottomsheetDialogChoosePicRummyBinding>(
            requireContext(),
            R.layout.bottomsheet_dialog_choose_pic_rummy
        ).apply {
            binding.btnCamera.setOnClickListener {
                pickFrom = 0
                PermissionActivity.startActivityForPermissionWithResult(
                    this@FragmentManualAddressVerification,
                    cameraPermissions.toList(),
                    PermissionActivity.PERMISSION_REQUEST_CODE
                )
                dismiss()
            }

            binding.btnGallery.setOnClickListener {
                pickFrom = 1
                PermissionActivity.startActivityForPermissionWithResult(
                    this@FragmentManualAddressVerification,
                    permissions.toList(),
                    PermissionActivity.PERMISSION_REQUEST_CODE
                )
                dismiss()
            }
            val titleMsg = if (viewModel.selectedDocument.get() == 1)
                if (viewModel.selectedDocSide.get() == MyConstants.DOC_SIDE_FRONT) " Front" else " Back"
            else
                ""
            binding.txtTitle.text = viewModel.selectedDocName.get() + titleMsg
            show()
        }
    }

    override fun onVerificationSuccess(){
        viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick ,
            bundleOf(AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.GoBack,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
        )

        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onVerificationFailed() {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick ,
            bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.TryAgain,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
        )
        viewModel.frontImgUrl.set("")
        viewModel.frontImgName.set("")
        viewModel.backImgName.set("")
        viewModel.backImgUrl.set("")
        binding.layoutDl.editDLNumber.setText("")
        binding.layoutDl.editDob.setText("")
        viewModel.dlNumber = ""
        viewModel.dob = ""
        viewModel.setButtonEnabledOrDisabled()
    }

    override fun showUploadingSheet(statusDataModel: BottomSheetStatusDataModel) {
        if (uploadingDialog == null) {
            uploadingDialog = LottieBottomSheetDialog(requireContext(), statusDataModel, this)
        } else {
            uploadingDialog?.updateData(statusDataModel)
        }
        uploadingDialog?.show()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        (activity as? BaseActivity)?.hideKeyboardView()
    }

}

