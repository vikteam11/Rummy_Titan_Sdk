package com.rummytitans.playcashrummyonline.cardgame.utils.permissions


import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.utils.hasPermissions
import com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.utils.requestPermissionsExt
import com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.utils.shouldShowRational
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class PermissionActivity : Activity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 8011
        const val EXTRA_KEY_PERMISSIONS = "Permissions"

        const val DIALOG_ACTION_POS_PERM_RATIONAL = "Grant Now"
        const val DIALOG_ACTION_NEG_PERM_RATIONAL = "Not Now"

        const val DIALOG_MSG_PERM_MANDATORY =
            "You need to grant required permission from device settings."

        @JvmStatic
        fun startActivityForPermissionWithResult(
            activity: Activity,
            permissions: List<String>,requestCode:Int
        ) {
                activity.startActivityForResult(
                    Intent(activity, PermissionActivity::class.java)
                        .apply {
                            putStringArrayListExtra(
                                EXTRA_KEY_PERMISSIONS,
                                ArrayList(permissions)
                            )
                        },requestCode
                )
        }
        @JvmStatic
        fun startActivityForPermissionWithResult(
            fragment: Fragment,
            permissions: List<String>,requestCode:Int
        ) {
            fragment.startActivityForResult(
                Intent(fragment.requireContext(), PermissionActivity::class.java)
                    .apply {
                        putStringArrayListExtra(
                            EXTRA_KEY_PERMISSIONS,
                            ArrayList(permissions)
                        )
                    },requestCode
            )
        }
    }
    private var alertDialog: AlertDialog? = null

    private lateinit var arrPermissions: Array<String>
    private val LIMIT=2
    private val EMPTY_LIMIT=0
    private var requestPermisstion=LIMIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_location_rummy)
        hideProgressBar()
        intent.getStringArrayListExtra(EXTRA_KEY_PERMISSIONS)
            .let { pList ->
                if (pList.isNullOrEmpty()) {
                    finish()
                } else {
                    arrPermissions = pList.toTypedArray()
                    reqPermissions()
                }
            }
    }

    fun hideProgressBar(){
        findViewById<View>(R.id.progressBar)?.visibility= View.GONE
    }

    private fun reqPermissions() {
        if (hasPermissions(arrPermissions)) {
            onPermissionsAccept()
        } else {
            if (requestPermisstion!=EMPTY_LIMIT){
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

    private fun showDialogRationalPermission() {
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
        startActivityForResult(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }, MyConstants.REQUEST_APP_SETTING)
    }

    private fun onPermissionsAccept() {
        setResult(RESULT_OK)
        finish()
    }

    private fun onPermissionsReject() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPermissions(arrPermissions)) {
                onPermissionsAccept()
            } else {
                if (shouldShowRational(*arrPermissions)) {
                    showDialogRationalPermission()
                } else {
                    onPermissionsReject()
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            MyConstants.REQUEST_APP_SETTING-> reqPermissions()
        }
    }

    override fun onDestroy() {
        alertDialog?.dismiss()
        super.onDestroy()
    }

}

