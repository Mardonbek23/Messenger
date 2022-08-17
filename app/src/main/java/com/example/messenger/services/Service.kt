package com.example.messenger.services

import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.DOWNLOAD_SERVICE
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import java.io.File

class Service {
    fun saveImage(urlDownload:String,context: Context){
        val cm =context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val NInfo = cm.activeNetworkInfo
        if (NInfo != null && NInfo.isConnectedOrConnecting) {
            try {
                val dm =context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val downloadUri = Uri.parse(urlDownload)
                val request = DownloadManager.Request(downloadUri)
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("Download")
                    .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                        File.separator + "Download" + ".jpg")
                dm.enqueue(request)
                Toast.makeText(context, "Rasm yuklandi!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Rasm yuklanmadi!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Internetda hatolik", Toast.LENGTH_SHORT).show()
        }
    }
}