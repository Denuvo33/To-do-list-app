package com.denuvo.texteditorapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

var notificationId = 1
const val channelId = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"



class Notification() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(context,1,activityIntent,if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)PendingIntent.FLAG_IMMUTABLE else 0)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notiff)
            .setColor(ContextCompat.getColor(context, R.color.orange_900))
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId,notification)
    }
}