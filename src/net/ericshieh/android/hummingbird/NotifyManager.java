
package net.ericshieh.android.hummingbird;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotifyManager {
    public int NOTI_HB = 1;
    public int NOTI_NEW_VERSION = 10;
    private NotificationManager nm;
    
    private Context mContext;

    public NotifyManager(Context context) {
        this.mContext=context;
        this.nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public void removeAll() {
        this.nm.cancelAll();
    }

    public void showHBNotify(Context context, boolean isShow) {
        if (isShow) {
            String tickerText = mContext.getString(R.string.hummingbird_running);
            String contentTitle = tickerText;
            String contentText = context.getString(R.string.touch_to_show);
            long now = System.currentTimeMillis();
            
            Intent intent = new Intent(mContext, context.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            
            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            
            Notification notification = new Notification(R.drawable.icon, tickerText, now);
            notification.flags|=Notification.FLAG_ONGOING_EVENT;
            notification.setLatestEventInfo(mContext, contentTitle, contentText, pIntent);

            this.nm.notify(this.NOTI_HB, notification);
        } else {
            this.nm.cancel(this.NOTI_HB);
        }
    }
    
    public Notification getHBNotification(){
        String tickerText = mContext.getString(R.string.hummingbird_running);
        String contentTitle = tickerText;
        String contentText = mContext.getString(R.string.touch_to_show);
        long now = System.currentTimeMillis();
        
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(mContext, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

              
        //Intent intent = new Intent(mContext, curContext.getClass());
        //intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        
        Notification notification = new Notification(R.drawable.icon, tickerText, now);
        notification.flags|=Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(mContext, contentTitle, contentText, pIntent);
        return notification;
    }

}
