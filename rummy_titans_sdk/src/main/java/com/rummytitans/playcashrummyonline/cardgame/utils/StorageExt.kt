package com.rummytitans.playcashrummyonline.cardgame.utils

import com.rummytitans.playcashrummyonline.cardgame.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

fun Activity.openGallery() {
    val intent = Intent()
    intent.type = "*/*"
    intent.action = Intent.ACTION_GET_CONTENT
    startActivityForResult(
        Intent.createChooser(intent, getString(R.string.select)),
        MyConstants.REQUEST_CODE_GALLERY
    )

}

fun Fragment.openGallery() {
    val intent = Intent()
    intent.type = "*/*"
    intent.action = Intent.ACTION_GET_CONTENT
    startActivityForResult(
        Intent.createChooser(intent, getString(R.string.select)),
        MyConstants.REQUEST_CODE_GALLERY
    )

}

fun Activity.openCamera():String {
    var currentPath:String=""
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(packageManager)?.also {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                currentPath = it.absolutePath
                val photoURI: Uri =
                    FileProvider.getUriForFile(this, "${packageName}.provider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, MyConstants.REQUEST_CODE_CAMERA)
            }
        }
    }
    return currentPath
}

fun Fragment.openCamera():String {
    var currentPath:String=""
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
            val photoFile: File? = try {
                requireActivity().createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.also {
                currentPath = it.absolutePath
                val photoURI: Uri =
                    FileProvider.getUriForFile(
                        requireContext(),
                        "${requireActivity().packageName}.provider",
                        it
                    )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, MyConstants.REQUEST_CODE_CAMERA)
            }
        }
    }
    return currentPath
}

@Throws(IOException::class)
private fun Activity.createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}

fun Activity.getMimeType(uri: Uri?):String{
    var docType = ""
    uri?.let {
        val mime = MimeTypeMap.getSingleton()
        docType = mime.getExtensionFromMimeType(contentResolver.getType(it))?:""
    }
    return docType
}

fun Fragment.getMimeType(uri: Uri?):String{
    var docType = ""
    uri?.let {
        val mime = MimeTypeMap.getSingleton()
        docType = mime.getExtensionFromMimeType(requireActivity().contentResolver.getType(it))?:""
    }
    return docType
}

@SuppressLint("NewApi")
@Throws(URISyntaxException::class)
fun Context.getFilePath(uri: Uri?): String? {
    var uri = uri
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    // Uri is different in versions after KITKAT (Android 4.4), we need to
    if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
            applicationContext, uri
        )
    ) {
        if (isExternalStorageDocument(uri!!)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            uri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), id.toLong()
            )
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            if ("image" == type) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            selection = "_id=?"
            selectionArgs = arrayOf(split[1])
        }
    }
    if ("content".equals(uri!!.scheme, ignoreCase = true)) {
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        var cursor: Cursor?
        try {
            cursor = contentResolver
                .query(uri, projection, selection, selectionArgs, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
        }

    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}


private fun isExternalStorageDocument(uri: Uri) =
    "com.android.externalstorage.documents" == uri.authority

private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority

private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority


