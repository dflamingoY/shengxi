package org.xiaoxingqi.shengxi.modules

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.xiaoxingqi.alarmService.oreo
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.*

fun Activity.startNotify() {
    val localIntent = Intent()
    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
    localIntent.data = Uri.fromParts("package", this.packageName, null)
    startActivity(localIntent)
}

@SuppressLint("NewApi")
fun Context.createNotification(channelId: String,
                               importance: Int = 0,
                               name: String,
                               notificationBuilder: Notification.Builder.() -> Unit): Notification {
    oreo {
        val channel = NotificationChannel(channelId, name, importance)
        channel.setSound(null, null)

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }
    //TODO 修改notification 创建方式
    val builder = Notification.Builder(this, channelId)
// when {
//        else -> NotificationCompat.Builder(this, channelId)
//    }
    notificationBuilder(builder)
    return builder.build()
}

/**
 * @param type 0 记录小能手
 */
fun Activity.showAchieve(type: Int = 0) {
    val view = LayoutInflater.from(this).inflate(R.layout.layout_achievement, null, false)
    val toast = Toast.makeText(this, null, Toast.LENGTH_LONG)
    val toastView = toast.view as LinearLayout
    toastView.setPadding(0, 0, 0, 0)
    toastView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    toastView.setBackgroundResource(R.drawable.shape_transparent)
    toastView.removeAllViews()
    toastView.gravity = Gravity.TOP
    toastView.addView(view, LinearLayout.LayoutParams(AppTools.getWindowsWidth(this), AppTools.dp2px(this, 100)))
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, -100)
    toast.show()
}