package com.tencent.qcloud.tim.demo.map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 *
 * @author cjianquan
 * @since 2014年10月29日
 */
public class AlarmReceiver extends BroadcastReceiver {

    private Intent mIntent = null;
    private PendingIntent mPendingIntent = null;
    private Notification mNotification = null;
    private NotificationManager mNotificationManager = null;


    @Override
    public void onReceive(Context context, Intent intent) {


        Log.e("AndroidBWL", "AlarmReceiver:onReceive");

        mIntent = intent;

        Bundle bundle = mIntent.getExtras();

        mNotificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
//        mIntent = new Intent(context,AddBwlActivity.class);
        mIntent.setClass(context,mapFragment.class);

        mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
        mNotification = new Notification();
        mNotification.tickerText="备忘录";
        //设置默认声音、默认振动、和默认闪光灯
        mNotification.defaults = Notification.DEFAULT_ALL;
        //点击通知后自动取消
        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
       // mNotification.setLatestEventInfo(context, bundle.getString("title"), bundle.getString("content"), mPendingIntent);
        mNotificationManager.notify(1,mNotification);

    }

}