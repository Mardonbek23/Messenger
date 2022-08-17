package com.example.messenger.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.messenger.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.annotation.TargetApi
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.fragments.MessageChatFragment
import com.example.messenger.retrofit.Notification
import java.util.*


class MyFirbaseService : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("key", p0.data["number"])
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
        var pending = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var builder = NotificationCompat.Builder(this, "channelid")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("${p0.notification!!.title}: ${p0.notification!!.body}")
        builder.setAutoCancel(true)
        builder.setChannelId("channelid")
        builder.setContentIntent(pending)
        builder.setWhen(Calendar.getInstance().timeInMillis)
        var notification = builder.build()

        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("channelid", "name", importance)
            mChannel.description = "descriptionText"
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(Random().nextInt(100), notification)
    }


}