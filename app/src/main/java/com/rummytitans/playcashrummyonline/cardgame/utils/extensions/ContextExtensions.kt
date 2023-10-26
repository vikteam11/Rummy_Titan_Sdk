package com.rummytitans.playcashrummyonline.cardgame.utils.extensions

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File


fun Context.openPdfFile(file: File){
    kotlin.runCatching {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
}
