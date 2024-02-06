package com.rummytitans.sdk.cardgame.ui.verifications

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.verifications.viewmodels.PanVerificationViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.permissions.PermissionActivity
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.rummytitans.sdk.cardgame.databinding.FragmentPanVerificationRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.widget.cropImage.CropImage
import com.rummytitans.sdk.cardgame.widget.cropImage.CropImageView
import kotlinx.android.synthetic.main.dialog_choose_pic_rummy.*
import kotlinx.android.synthetic.main.fragment_bank_verification_rummy.*
import kotlinx.android.synthetic.main.fragment_pan_verification_rummy.*
import kotlinx.android.synthetic.main.fragment_pan_verification_rummy.imgDeleteImage
import kotlinx.android.synthetic.main.fragment_pan_verification_rummy.imgPanCard
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

class FragmentPanVerification : BaseFragment(), RequestVarificationInterface,
    MainNavigationFragment, BaseNavigator {

    lateinit var binding: FragmentPanVerificationRummyBinding
    lateinit var viewModel: PanVerificationViewModel

    private var currentPhotoPath = ""

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE)

    private val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA
    )

    companion object {
        fun newInstance() = FragmentPanVerification()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTheme(inflater)
        viewModel =
            ViewModelProvider(this).get(PanVerificationViewModel::class.java)
        binding =
            FragmentPanVerificationRummyBinding.inflate(localInflater ?: inflater, container, false)
                .apply {
                    lifecycleOwner = this@FragmentPanVerification
                    viewModel.navigator = this@FragmentPanVerification
                    viewmodel = this@FragmentPanVerification.viewModel
                    viewModel.myDialog = MyDialog(requireActivity())
                }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getState()
        viewModel.mRequestVarifyInterface = this
        btnVerifyPan.setOnClickListener {
            viewModel.SubmitPanDetails(
                name = editPanUserName.text.toString(),
                number = editPanNumber.editableText.toString(),
                dob = editPanDOB.text.toString()
            )
        }

        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireActivity(),R.style.RummySdkDatePickerDialogTheme, { _, p1, p2, p3 ->
                val dob = p3.toString() + "/" + (p2 + 1).toString() + "/" + p1.toString()
                editPanDOB.setText(dob)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
        val date = Date()
        date.year = date.year - 18
        datePickerDialog.datePicker.maxDate = date.time

        editPanDOB.setOnClickListener {
            datePickerDialog.show()
        }


        btnSelectPanCardImage.setOnClickListener {
            if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return@setOnClickListener
            showChooseDialog()
        }
        spnState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0)
                    viewModel.selectedState = ""
                else viewModel.selectedState = (view as TextView?)?.text as String? ?: ""
            }
        }

        imgDeleteImage.setOnClickListener { viewModel.imageUrl.value = "" }
    }

    override fun fireBranchEvent(userId: Int) {
        activity?.let {
            viewModel.analyticsHelper.fireAttributesEvent(
                AnalyticsEventsKeys.PAN_VARIFY_REQUESTED, userId.toString()
            )
        }
    }

    var pickFrom = 0

    private fun showChooseDialog() {
        val d = MyDialog(requireActivity()).getMyDialog(R.layout.dialog_choose_pic_rummy)
        d.show()
        d.txtCamera.setOnClickListener {
            d.dismiss()
            pickFrom = 0
            PermissionActivity.startActivityForPermissionWithResult(
                this,
                cameraPermissions.toList(),
                PermissionActivity.PERMISSION_REQUEST_CODE
            )
        }
        d.txtGallery.setOnClickListener {
            d.dismiss()
            pickFrom = 1
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                openGallery()
            }else{
                PermissionActivity.startActivityForPermissionWithResult(
                    this,
                    permissions.toList(),
                    PermissionActivity.PERMISSION_REQUEST_CODE
                )
            }
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
                        or type.contains("png", ignoreCase = true)){
                        CropImage.activity(uri)
                            .setGuidelines(CropImageView.Guidelines.OFF)
                            .setBorderCornerOffset(8f)
                            .start(requireContext(), this)
                        lifecycleScope.launch {
                           /* ImageCompressor.getCompressedImage(requireActivity(),uri){
                                val bmOptions = BitmapFactory.Options()
                                val bitmap1 = BitmapFactory.decodeFile(it, bmOptions)
                                imgPanCard.setImageBitmap(bitmap1)
                                viewModel.imageUrl.value = it
                            }*/
                        }
                    }else{
                        showError(R.string.only_jpg_png_pdf_supported)
                        /*val path = requireContext().getFilePath(uri)?:""
                        val file = File(path)
                        when {
                            file.extension.contains("pdf",false) -> {
                                imgPanCard.setImageResource(R.drawable.ic_pdf_selected)
                                viewModel.imageUrl.value = path
                            }
                            else -> showError(R.string.only_jpg_png_pdf_supported)
                        }*/
                    }
                } else if (requestCode == MyConstants.REQUEST_CODE_CAMERA) {
                    val oldFile = File(currentPhotoPath)
                    CropImage.activity(Uri.fromFile(oldFile))
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setBorderCornerOffset(8f)
                        .start(requireContext(), this)
                  /*  lifecycleScope.launch {
                        val oldFile = File(currentPhotoPath)
                        ImageCompressor.getCompressedImage(requireActivity(),Uri.fromFile(oldFile)){
                            val bmOptions = BitmapFactory.Options()
                            val bitmap1 = BitmapFactory.decodeFile(it, bmOptions)
                            imgPanCard.setImageBitmap(bitmap1)
                            viewModel.imageUrl.value = it
                            runTextRecognition(bitmap1)
                        }
                    }*/
                }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    lifecycleScope.launch {
                        CropImage.getActivityResult(data)?.also { result ->
                            val resultUri = result.uri
                            ImageCompressor.getCompressedImage(requireContext(), resultUri) {
                                val bmOptions = BitmapFactory.Options()
                                val bitmap1 = BitmapFactory.decodeFile(it, bmOptions)
                                val selFile = File(it)
                                val fileSize = (selFile.length() / 1024) / 1024
                                if (fileSize <= 5) {
                                    imgPanCard.setImageBitmap(bitmap1)
                                    viewModel.imageUrl.value = it
                                    runTextRecognition(bitmap1)
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
                    currentPhotoPath= openCamera()
                }else{
                    openGallery()
                }
            }else{
                onPermissionReject()
            }
        }
    }


    fun runTextRecognition(mSelectedImage: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(mSelectedImage)
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        recognizer.processImage(image).addOnSuccessListener {
            processTextRecognize(it)
        }
    }


    fun processTextRecognize(text: FirebaseVisionText) {
        val blocks = text.textBlocks
        if (blocks.size != 0) {
            for (i in 0 until blocks.size) {
                val lines = blocks.get(i).lines
                for (j in 0 until lines.size) {
                    val elements = lines.get(j).elements
                    for (k in 0 until elements.size) {
                        val s = elements.get(k).text
                        println(s)
                        if (validPancard(s)) {
                            editPanNumber.setText(s)
                        }
                    }
                }
            }
        }
    }

    override fun goBack() {
        try {
            activity?.onBackPressed()
        } catch (e: Exception) {
        }
    }

    override fun handleError(throwable: Throwable?) {
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        message?.let {
            showErrorMessageView(activity?.getString(it) ?: "")
        }
    }

    override fun showMessage(message: String?) {
        message?.let {
            showMessageView(it, isBackOnDismiss = true)
        }
    }

    private fun onPermissionReject(){
        showErrorMessageView("Please allow permission to upload document.")
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}