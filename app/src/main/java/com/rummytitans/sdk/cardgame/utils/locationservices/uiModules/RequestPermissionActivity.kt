package com.rummytitans.sdk.cardgame.utils.locationservices.uiModules

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.utils.bottomsheets.AlertBottomSheetDialog
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.REQUEST_OPEN_APP_SETTINGS
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LOCATION_OK
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE__LOCATION_FAILED
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationProviderPluginConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.models.ModelPermission
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.*
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog

class RequestPermissionActivity : Activity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 8011
        const val EXTRA_KEY_PERMISSIONS_MODELS = "PermissionModels"

        const val DIALOG_ACTION_POS_PERM_RATIONAL = "Grant Now"
        const val DIALOG_ACTION_NEG_PERM_RATIONAL = "Not Now"

        const val DIALOG_MSG_PERM_MANDATORY =
            "You need to grant required location permission from device settings."
        const val DIALOG_ACTION_POS_PERM_MANDATORY = "Okay"

        @JvmStatic
        fun startActivityForPermissionWithResult(
            activity: Activity,
            permissions: List<ModelPermission>, requestCode:Int
        ) {
                activity.startActivityForResult(
                    Intent(activity, RequestPermissionActivity::class.java)
                        .apply {
                            putParcelableArrayListExtra(
                                EXTRA_KEY_PERMISSIONS_MODELS,
                                ArrayList(permissions)
                            )
                        },requestCode
                )
        }
    }

    private var mLocationDialog: AlertBottomSheetDialog?=null
    private var alertDialog: AlertDialog? = null
    private lateinit var arrPermissions: Array<String>
    private lateinit var arrPermissionsMandatory: Array<String>
    private lateinit var listPermissionModels: ArrayList<ModelPermission>
    private val LIMIT=2
    private val EMPTY_LIMIT=0
    private var requestPermisstion=LIMIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_location_rummy)
        hideProgressBar()
        intent.getParcelableArrayListExtra<ModelPermission>(EXTRA_KEY_PERMISSIONS_MODELS)
            .let { pList ->
                if (pList.isNullOrEmpty()) {
                    finish()
                } else {
                    arrPermissions = pList.map { it.permName }.toTypedArray()
                    arrPermissionsMandatory =
                        pList.filter { it.isMandatory }.map { it.permName }.toTypedArray()
                    listPermissionModels = pList
                    reqPermissions()
                }
            }
    }

    fun hideProgressBar(){
        findViewById<View>(R.id.progressBar)?.visibility= View.GONE
    }

    private fun reqPermissions() {
        if (hasPermissions(arrPermissions)) {
            Log.e("locationTEst","allredy pemissions")
            onPermissionsAccept()
        } else {
            if (requestPermisstion!=EMPTY_LIMIT){
                Log.e("locationTEst","request for  pemissions")
                requestPermisstion-=1
                requestPermissionsExt(
                    arrPermissions,
                    PERMISSION_REQUEST_CODE
                )
            }else{
                onPermissionsReject()
            }
        }
    }

    private fun alertDialog(
        msg: String,
        posTxt: String,
        negTxt: String? = null,
        finishProcess: Boolean = false
    ) {
        dismissDialog()
        AlertDialog.Builder(this).let { builder ->
            builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(posTxt) { dialog, _ ->
                    dialog.dismiss()
                    if (finishProcess)
                        onPermissionsReject()
                }
            if (!negTxt.isNullOrBlank()) {
                builder.setNegativeButton(negTxt) { dialog, _ ->
                    onPermissionsReject()
                    dialog.dismiss()
                    finish()
                }
            }
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    private fun alertUser() {
        if (requestPermisstion==0){
            alertDialog(LocationProviderPluginConstants.DIALOG_MSG_NOT_ALLOW, "Exit", finishProcess = true)
        }else
            showDialogMandatoryPermission()

    }

    private fun showDialogMandatoryPermission() {
        dismissDialog()
        AlertDialog.Builder(this).let { builder ->
            builder.setMessage(DIALOG_MSG_PERM_MANDATORY)
                .setCancelable(false)
                .setPositiveButton(DIALOG_ACTION_POS_PERM_MANDATORY) { dialog, _ ->
                    reqPermissions()
                    dialog.dismiss()
                }
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    private fun showDialogRationalPermission() {
        dismissDialog()
        AlertDialog.Builder(this).let { builder ->
            builder.setMessage(DIALOG_MSG_PERM_MANDATORY)
                .setCancelable(false)
                .setPositiveButton(DIALOG_ACTION_POS_PERM_RATIONAL) { dialog, _ ->
                    requestGoToAppSettings()
                    dialog.dismiss()
                }.setNegativeButton(DIALOG_ACTION_NEG_PERM_RATIONAL) { dialog, _ ->
                    onPermissionsReject()
                    dialog.dismiss()
                    finish()
                }
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    private fun requestGoToAppSettings(){
        Log.e("locationTEst","open intent for enable location")
        goToAppSettings()
    }

    private fun onPermissionsAccept() {
        if (isLocationEnabled()) {
            Log.e("locationTEst","location enabled")
            setResult(RESPONSE_LOCATION_OK)
            finish()
        } else {
            requestGoToAppSettings()
        }
    }

    private fun onPermissionsReject() {
        setResult(RESPONSE__LOCATION_FAILED)
        finish()
    }

    private fun dismissDialog() {
        alertDialog?.let { dialog ->
            if (dialog.isShowing) dialog.dismiss()
            alertDialog = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPermissions(arrPermissionsMandatory)) {
                Log.e("locationTEst","granted pemissions")
                onPermissionsAccept()
            } else {
                if (shouldShowRational(*arrPermissionsMandatory)) {
                    Log.e("locationTEst","rational  pemissions")
                    showAllowBottomSheet()
                } else {
                    if (arrPermissionsMandatory.isEmpty()) {
                        Log.e("locationTEst","no mendotery  pemissions exit")
                        onPermissionsReject()
                        finish()
                    } else {
                        Log.e("locationTEst","request for mandotry pemissions")
                        showAllowBottomSheet()
                    }
                }
            }
        }
    }

    fun showAllowBottomSheet(){
        if (mLocationDialog==null)
            mLocationDialog=AlertBottomSheetDialog(this,R.layout.bottomsheet_allow_location_rummy,
                positiveBtnId = R.id.btnEnableLocation,
                negativeBtnId = R.id.imgCross, onPositiveClick =  {
                requestGoToAppSettings()
            }, onNegativeClick = {
                onPermissionsReject()
        })
        if (mLocationDialog?.isShowing==false) mLocationDialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQUEST_OPEN_APP_SETTINGS-> reqPermissions()
        }
    }

    override fun onDestroy() {
        dismissDialog()
        super.onDestroy()
    }

}

